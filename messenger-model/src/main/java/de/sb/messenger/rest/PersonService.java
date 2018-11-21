package de.sb.messenger.rest;

import de.sb.messenger.persistence.Document;
import de.sb.messenger.persistence.Group;
import de.sb.messenger.persistence.HashTools;
import de.sb.messenger.persistence.Message;
import de.sb.messenger.persistence.Person;
import de.sb.toolbox.Copyright;

import javax.enterprise.context.BeforeDestroyed;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Positive;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static de.sb.messenger.rest.BasicAuthenticationFilter.REQUESTER_IDENTITY;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Path("people")
@Copyright(year = 2018, holders = "Gruppe Drei (Gruppe 5)")
public class PersonService implements PersistenceManagerFactoryContainer{
    /**
     * TODO: GET /people
     * Returns the people matching the given filter criteria, with missing
     * parameters identifying omitted (ausgelassenen) criteria, sorted by family name, given name, email.
     * Search criteria should be any â€œnormalâ€� property of person and itâ€™s composites, except
     * identity and password, plus resultOffset and resultLimit which define a result range.
     */
    @GET
    @Produces({ APPLICATION_JSON, APPLICATION_XML })
    public Collection<Person> queryPeople (
    		// Default Values?
    		@QueryParam("resultOffset") int resultOffset,
    		@QueryParam("resultLimit") int resultLimit,
   			@QueryParam("familyName") String familyName, 
   			@QueryParam("givenName") String givenName, 
   			@QueryParam("email") String email,
   			@QueryParam("street") String street,
   			@QueryParam("postCode") String postCode,
   			@QueryParam("city") String city,
   			@QueryParam("group") String group){
    		
    	final EntityManager entityManager = entityManagerFactory.createEntityManager();
    	
    	Person [] people;
    	// query maybe static
    	String query = "SELECT p from Person as p WHERE "
    			           + "(:familyName is null or p.familyName = :familyName) and "
    					   + "(:givenName is null or p.givenName = :givenName) and "
    					   + "(:email is null or p.email = :email) and "
    					   + "(:street is null or p.street = :street) and "
    					   + "(:city is null or p.city = :city) and "
    					   + "(:postCode is null or p.postCode) and "
    					   + "(:group is null or p.group = :group)";
    	
    	
    	TypedQuery<Person> query2 = entityManager.createQuery(query, Person.class);
    	//TODO Parameter declaration (familyName to group)
    	if (resultOffset>0) query2.setFirstResult(resultOffset);
    	if (resultLimit>0) query2.setMaxResults(resultLimit);
    	
    	query2.setParameter("familyName", familyName);
    	List<Person> peopleList = query2.getResultList();
        
    	// TODO static
        Collections.sort(peopleList, Comparator.comparing(Person::getName).thenComparing(Person::getEmail));
        
        //TODO try catch -finally-
    	entityManager.close();

    	return peopleList;
    }

    /**
     * TODO: POST /people
     * Creates or updates a person from template data within the HTTP request body in application/json format.
     * It creates a new person if the given template' identity is zero, otherwise it updates the corresponding
     * person with the given data. Use the Header field â€œRequester-Identityâ€� to make sure the requester is either
     * the person to be modified, or an administrator â€“ this header field will later be supplied during
     * authentication. Also, make sure non-administrators donâ€™t set their Group to ADMIN.
     * Optionally, a new password may be set using the header field â€œSet-Passwordâ€�. Returns
     * the affected person's identity as text/plain
     */
    @POST
    @Consumes({ APPLICATION_JSON })
    @Produces({ TEXT_PLAIN })
    public long createUpdatePerson(
            @HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
            @HeaderParam("Set-Password") String setPassword,
            Person personTemplate) {

        // TODO: method can probably be simplified

        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        Person requester = entityManager.find(Person.class, requesterIdentity);

        // check if requester is null
        if (requester == null) throw new ClientErrorException(FORBIDDEN);
        // TODO check if requester is user
        final Person person;
        final boolean insertMode = personTemplate.getIdentity() == 0;
        if(insertMode) {
        	final Document avatar = null;
        	person = new Person(avatar);
        } else {
        	person = entityManager.find(Person.class, personTemplate.getIdentity()); 
        }
        // TODO set all attributes from 
        // TODO if else (isertmode) persist !insertmode flush
        // TODO commit
        // creates person if requester is admin
        if (personTemplate.getIdentity() == 0 && requester.getGroup() == Group.ADMIN) {
            //TODO set password: setPassword
            entityManager.getTransaction().begin();
            entityManager.persist(person);

            try {
                entityManager.getTransaction().commit();
            } catch (final RollbackException exception) {
                throw new ClientErrorException(CONFLICT);
            } finally {
                entityManager.getTransaction().rollback();
            }

        }
        // (see description above) Use the Header field â€œRequester-Identityâ€� to make sure the requester is either
        // the person to be modified, or an administrator
        else if (person.getIdentity() == requesterIdentity || requester.getGroup() == Group.ADMIN) {

            if (requester.getGroup() != Group.ADMIN)
                person.setGroup(Group.USER);

            //TODO set password: setPassword
            entityManager.getTransaction().begin();
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
     * TODO: GET /people/{id}
     * Returns the person matching the given identity, or the person
     * matching the given header field â€œRequester-Identityâ€� if the former is zero. The header
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
            throw new ClientErrorException(NOT_FOUND);

        entityManager.close();
        return person;
    }

    /**
     * TODO: GET /people/{id}/avatar
     * Returns the avatar content of the person matching the
     * given identity, plus it's content type as part of the HTTP response header. Use
     * MediaType.WILDCARD to declare production of an a priori unknown media type, and return
     * an instance of Response that contains both the media type and the content. If the query
     * parameters width and height are present, then scale the returned image content using
     * Document#scaledImageContent().
     */
    @GET
    @Path("{id}/avatar")
    @Produces({ WILDCARD })
    public Response queryAvatar (
            @HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
            @PathParam("id") @Positive final long entityIdentity,
            @QueryParam("width") final long width,
            @QueryParam("height") final long height) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();

        Person person = entityManager.find(Person.class, entityIdentity);

        Response.ResponseBuilder rBuild = Response.status(Response.Status.NOT_FOUND);

        if (person != null && person.getAvatar() != null) {
            rBuild = Response.ok(person.getAvatar(), APPLICATION_JSON);

            if (width != 0 && height != 0) {
                //TODO: person.avatar.scaledImageContent()
            }
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
        	List<Document> docs = entityManager.createQuery("SELECT doc from Document doc WHERE contentHash =" + HashTools.sha256HashCode(body)).getResultList();
            Document doc = docs.get(0);
        	
        	entityManager.getTransaction().begin();
            
            if (body == null) { // TODO set avatar default
            	person.avatar = entityManager.find(Document.class, 1L);
            } else if (doc != null) {
            	person.avatar = doc;
            } else {
            	person.avatar = new Document(HashTools.sha256HashCode(body), body, type);
            }	
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
     * TODO: GET /people/{id}/messagesAuthored
     * Returns the messages authored by the
     * person matching the given identity, sorted by identity
     */
    @GET
    @Path("{id}/messagesAuthored")
    @Produces({ APPLICATION_JSON, APPLICATION_XML })
    public Message[] queryPersonMessages (
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
        return person.messagesAuthored.toArray(new Message[person.messagesAuthored.size()]);
    }

    /**
     * TODO: PUT /people/{id}/peopleObserved
     * Updates the given person to monitor the
     * people matching the form-supplied collection of person identities in application/x-wwwform-urlencoded
     * format, meaning as multiple form-entries sharing the same name.
     * Make sure the Header field â€œRequester-Identityâ€� matches the given person identity â€“
     * this header field will later be supplied during authentication.
     */
    @PUT
    @Path("{id}/peopleObserved")
    @Produces({ APPLICATION_JSON, APPLICATION_XML })
    public Person[] updatePeopleObserved (
            @HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
            @PathParam("id") @Positive final long entityIdentity) {
    	
    	final EntityManager entityManager = entityManagerFactory.createEntityManager();
        Person requester = entityManager.find(Person.class, requesterIdentity);
        Person person = new Person(new Document(){}); 
    	
    	if (requesterIdentity == entityIdentity || requester.getGroup() == Group.ADMIN)
    		person = entityManager.find(Person.class, entityIdentity);
        
        if (person == null)
            throw new ClientErrorException(NOT_FOUND);

        entityManager.close();
        return person.getPeopleObserved().toArray(new Person[person.getPeopleObserved().size()]);
    }
}
