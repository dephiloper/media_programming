package de.sb.messenger.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class SanityCheck {

	public static void main (String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("messenger");
		EntityManager em = emf.createEntityManager();
		Person e = em.find(Person.class, 2L);
		
		
		System.out.println(e);
	}

}
