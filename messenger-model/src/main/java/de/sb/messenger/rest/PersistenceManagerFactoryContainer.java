package de.sb.messenger.rest;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public interface PersistenceManagerFactoryContainer {
	 static final EntityManagerFactory entityManagerFactory =
	            Persistence.createEntityManagerFactory("messenger");
}
