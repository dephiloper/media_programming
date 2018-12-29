package de.sb.messenger.persistence;

import javax.persistence.*;
import java.util.List;

/**
 * Application for sanity checking the messenger-model JPA setup.
 */
public class SanityCheck {

	public static void main (final String[] args) {
		final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("messenger");
		try {
			final EntityManager entityManager = entityManagerFactory.createEntityManager();
			try {
				// ord.quoteNumber = :quoteNumber or :quoteNumber is null or :quoteNumber = ''
				TypedQuery<Person> query = entityManager.createQuery("select p from Person p", Person.class);

				List<Person> peopleList = query.getResultList();
				System.out.println(query.getResultList());
			} finally {
				entityManager.close();
			}
		} finally {
			entityManagerFactory.close();
		}
	}
}
