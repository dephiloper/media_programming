package de.sb.messenger.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;


/**
 * Application for sanity checking the messenger-model JPA setup.
 */
public class SanityCheck {

	public static void main (final String[] args) {
		final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("messenger");
		try {
			final EntityManager entityManager = entityManagerFactory.createEntityManager();
			try {
				final Query query = entityManager.createQuery("select p from Person as p");
				System.out.println(query.getResultList());
			} finally {
				entityManager.close();
			}
		} finally {
			entityManagerFactory.close();
		}
	}
}
