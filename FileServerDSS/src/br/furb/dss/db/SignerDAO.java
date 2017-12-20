package br.furb.dss.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SignerDAO {
	
	public void computeRowFingerPrint(String user) throws SQLException {
		
		PreparedStatement st = Connection.getInstance().getConnection()
				.prepareStatement("select id, name, hash_pass, salt, file_salt, permissions,"
						+ " signature_row, signature_salt from users where name = ?");

		st.setString(1, user);

		ResultSet rs = st.executeQuery();

		if (rs.next()) {
			
			byte[] id = rs.getBytes(1);
			byte[] name = rs.getBytes(2);
			byte[] hash_pass = rs.getBytes(3);
			byte[] salt = rs.getBytes(4);
			byte[] file_salt = rs.getBytes(5);
			byte[] permissions = rs.getBytes(6);
			
			System.out.println(id.length);
			System.out.println(name.length);
			System.out.println(hash_pass.length);
			System.out.println(salt.length);
			System.out.println(file_salt.length);
			System.out.println(permissions.length);
		}
		
		System.out.println("nao achou");
	}
	
}
