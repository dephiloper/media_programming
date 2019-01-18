package de.sb.messenger.rest;

import de.sb.messenger.persistence.BaseEntity;
import de.sb.messenger.persistence.Message;
import de.sb.messenger.persistence.Person;
import de.sb.toolbox.Copyright;
import de.sb.toolbox.net.RestJpaLifecycleProvider;

import javax.persistence.*;
import javax.validation.constraints.*;
import javax.ws.rs.*;

import java.util.*;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

@Path("messages")
@Copyright(year = 2018, holders = "Gruppe Drei (Gruppe 5)")
public class MessageService implements PersistenceManagerFactoryContainer {

    private static final String QUERY_STRING = "SELECT m.identity from Message as m WHERE "
            + "(:bodyFragment is null or m.body like :bodyFragment) and "
            + "(:lowerCreationTimestamp is null or m.creationTimestamp >= :lowerCreationTimestamp) and"
            + "(:upperCreationTimestamp is null or m.creationTimestamp <= :upperCreationTimestamp)";

    /**
     * Returns the messages matching the given criteria, with missing
     * parameters identifying omitted criteria, sorted by identity. Search criteria should be a
     * message body fragment (compare using the like operator), an upper/lower creation
     * timestamp, plus resultOffset and resultLimit which define a result range.
     */
    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Message[] queryMessages (
            @QueryParam("bodyFragment") String bodyFragment,
            @QueryParam("lowerCreationTimestamp") Long lowerCreationTimestamp,
            @QueryParam("upperCreationTimestamp") Long upperCreationTimestamp,
            @QueryParam("resultOffset") int resultOffset,
            @QueryParam("resultLimit") int resultLimit
    ) {
    	final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("messenger");
        TypedQuery<Long> query = entityManager.createQuery(QUERY_STRING, Long.class);

        if (resultOffset > 0) query.setFirstResult(resultOffset);
        if (resultLimit > 0) query.setMaxResults(resultLimit);

        query.setParameter("bodyFragment", bodyFragment);
        query.setParameter("lowerCreationTimestamp", lowerCreationTimestamp);
        query.setParameter("upperCreationTimestamp", upperCreationTimestamp);
        
        List<Long> messageList = query.getResultList();
        // TODO: make other queries like this
        return messageList
                .stream()
                .map(reference -> entityManager.find(Message.class, reference))
                .filter(message -> message != null)
                .sorted(Comparator.naturalOrder())
                .toArray(length -> new Message[length]);
    }

    /**
     * Returns the message matching the given identity.
     */
    @GET
    @Path("{id}")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Message queryMessage(@PathParam("id") @Positive final long messageIdentity) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        Message message = entityManager.find(Message.class, messageIdentity);

        if (message == null) throw new ClientErrorException(NOT_FOUND);
        return message;
    }

    /**
     * Creates a new message, using an HTTP form as a body
     * parameter (content type "application/x-www-form-urlencoded") with the fields body
     * (message body) and subjectReference (message subject), and Header field
     * Requester-Identity (message author) this header field will later be supplied during
     * authentication. Return the affected message's identity as text/plain.
     */
    
    @POST
    @Produces(TEXT_PLAIN)
    @Consumes(TEXT_PLAIN)
    public long createMessage(@NotNull String body,
                              @Positive @QueryParam("subjectReference") long subjectReference,
                              @Positive @HeaderParam("Requester-Identity") long requesterIdentity
    ) {
        final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("messenger");

        Person requester = entityManager.find(Person.class, requesterIdentity);
        if (requester == null) throw new ClientErrorException(FORBIDDEN);

        BaseEntity subject = entityManager.find(BaseEntity.class, subjectReference);
        if (subject == null) throw new ClientErrorException(NOT_FOUND);

        final Message message = new Message(requester, subject);
        message.setBody(body);
        message.generateCreationTimestampFromSystemTime();

        Collection<Message> messagesCaused = subject.getMessagesCaused();
        messagesCaused.add(message);
        subject.setMessagesCaused(new HashSet<>(messagesCaused));

        entityManager.persist(message);

        try {
        	entityManager.getTransaction().commit();
        } catch (final RollbackException exception) {
        	throw new ClientErrorException(CONFLICT);
        } finally {
        	entityManager.getTransaction().begin();
        }

        return message.getIdentity();
    }

    /*
     * Sind dabei Filter-Queries gefordert, so definiert als Query-Parameter ein Suchkriterium
     * pro textuellem Entity-Feld, und zwei für numerische Felder (für >= und <= Vergleich).
     * Definiert zudem einen JP-QL Query nach folgendem Muster (statt = kann für mehr
     * Flexibilität auch like verwendet werden, ist aber deutlich teurer in der Ausführung):
     *
     * select x from X as x where
     * (:lowerNumber is null or x.number >= :lowerNumber) and
     * (:upperNumber is null or x.number <= :upperNumber) and
     * (:text is null or x.text = :text) ...
     *
     * Wird durch einen Service eine Relation modifiziert, dann sind nach Speicherung der
     * Änderungen (i.e. commit) zudem alle Entitäten aus dem 2nd-Level Cache zu entfernen
     * deren mappedBy-Relationsmengen sich dadurch ändern; diese Spiegel-Mengen werden
     * weder im 1st-Level Cache noch im 2nd-Level Cache automatisch verwaltet:
     *
     * cache = entityManager.getEntityManagerFactory().getCache();
     * cache.evict(entity.getClass(), entity.getIdentity());
     *
     * Beachtet: Streng genommen müssten wir dieselbe Cache-Invalidierung auch mit dem
     * 1st-Level Cache durchführen (z.B. mittels entityManager#refresh(entity)); dieser
     * wird jedoch im Web-Services sowieso zeitnah geschlossen, daher können wir diesen
     * Aufwand fast immer sparen.
     */
}
