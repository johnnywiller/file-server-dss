package br.furb.dss.db;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;

public class UserDAO {

	private final int HASH_ROUNDS = 70_000;

	MessageDigest sha256;

	public UserDAO() throws NoSuchAlgorithmException {
		this.sha256 = MessageDigest.getInstance("SHA-256");
	}

	public boolean login(String user, String pass) throws NoSuchAlgorithmException {

		try {

			byte[] salt = getSalt(user);

			if (salt == null)
				return false;

			byte[] hash = getHash(user);

			if (hash == null)
				return false;

			byte[] hashedPass = sha256.digest(pass.getBytes());

			byte[] concatHashes = new byte[64];

			for (int i = 0; i < HASH_ROUNDS; i++) {

				System.arraycopy(hashedPass, 0, concatHashes, 0, hashedPass.length);
				System.arraycopy(salt, 0, concatHashes, hashedPass.length, salt.length);

				hashedPass = sha256.digest(hashedPass);

			}

			return Arrays.equals(hash, hashedPass);

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}

	private byte[] getSalt(String user) throws SQLException {

		PreparedStatement st = Connection.getInstance().getConnection()
				.prepareStatement("Select SALT from users where name = ?");

		st.setString(1, user);

		ResultSet rs = st.executeQuery();

		String salt = rs.getString(1);

		if (salt == null)
			return null;

		byte[] saltDecoded = Base64.getDecoder().decode(salt.getBytes());

		return saltDecoded;

	}

	private byte[] getHash(String user) throws SQLException {

		PreparedStatement st = Connection.getInstance().getConnection()
				.prepareStatement("Select HASH_PASS from users where name = ?");

		st.setString(1, user);

		ResultSet rs = st.executeQuery();

		String hash = rs.getString(1);

		if (hash == null)
			return null;

		byte[] hashDecoded = Base64.getDecoder().decode(hash.getBytes());

		return hashDecoded;

	}

}
