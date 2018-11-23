package de.sb.messenger.rest;

import de.sb.messenger.persistence.*;
import de.sb.toolbox.Copyright;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Positive;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static de.sb.messenger.persistence.Group.USER;
import static de.sb.messenger.rest.BasicAuthenticationFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

@Path("people")
@Copyright(year = 2018, holders = "Gruppe Drei (Gruppe 5)")
public class PersonService implements PersistenceManagerFactoryContainer {
    private static final Comparator<Person> personComparator = Comparator.comparing(Person::getName).thenComparing(Person::getEmail);

    private static final String QUERY_STRING = "SELECT p from Person as p WHERE "
            + "(:familyName is null or p.familyName = :familyName) and "
            + "(:givenName is null or p.givenName = :givenName) and "
            + "(:email is null or p.email = :email) and "
            + "(:street is null or p.street = :street) and "
            + "(:city is null or p.city = :city) and "
            + "(:postCode is null or p.postCode) and "
            + "(:group is null or p.group = :group)";

    /**
     * Returns the people matching the given filter criteria, with missing
     * parameters identifying omitted (ausgelassenen) criteria, sorted by family name, given name, email.
     * Search criteria should be any â€œnormalâ€� property of person and itâ€™s composites, except
     * identity and password, plus resultOffset and resultLimit which define a result range.
     */
    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Collection<Person> queryPeople(
            // Default Values?
            @QueryParam("resultOffset") int resultOffset,
            @QueryParam("resultLimit") int resultLimit,
            @QueryParam("familyName") String familyName,
            @QueryParam("givenName") String givenName,
            @QueryParam("email") String email,
            @QueryParam("street") String street,
            @QueryParam("postCode") String postCode,
            @QueryParam("city") String city,
            @QueryParam("group") String group) {

        final EntityManager entityManager = entityManagerFactory.createEntityManager();

        TypedQuery<Person> query = entityManager.createQuery(QUERY_STRING, Person.class);

        if (resultOffset > 0) query.setFirstResult(resultOffset);
        if (resultLimit > 0) query.setMaxResults(resultLimit);
        if (familyName != null) query.setParameter("familyName", familyName);
        if (givenName != null) query.setParameter("givenName", givenName);
        if (email != null) query.setParameter("email", email);
        if (street != null) query.setParameter("street", street);
        if (postCode != null) query.setParameter("postCode", postCode);
        if (city != null) query.setParameter("city", city);
        if (group != null) query.setParameter("group", group);

        try {
            List<Person> peopleList = query.getResultList();
            peopleList.sort(personComparator);
            return peopleList;
        } finally {
            entityManager.close();
        }
    }

    /**
     * POST /people
     * Creates or updates a person from template data within the HTTP request body in application/json format.
     * It creates a new person if the given template' identity is zero, otherwise it updates the corresponding
     * person with the given data. Use the Header field Requester-Identity to make sure the requester is either
     * the person to be modified, or an administrator this header field will later be supplied during
     * authentication. Also, make sure non-administrators don't set their Group to ADMIN.
     * Optionally, a new password may be set using the header field â€œSet-Password. Returns
     * the affected person's identity as text/plain
     * ADMIN, USER
     */
    @POST
    @Consumes({APPLICATION_JSON})
    @Produces({TEXT_PLAIN})
    public long createUpdatePerson(
            @HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
            @HeaderParam("Set-Password") String setPassword,
            Person personTemplate) {

        long affectedPersonId = -1;
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        Person requester = entityManager.find(Person.class, requesterIdentity);

        // check if requester is null
        if (requester == null) throw new ClientErrorException(FORBIDDEN);


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
                    throw new ClientErrorException(CONFLICT);
                } finally {
                    entityManager.getTransaction().rollback();
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
                    throw new ClientErrorException(CONFLICT);
                } finally {
                    entityManager.getTransaction().rollback();
                }

                break;
        }

        return affectedPersonId;
    }


    /**
     * GET /people/{id}
     * Returns the person matching the given identity, or the person
     * matching the given header field â€œRequester-Identityâ€� if the former is zero. The header
     * field will later be injected during successful authentication.
     */
    @GET
    @Path("{id}")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Person queryPerson(
            @HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
            @PathParam("id") @Positive final long entityIdentity) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        Person person = entityManager.find(Person.class, entityIdentity);

        if (person == null)
            throw new ClientErrorException(NOT_FOUND);

        entityManager.close();
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
            @PathParam("id") @Positive final long entityIdentity,
            @QueryParam("width") final long width,
            @QueryParam("height") final long height) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();

        Person person = entityManager.find(Person.class, entityIdentity);

        Response.ResponseBuilder rBuild;

        if (person != null && person.getAvatar() != null) {
            Document image = person.getAvatar();

            if (width != 0 && height != 0) {
                rBuild = Response.ok(person.getAvatar().scaledImageContent("jpg", image.getContent(), (int)width, (int)height));
            } else {
                rBuild = Response.status(Response.Status.BAD_REQUEST);
            }
        } else {
            rBuild = Response.status(Response.Status.BAD_REQUEST);
        }

        return rBuild.build();
    }

    /**
     * TODO: PUT /people/{id}/avatar
     * Updates the given person's avatar using the document
     * content passed within the HTTP request body, and the media type passed as HeaderField
     * â€œContent-Typeâ€�. Use MediaType.WILDCARD to declare consumption of an a priori
     * unknown media type. If the given content is empty, the person's avatar shall be set to
     * the default document (identity=1). Otherwise, if a document matching the hash or the
     * given content already exists, then this document shall become the person's avatar.
     * Otherwise, the given content and content-type is used to create a new document which
     * becomes the person's avatar. Make sure the Header field â€œRequester-Identityâ€� matches
     * the given person identity â€“ this header field will later be supplied during authentication.
     */
    @PUT
    @Path("{id}/avatar")
    @Consumes({APPLICATION_JSON, WILDCARD})
    public long updateAvatar(
            @PathParam("id") final long personIdentity,
            @HeaderParam(REQUESTER_IDENTITY) final long requesterIdentity,
            @HeaderParam("Content-Type") String type, byte[] body) {

        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        Person requester = entityManager.find(Person.class, requesterIdentity);
        Person person = entityManager.find(Person.class, personIdentity);

        if (requester == null)
            throw new ClientErrorException(NOT_FOUND);

        if (person == null) {
            throw new ClientErrorException(NOT_FOUND);
        } else if (personIdentity == requesterIdentity || requester.getGroup() == Group.ADMIN) {
        	        	
        	// if a Document is found, the request body contains the exact picture which can be found in the database
        	List<Document> docs = entityManager.createQuery("SELECT doc from Document doc WHERE contentHash =" + HashTools.sha256HashCode(body), Document.class).getResultList();
            Document doc = docs.get(0);

            entityManager.getTransaction().begin();

            if (body == null) { // TODO set avatar default
                person.setAvatar(entityManager.find(Document.class, 1L));
            } else if (doc == null) {
                doc = new Document();
                doc.setContent(body);
                doc.setContentType(type);
            }

            person.setAvatar(doc);

            entityManager.merge(person);
            try {
                entityManager.getTransaction().commit();
            } catch (final RollbackException exception) {
                throw new ClientErrorException(CONFLICT);
            } finally {
                entityManager.getTransaction().rollback();
            }
        }

        entityManager.close();
        return person.getIdentity();
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
            @PathParam("id") @Positive final long entityIdentity) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        Person person = entityManager.find(Person.class, entityIdentity);
        Person requester = entityManager.find(Person.class, requesterIdentity);

        if (requesterIdentity == entityIdentity || requester.getGroup() == Group.ADMIN)
            person = entityManager.find(Person.class, entityIdentity);

        if (person == null)
            throw new ClientErrorException(NOT_FOUND);

        entityManager.close();

        return person.getMessagesAuthored();
    }

    /**
     * PUT /people/{id}/peopleObserved
     * Updates the given person to monitor the
     * people matching the form-supplied collection of person identities in application/x-wwwform-urlencoded
     * format, meaning as multiple form-entries sharing the same name.
     * Make sure the Header field â€œRequester-Identityâ€� matches the given person identity â€“
     * this header field will later be supplied during authentication.
     */
    @PUT
    @Path("{id}/peopleObserved")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Collection<Person> updatePeopleObserved(
            @HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
            @PathParam("id") @Positive final long entityIdentity) {

        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        Person requester = entityManager.find(Person.class, requesterIdentity);
        Person person = new Person(new Document() {
        });

        if (requesterIdentity == entityIdentity || requester.getGroup() == Group.ADMIN)
            person = entityManager.find(Person.class, entityIdentity);

        if (person == null)
            throw new ClientErrorException(NOT_FOUND);

        entityManager.close();
        return person.getPeopleObserved();
    }
}
