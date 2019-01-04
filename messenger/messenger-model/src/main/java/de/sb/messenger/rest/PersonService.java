package de.sb.messenger.rest;

import de.sb.messenger.persistence.*;
import de.sb.toolbox.Copyright;
import de.sb.toolbox.net.RestJpaLifecycleProvider;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static de.sb.messenger.persistence.Group.USER;
import static de.sb.messenger.persistence.Person.PERSON_COMPARATOR;
import static de.sb.messenger.rest.BasicAuthenticationFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

@Path("people")
@Copyright(year = 2018, holders = "Gruppe Drei (Gruppe 5)")
public class PersonService implements PersistenceManagerFactoryContainer {
    /* TODO:
     * Wird durch einen Service eine Relation modifiziert, dann sind nach Speicherung der
     * Änderungen (i.e. commit) zudem alle Entitäten aus dem 2nd-Level Cache zu entfernen
     * deren „mappedBy“-Relationsmengen sich dadurch ändern; diese Spiegel-Mengen werden
     * weder im 1st-Level Cache noch im 2nd-Level Cache automatisch verwaltet:
     *
     * TODO:
     * 3.3 fehlt. Ist das beabsichtigt?
     */

    private static final String QUERY_STRING = "SELECT p from Person as p WHERE "
            + "(:surname is null or p.name.family = :surname) and "
            + "(:forename is null or p.name.given = :forename) and "
            + "(:email is null or p.email = :email) and "
            + "(:street is null or p.address.street = :street) and "
            + "(:city is null or p.address.city = :city) and "
            + "(:postcode is null or p.address.postcode = :postcode) and "
            + "(:group is null or p.group = :group)";

    /**
     * Returns the people matching the given filter criteria, with missing
     * parameters identifying omitted (ausgelassenen) criteria, sorted by family name, given name, email.
     * Search criteria should be any normal property of person and it composites, except
     * identity and password, plus resultOffset and resultLimit which define a result range.
     */
    // TODO creationTImestamp upper lower
    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Collection<Person> queryPeople(
            // Default Values?
            @QueryParam("resultOffset") int resultOffset,
            @QueryParam("resultLimit") int resultLimit,
            @QueryParam("surname") String familyName,
            @QueryParam("forename") String givenName,
            @QueryParam("email") String email,
            @QueryParam("street") String street,
            @QueryParam("postcode") String postCode,
            @QueryParam("city") String city,
            @QueryParam("groupAlias") String group) {

        final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("messenger");

        TypedQuery<Person> query = entityManager.createQuery(QUERY_STRING, Person.class);

        if (resultOffset > 0) query.setFirstResult(resultOffset);
        if (resultLimit > 0) query.setMaxResults(resultLimit);
        query.setParameter("surname", familyName);
        query.setParameter("forename", givenName);
        query.setParameter("email", email);
        query.setParameter("street", street);
        query.setParameter("postcode", postCode);
        query.setParameter("city", city);
        query.setParameter("group", group);

        List<Person> people = query.getResultList();
        people.sort(PERSON_COMPARATOR);
        return people;
    }

    /**
     * POST /people
     * Creates or updates a person from template data within the HTTP request body in application/json format.
     * It creates a new person if the given template' identity is zero, otherwise it updates the corresponding
     * person with the given data. Use the Header field Requester-Identity to make sure the requester is either
     * the person to be modified, or an administrator this header field will later be supplied during
     * authentication. Also, make sure non-administrators don't set their Group to ADMIN.
     * Optionally, a new password may be set using the header field Set-Password. Returns
     * the affected person's identity as text/plain
     * ADMIN, USER
     */
    @POST
    @Consumes({APPLICATION_JSON})
    @Produces({TEXT_PLAIN})
    public long createUpdatePerson(
            @HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
            @HeaderParam("Set-Password") String setPassword,
            @NotNull Person personTemplate
    ) {
        long affectedPersonId = -1;
        final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("messenger");
        Person requester = entityManager.find(Person.class, requesterIdentity);
        if (requester == null) throw new ClientErrorException(FORBIDDEN);

        if (personTemplate.getIdentity() != requesterIdentity && requester.getGroup() != Group.ADMIN)
            throw new ClientErrorException(FORBIDDEN);

        // TODO keine fallunterscheidung!

        switch (requester.getGroup()) {
            // check if requester is Admin
            case ADMIN:
                entityManager.getTransaction().begin();

                try {
                    // when the person does not exist create a new one and set the avatar default
                    if (entityManager.find(Person.class, personTemplate.getIdentity()) == null) {
                        personTemplate.setAvatar(entityManager.find(Document.class, 1L));
                        entityManager.persist(personTemplate);
                    } else { // otherwise this is an update so pls merge
                        entityManager.merge(personTemplate);
                    }

                    entityManager.getTransaction().commit();
                    affectedPersonId = personTemplate.getIdentity();
                } catch (final RollbackException exception) {
                    entityManager.getTransaction().rollback();
                    throw new ClientErrorException(CONFLICT);
                } finally {
                    entityManager.getTransaction().begin();
                }
                break;
            // you are a user
            case USER:
                if (requesterIdentity != personTemplate.getIdentity()) throw new ClientErrorException(UNAUTHORIZED);

                // set group always to user if requester is a user on his own
                personTemplate.setGroup(USER);
                entityManager.getTransaction().begin();

                // merge the user with his new data
                try {
                    entityManager.merge(personTemplate);
                    entityManager.getTransaction().commit();
                    affectedPersonId = personTemplate.getIdentity();
                } catch (final RollbackException exception) {
                    entityManager.getTransaction().rollback();
                    throw new ClientErrorException(CONFLICT);
                } finally {
                    entityManager.getTransaction().begin();
                }

                break;
        }

        return affectedPersonId;
    }


    /**
     * GET /people/{id}
     * Returns the person matching the given identity, or the person
     * matching the given header field Requester-Identity if the former is zero. The header
     * field will later be injected during successful authentication.
     */
    @GET
    @Path("{id}")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Person queryPerson(
            @HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
            @PathParam("id") @PositiveOrZero final long personIdentity
    ) {
        final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("messenger");
        final long identity = personIdentity == 0 ? requesterIdentity : personIdentity;
        Person person = entityManager.find(Person.class, identity);

        if (person == null) throw new ClientErrorException(NOT_FOUND);
        return person;
    }


    /**
     * GET /people/{id}/avatar
     * Returns the avatar content of the person matching the
     * given identity, plus it's content type as part of the HTTP response header. Use
     * MediaType.WILDCARD to declare production of an a priori unknown media type, and return
     * an instance of Response that contains both the media type and the content. If the query
     * parameters width and height are present, then scale the returned image content using
     * Document#scaledImageContent().
     */
    @GET
    @Path("{id}/avatar")
    @Produces({WILDCARD})
    public Response queryAvatar(
            @HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
            @PathParam("id") @Positive final long personIdentity,
            @QueryParam("width") final int width,
            @QueryParam("height") final int height
    ) {
        final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("messenger");

        final Person person = entityManager.find(Person.class, personIdentity);
        if (person == null) throw new ClientErrorException(NOT_FOUND);

        final Document avatar = person.getAvatar();

        final byte[] content;
        if (width == 0 || height == 0) {
            content = avatar.getContent();
        } else {
            content = Document.scaledImageContent("jpg", avatar.getContent(), width, height);
        }
        return Response.ok(content, avatar.getContentType()).build();
    }

    /**
     * PUT /people/{id}/avatar
     * Updates the given person's avatar using the document
     * content passed within the HTTP request body, and the media type passed as HeaderField
     * Content-Type. Use MediaType.WILDCARD to declare consumption of an a priori
     * unknown media type. If the given content is empty, the person's avatar shall be set to
     * the default document (identity=1). Otherwise, if a document matching the hash or the
     * given content already exists, then this document shall become the person's avatar.
     * Otherwise, the given content and content-type is used to create a new document which
     * becomes the person's avatar. Make sure the Header field Requester-Identity matches
     * the given person identity this header field will later be supplied during authentication.
     */
    @PUT
    @Path("{id}/avatar")
    @Consumes({APPLICATION_JSON, WILDCARD})
    public long updateAvatar(
            @PathParam("id") final long personIdentity,
            @HeaderParam(REQUESTER_IDENTITY) final long requesterIdentity,
            @HeaderParam("Content-Type") String contentType,
            @NotNull byte[] content
    ) {
        final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("messenger");
        Person requester = entityManager.find(Person.class, requesterIdentity);
        Person person = entityManager.find(Person.class, personIdentity);

        if (requester == null) throw new ClientErrorException(NOT_FOUND);

        if (person == null) throw new ClientErrorException(NOT_FOUND);

        if (personIdentity != requesterIdentity && requester.getGroup() != Group.ADMIN) throw new ClientErrorException(FORBIDDEN);

        // if a Document is found, the request body contains the exact picture which can be found in the database
        // TODO Parameter
        byte[] contentHash = HashTools.sha256HashCode(content);
        List<Document> docs = entityManager.createQuery("SELECT doc from Document doc WHERE contentHash =" + contentHash, Document.class).getResultList();

        Document doc;

        if (docs.isEmpty()) {
            doc = new Document();
            doc.setContent(content);
            doc.setContentType(contentType);
            entityManager.persist(doc);
        } else {
            doc = docs.get(0);
            doc.setContentType(contentType);
            entityManager.flush();
        }

        commitBegin(entityManager);

        person.setAvatar(doc);
        entityManager.flush();

        commitBegin(entityManager);

        return person.getIdentity();
    }

    private void commitBegin(EntityManager entityManager) {
        try {
            entityManager.getTransaction().commit();
        } catch (final RollbackException exception) {
            entityManager.getTransaction().rollback();
            throw new ClientErrorException(CONFLICT);
        } finally {
            entityManager.getTransaction().begin();
        }
    }

    /**
     * GET /people/{id}/messagesAuthored
     * Returns the messages authored by the
     * person matching the given identity, sorted by identity
     */
    @GET
    @Path("{id}/messagesAuthored")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Collection<Message> queryPersonMessages(
            @HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
            @PathParam("id") @Positive final long entityIdentity
    ) {
        final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("messenger");
        Person person = entityManager.find(Person.class, entityIdentity);
        Person requester = entityManager.find(Person.class, requesterIdentity);

        if (requesterIdentity == entityIdentity || requester.getGroup() == Group.ADMIN)
            person = entityManager.find(Person.class, entityIdentity);

        if (person == null) throw new ClientErrorException(NOT_FOUND);

        final List<Message> messages = new ArrayList<>(person.getMessagesAuthored());
        messages.sort(Comparator.naturalOrder());
        return messages;
    }

    /**
     * PUT /people/{id}/peopleObserved
     * Updates the given person to monitor the
     * people matching the form-supplied collection of person identities in application/x-wwwform-urlencoded
     * format, meaning as multiple form-entries sharing the same name.
     * Make sure the Header field Requester-Identity matches the given person identity
     * this header field will later be supplied during authentication.
     */
    @PUT
    @Path("{id}/peopleObserved")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Collection<Person> updatePeopleObserved(
            @HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
            @PathParam("id") @Positive final long entityIdentity
    ) {
        final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("messenger");
        Person requester = entityManager.find(Person.class, requesterIdentity);
        Person person = new Person(new Document() {
        });

        if (requesterIdentity == entityIdentity || requester.getGroup() == Group.ADMIN)
            person = entityManager.find(Person.class, entityIdentity);

        if (person == null)
            throw new ClientErrorException(NOT_FOUND);

        final List<Person> people = new ArrayList<>(person.getPeopleObserved());
        people.sort(PERSON_COMPARATOR);
        return people;
    }
}
