package de.sb.messenger.persistence;

import javax.persistence.EntityManagerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import com.mysql.jdbc.Driver;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException, SQLException{
		// mysql -uroot -p
		
		final MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setURL("jdbc:mysql://localhost:3306/messenger");
		dataSource.setCharacterEncoding("utf-8");
		dataSource.setUser("root");
		dataSource.setPassword("root");
		Connection con = dataSource.getConnection();
		ResultSet set = con.createStatement().executeQuery("Select * from BaseEntity");
		while(set.next()) {
			System.out.println(set.getString("identity"));
		}
	}

}
