package de.sb.messenger.rest;

import de.sb.messenger.persistence.BaseEntity;
import de.sb.messenger.persistence.Message;
import de.sb.messenger.persistence.Person;
import de.sb.toolbox.Copyright;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.ws.rs.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("messages")
@Copyright(year = 2018, holders = "Gruppe Drei (Gruppe 5)")
public class MessageService implements PersistenceManagerFactoryContainer {
    private static final EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory("messenger");

    /**
     * Returns the messages matching the given criteria, with missing
     * parameters identifying omitted criteria, sorted by identity. Search criteria should be a
     * message body fragment (compare using the like operator), an upper/lower creation
     * timestamp, plus resultOffset and resultLimit which define a result range.
     */
    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Collection<Message> queryMessages(
            @QueryParam("fragment") String fragment,
            @QueryParam("lowerCreationTimestamp") Long lowerCreationTimestamp,
            @QueryParam("upperCreationTimestamp") Long upperCreationTimestamp,
            @QueryParam("resultOffset") Integer resultOffset,
            @QueryParam("resultLimit") Integer resultLimit) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();

        StringBuilder queryStrings = new StringBuilder();
        queryStrings.append("select message from Message as message");

        boolean criteria_present = false;
        if (fragment != null) {
            queryStrings.append(" where\n(message.body like :fragment)");
            criteria_present = true;
        }
        if (lowerCreationTimestamp != null) {
            if (criteria_present)
                queryStrings.append(" and\n");
            else
                queryStrings.append(" where\n");

            queryStrings.append("(message.creationTimestamp >= :lowerCreationTimestamp)");
            criteria_present = true;
        }
        if (upperCreationTimestamp != null) {
            if (criteria_present)
                queryStrings.append(" and\n");
            else
                queryStrings.append(" where\n");
            queryStrings.append("(message.creationTimestamp <= :upperCreationTimestamp)");
        }

        String queryString = queryStrings.toString();

        TypedQuery<Message> query = entityManager.createQuery(queryString, Message.class);

        if (resultOffset != null)
            query.setFirstResult(resultOffset);
        if (resultLimit != null)
            query.setMaxResults(resultLimit);

        if (fragment != null)
            query.setParameter("fragment", fragment);
        if (lowerCreationTimestamp != null)
            query.setParameter("lowerCreationTimestamp", lowerCreationTimestamp);
        if (upperCreationTimestamp != null)
            query.setParameter("upperCreationTimestamp", upperCreationTimestamp);

        entityManager.close();

        List<Message> messageList = query.getResultList();
        messageList.sort(Comparator.comparing(Message::getIdentity));

        return messageList;
    }

    /**
     * Returns the message matching the given identity.
     */
    @GET
    @Path("{id}")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Message queryPerson(@PathParam("id") @Positive final long messageIdentity) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        Message message = entityManager.find(Message.class, messageIdentity);

        if (message == null)
            throw new ClientErrorException(NOT_FOUND);

        entityManager.close();
        return message;
    }

    /**
     * Creates a new message, using an HTTP form as a body
     * parameter (content type "application/x-www-form-urlencoded") with the fields “body”
     * (message body) and “subjectReference” (message subject), and Header field
     * “Requester-Identity” (message author) – this header field will later be supplied during
     * authentication. Return the affected message's identity as text/plain.
     */
    @POST
    @Produces({TEXT_PLAIN})
    @Consumes({"application/x-www-form-urlencoded"})
    public String createMessage(@NotNull @FormParam("body") String body,
                                @NotNull @FormParam("subjectReference") String subjectReference,
                                @NotNull @HeaderParam("Requester-Identity") Long requesterIdentity) {

        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        Person author = entityManager
                .createQuery("select person from Person as person where (person.identity = :personIdentity", Person.class)
                .setParameter("personIdentity", requesterIdentity)
                .getSingleResult();
        if (author == null) {
            throw new ClientErrorException(BAD_REQUEST);
        }
        BaseEntity subject = entityManager.createQuery("TODO", BaseEntity.class).getSingleResult();
        if (subject == null) {
            throw new ClientErrorException(BAD_REQUEST);
        }

        Message message = new Message(author, subject);
        entityManager.getTransaction().begin();
        entityManager.persist(message);
        entityManager.flush();
        entityManager.getTransaction().commit();
        entityManager.close();

        return Long.toString(message.getIdentity());
    }

    /*
     * Sind dabei Filter-Queries gefordert, so definiert als Query-Parameter ein Suchkriterium
     * pro textuellem Entity-Feld, und zwei für numerische Felder (für >= und <= Vergleich).
     * Definiert zudem einen JP-QL Query nach folgendem Muster (statt „=“ kann für mehr
     * Flexibilität auch „like“ verwendet werden, ist aber deutlich teurer in der Ausführung):
     *
     * select x from X as x where
     * (:lowerNumber is null or x.number >= :lowerNumber) and
     * (:upperNumber is null or x.number <= :upperNumber) and
     * (:text is null or x.text = :text) ...
     *
     * Wird durch einen Service eine Relation modifiziert, dann sind nach Speicherung der
     * Änderungen (i.e. commit) zudem alle Entitäten aus dem 2nd-Level Cache zu entfernen
     * deren „mappedBy“-Relationsmengen sich dadurch ändern; diese Spiegel-Mengen werden
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
