package br.furb.dss.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RolesDAO {

	public long getPermissions(Integer id) throws SQLException {

		Statement st = Connection.getInstance().getConnection().createStatement();

		ResultSet rs = st.executeQuery("Select permissions from user_permissions where id =" + id);

		long permissions = rs.getLong(0);
		
		rs.close();
		st.close();
		
		return permissions;
	}

}
