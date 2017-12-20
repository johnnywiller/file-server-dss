package br.furb.dss.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RolesDAO {

	public long getPermissions(String user) throws SQLException {

		PreparedStatement st = Connection.getInstance().getConnection()
				.prepareStatement("select permissions from users where name = ?");

		st.setString(1, user);

		ResultSet rs = st.executeQuery();

		if (!rs.next())
			return 0;

		long permissions = rs.getLong(1);

		return permissions;
	}
	
	
	public void addUserPerm(String user, long perm) throws Exception {
		
		long actualPerm = getPermissions(user);
		
		long newPerm = actualPerm | perm;
		
		PreparedStatement st = Connection.getInstance().getConnection()
				.prepareStatement("update users set permissions = ? where user = ?");

		st.setLong(1, newPerm);
		st.setString(2, user);

		st.executeUpdate();
		
		SignerDAO.getInstance().updateRowSignature(user);
		
	}
	
	public void rmUserPerm(String user, long perm) throws Exception {
		
		long actualPerm = getPermissions(user);
		
		// subtract a mask permission 
		long newPerm = actualPerm ^ (actualPerm & perm);
		
		PreparedStatement st = Connection.getInstance().getConnection()
				.prepareStatement("update users set permissions = ?");

		st.setLong(1, newPerm);

		st.executeUpdate();
		
		SignerDAO.getInstance().updateRowSignature(user);
		
	}
}
