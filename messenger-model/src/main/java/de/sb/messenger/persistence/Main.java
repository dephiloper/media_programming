package de.sb.messenger.persistence;

import javax.persistence.EntityManagerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.mysql.jdbc.Driver;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException, SQLException{
		// mysql -uroot -p
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("messenger");
		EntityManager em = factory.createEntityManager();
		List<Person> persons = em.createQuery("SELECT p FROM Person p", Person.class).getResultList();

	}

}
