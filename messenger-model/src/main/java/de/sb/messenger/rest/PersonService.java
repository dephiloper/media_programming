package de.sb.messenger.rest;

import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Person;
import de.sb.toolbox.Copyright;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.validation.constraints.Positive;
import javax.ws.rs.*;
import java.util.List;
import java.util.Set;

import static de.sb.messenger.rest.BasicAuthenticationFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NOT_IMPLEMENTED;

@Path("people")
@Copyright(year = 2018, holders = "Gruppe Drei (Gruppe 5)")
public class PersonService {
    private static EntityManagerFactory entityManagerFactory;

    /**
     * TODO: GET /people
     * Returns the people matching the given filter criteria, with missing
     * parameters identifying omitted criteria, sorted by family name, given name, email.
     * Search criteria should be any “normal” property of person and it’s composites, except
     * identity and password, plus resultOffset and resultLimit which define a result range.
     */
    @GET
    @Produces({ APPLICATION_JSON, APPLICATION_XML })
    public Set<Person> queryPeople (/*@QueryParam("filter") int filter*/) {
        final EntityManager messengerManager = entityManagerFactory.createEntityManager();
        /*final Set<Person> entity = messengerManager.find(Person.class);
        if (entity == null) throw new ClientErrorException(NOT_FOUND);

        return entity;*/

        throw new ClientErrorException(NOT_IMPLEMENTED);
    }

    /**
     * TODO: POST /people
     * Creates or updates a person from template data within the HTTP request body in application/json format.
     * It creates a new person if the given template' identity is zero, otherwise it updates the corresponding
     * person with the given data. Use the Header field “Requester-Identity” to make sure the requester is either
     * the person to be modified, or an administrator – this header field will later be supplied during
     * authentication. Also, make sure non-administrators don’t set their Group to ADMIN.
     * Optionally, a new password may be set using the header field “Set-Password”. Returns
     * the affected person's identity as text/plain
     */
    @POST
    @Consumes({ APPLICATION_JSON })
    @Produces({ TEXT_PLAIN })
    public String createUpdatePerson(@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
            /*@FormParam("name") String name,*/) {
        final EntityManager messengerManager = entityManagerFactory.createEntityManager();
        /*final Set<Person> entity = messengerManager.find(Person.class);
        if (entity == null) throw new ClientErrorException(NOT_FOUND);

        return entity;*/

        throw new ClientErrorException(NOT_IMPLEMENTED);
    }


    /**
     * TODO: GET /people/{id}
     * Returns the person matching the given identity, or the person
     * matching the given header field “Requester-Identity” if the former is zero. The header
     * field will later be injected during successful authentication.
     */
    @GET
    @Path("{id}")
    @Produces({ APPLICATION_JSON, APPLICATION_XML })
    public Person queryPerson (
            @HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
            @PathParam("id") @Positive final long entityIdentity) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        Person person = entityManager.find(Person.class, entityIdentity);

        if (person == null)
            person = entityManager.find(Person.class,requesterIdentity);

        if (person == null)
            throw new ClientErrorException(NOT_FOUND);

        return person;
    }

    /**
     * TODO: GET /people/{id}/avatar
     * Returns the avatar content of the person matching the
     * given identity, plus it's content type as part of the HTTP response header. Use
     * MediaType.WILDCARD to declare production of an a priori unknown media type, and return
     * an instance of Result that contains both the media type and the content. If the query
     * parameters width and height are present, then scale the returned image content using
     * Document#scaledImageContent().
     */
    @GET
    @Path("{id}/avatar")
    @Produces({ APPLICATION_JSON, APPLICATION_XML })
    public Document queryAvatar (
            @HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
            @PathParam("id") @Positive final long entityIdentity) {
        final EntityManager messengerManager = entityManagerFactory.createEntityManager();
        /*final Set<Person> entity = messengerManager.find(Person.class);
        if (entity == null) throw new ClientErrorException(NOT_FOUND);

        return entity;*/

        throw new ClientErrorException(NOT_IMPLEMENTED);
    }

    /**
     * TODO: PUT /people/{id}/avatar
     * Updates the given person's avatar using the document
     * content passed within the HTTP request body, and the media type passed as HeaderField
     * “Content-Type”. Use MediaType.WILDCARD to declare consumption of an a priori
     * unknown media type. If the given content is empty, the person's avatar shall be set to
     * the default document (identity=1). Otherwise, if a document matching the hash of the
     * given content already exists, then this document shall become the person's avatar.
     * Otherwise, the given content and content-type is used to create a new document which
     * becomes the person's avatar. Make sure the Header field “Requester-Identity” matches
     * the given person identity – this header field will later be supplied during authentication.
     */
    @PUT
    @Path("{id}/avatar")
    @Consumes(APPLICATION_JSON)
    public String updateAvatar() {
        final EntityManager messengerManager = entityManagerFactory.createEntityManager();
        throw new ClientErrorException(NOT_IMPLEMENTED);
    }

    /**
     * TODO: GET /people/{id}/messagesAuthored
     * Returns the messages authored by the
     * person matching the given identity, sorted by identity
     */
    @GET
    @Path("{id}/messagesAuthored")
    @Produces({ APPLICATION_JSON, APPLICATION_XML })
    public Set<Document> queryPersonMessages (
            @HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
            @PathParam("id") @Positive final long entityIdentity) {
        final EntityManager messengerManager = entityManagerFactory.createEntityManager();
        /*final Set<Person> entity = messengerManager.find(Person.class);
        if (entity == null) throw new ClientErrorException(NOT_FOUND);

        return entity;*/

        throw new ClientErrorException(NOT_IMPLEMENTED);
    }

    /**
     * TODO: PUT /people/{id}/peopleObserved
     * Updates the given person to monitor the
     * people matching the form-supplied collection of person identities in application/x-wwwform-urlencoded
     * format, meaning as multiple form-entries sharing the same name.
     * Make sure the Header field “Requester-Identity” matches the given person identity –
     * this header field will later be supplied during authentication.
     */
    @PUT
    @Path("{id}/peopleObserved")
    @Produces({ APPLICATION_JSON, APPLICATION_XML })
    public String updatePeopleObserved (
            @HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
            @PathParam("id") @Positive final long entityIdentity) {
        final EntityManager messengerManager = entityManagerFactory.createEntityManager();
        /*final Set<Person> entity = messengerManager.find(Person.class);
        if (entity == null) throw new ClientErrorException(NOT_FOUND);

        return entity;*/

        throw new ClientErrorException(NOT_IMPLEMENTED);
    }
}
