package de.sb.messenger.rest;

import de.sb.messenger.persistence.Message;
import de.sb.toolbox.Copyright;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.validation.constraints.Positive;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NOT_IMPLEMENTED;

@Path("messages")
@Copyright(year = 2018, holders = "Gruppe Drei (Gruppe 5)")
public class MessageService {
    private static final EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory("messenger");

    /**
     * TODO: GET /messages
     * Returns the messages matching the given criteria, with missing
     * parameters identifying omitted criteria, sorted by identity. Search criteria should be a
     * message body fragment (compare using the like operator), an upper/lower creation
     * timestamp, plus resultOffset and resultLimit which define a result range.
     */
    @GET
    @Produces({ APPLICATION_JSON, APPLICATION_XML })
    public Message[] queryMessages () {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();

        throw new ClientErrorException(NOT_IMPLEMENTED);
    }

    /**
     * TODO: GET /messages/{id}
     * Returns the message matching the given identity.
     */
    @GET
    @Path("{id}")
    @Produces({ APPLICATION_JSON, APPLICATION_XML })
    public Message queryPerson (@PathParam("id") @Positive final long messageIdentity) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        Message message = entityManager.find(Message.class, messageIdentity);

        if (message == null)
            throw new ClientErrorException(NOT_FOUND);

        entityManager.close();
        return message;
    }

    /**
     * TODO: POST /messages
     * Creates a new message, using an HTTP form as a body
     * parameter (content type "application/x-www-form-urlencoded") with the fields “body”
     * (message body) and “subjectReference” (message subject), and Header field
     * “Requester-Identity” (message author) – this header field will later be supplied during
     * authentication. Return the affected message's identity as text/plain.
     */
    @POST
    @Path("{id}")
    @Produces({TEXT_PLAIN})
    public String createMessage (@PathParam("id") @Positive final long messageIdentity) {
        final EntityManager entityManager = entityManagerFactory.createEntityManager();

        throw new ClientErrorException(NOT_IMPLEMENTED);
    }

    /*
     * TODO: Appendix
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
