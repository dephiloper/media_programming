package de.sb.messenger.persistence;

import javax.persistence.*;
import java.util.List;

/**
 * Application for sanity checking the messenger-model JPA setup.
 */
public class SanityCheck {

	private static final String QUERY_STRING = "SELECT p from Person as p WHERE "
			+ "(:surname is null or p.name.family = :surname) and "
			+ "(:forename is null or p.name.given = :forename) and "
			+ "(:email is null or p.email = :email) and "
			+ "(:street is null or p.address.street = :street) and "
			+ "(:city is null or p.address.city = :city) and "
			+ "(:postcode is null or p.address.postcode = :postcode) and "
			+ "(:group is null or p.group = :group)";

	public static void main (final String[] args) {
		final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("messenger");
		try {
			final EntityManager entityManager = entityManagerFactory.createEntityManager();
			try {
				// ord.quoteNumber = :quoteNumber or :quoteNumber is null or :quoteNumber = ''
				TypedQuery<Person> query = entityManager.createQuery(QUERY_STRING, Person.class);

				query.setParameter("surname", null);
				query.setParameter("forename", null);
				query.setParameter("email", null);
				query.setParameter("street", null);
				query.setParameter("postcode", null);
				query.setParameter("city", null);
				query.setParameter("group", null);

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
