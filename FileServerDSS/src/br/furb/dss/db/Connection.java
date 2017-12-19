package br.furb.dss.db;

import java.sql.SQLException;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class Connection {

	private static Connection instance;
	private java.sql.Connection conn;

	private Connection() {

	}

	public static Connection getInstance() {

		if (instance == null)
			instance = new Connection();

		return instance;

	}

	public java.sql.Connection getConnection() throws SQLException {

		if (this.conn == null) {
			MysqlDataSource ds = new MysqlDataSource();

			ds.setUser("root");
			ds.setPassword("dssfurb");
			ds.setServerName("localhost");

			this.conn = ds.getConnection();
		}
		
		return this.conn;

	}
	
	

}
