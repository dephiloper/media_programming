package de.sb.messenger.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // mysql -uroot -p
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("messenger");
        EntityManager em = factory.createEntityManager();
        List<Person> persons = em.createQuery("SELECT p FROM Person p", Person.class).getResultList();
    }

}
