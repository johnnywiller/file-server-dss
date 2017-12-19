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

}
