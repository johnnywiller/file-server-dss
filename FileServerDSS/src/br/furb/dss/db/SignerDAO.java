package br.furb.dss.db;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Base64;

import br.furb.dss.Signer;

public class SignerDAO {

	private static SignerDAO instance;
	
	MessageDigest digest;
	
	private SignerDAO() throws NoSuchAlgorithmException {
		this.digest = MessageDigest.getInstance("SHA-256");
	}

	public static SignerDAO getInstance() throws NoSuchAlgorithmException {

		if (instance == null) 
			instance = new SignerDAO();

		return instance;
	}

	public byte[] computeRowFingerPrint(String user) throws Exception {

		PreparedStatement st = Connection.getInstance().getConnection()
				.prepareStatement("select id, name, hash_pass, salt, file_salt, permissions,"
						+ " signature_salt from users where name = ?");

		st.setString(1, user);

		ResultSet rs = st.executeQuery();

		if (rs.next()) {

			byte[] id = rs.getBytes(1);
			byte[] name = rs.getBytes(2);
			byte[] hash_pass = Base64.getDecoder().decode(rs.getBytes(3));
			byte[] salt = Base64.getDecoder().decode(rs.getBytes(4));
			byte[] file_salt = Base64.getDecoder().decode(rs.getBytes(5));
			byte[] permissions = rs.getBytes(6);
			byte[] signature_salt = Base64.getDecoder().decode(rs.getBytes(7));

			byte[] concated = concat(id, name);
			concated = concat(concated, hash_pass);
			concated = concat(concated, salt);
			concated = concat(concated, file_salt);
			concated = concat(concated, permissions);
			concated = concat(concated, signature_salt);

			byte[] signed = Signer.getInstance().sign(digest.digest(concated));

			return digest.digest(signed);
		}

		return null;
	}

	public boolean checkRowIntegrity(String user) throws Exception {

		byte[] oldSignature;
		byte[] newSignature;

		PreparedStatement st = Connection.getInstance().getConnection()
				.prepareStatement("select signature_row from users where name = ?");

		st.setString(1, user);

		ResultSet rs = st.executeQuery();

		if (rs.next()) {
			oldSignature = Base64.getDecoder().decode(rs.getString(1));

			newSignature = computeRowFingerPrint(user);

			return Arrays.equals(oldSignature, newSignature);
		}

		return false;
	}

	public void updateRowSignature(String user) throws Exception {

		byte[] signature = computeRowFingerPrint(user);

		String baseSignature = Base64.getEncoder().encodeToString(signature);

		PreparedStatement st = Connection.getInstance().getConnection()
				.prepareStatement("update users set signature_row = ? where name = ?");

		st.setString(1, baseSignature);
		st.setString(2, user);

		st.executeUpdate();

	}

	private byte[] concat(byte[] a, byte[] b) {
		int aLen = a.length;
		int bLen = b.length;
		byte[] c = new byte[aLen + bLen];
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);
		return c;
	}
}
