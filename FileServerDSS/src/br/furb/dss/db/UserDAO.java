package br.furb.dss.db;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class UserDAO {

	private final int HASH_ROUNDS = 70_000;
	private static final int ITERATION_COUNT = 65536;
	private static final int KEY_LENGTH = 256;
	
	MessageDigest sha256;

	public UserDAO() throws NoSuchAlgorithmException {
		this.sha256 = MessageDigest.getInstance("SHA-256");
	}

	private boolean checkPasswordStrength(String pass) {
		return pass.matches("^(?=.*[A-Z].*[A-Z])(?=.*[!@#$&*])(?=.*[0-9].*[0-9])(?=.*[a-z].*[a-z].*[a-z]).{8}$");
	}

	public void addUser(String user, String pass) throws Exception {

		if (user == null || pass == null || user.trim().isEmpty() || pass.trim().isEmpty())
			throw new Exception("Usuario e/ou senha nao podem estar em braco");

		if (!checkPasswordStrength(pass)) {
			throw new Exception("Senha nao atende aos requisitos minimos:\n" + "2 letra maiusculas\n"
					+ "1 caractere especial\n" + "2 numeros\n" + "3 letras minusculas\n" + "Comprimento 8");
		}

		// verify if exists a hash with this user, if yes user can't be added twice
		byte[] hash = getHash(user);

		if (hash != null)
			throw new Exception("Usuario ja existente, por favor digite outro");

		byte[] salt = generateSalt();
		byte[] hashed = getHashedPass(pass, salt);
		byte[] fileSalt = generateSalt();
		long permissions = 0;

		String signature = "dummy signature";

		String baseSalt = Base64.getEncoder().encodeToString(salt);
		String baseHashed = Base64.getEncoder().encodeToString(hashed);
		String baseFileSalt = Base64.getEncoder().encodeToString(fileSalt);

		PreparedStatement st = Connection.getInstance().getConnection()
				.prepareStatement("insert into users "
						+ "(name,hash_pass,salt,file_salt,permissions,signature_row)"
						+ " values (?,?,?,?,?,?)");

		st.setString(1, user);
		st.setString(2, baseHashed);
		st.setString(3, baseSalt);
		st.setString(4, baseFileSalt);
		st.setLong(5, permissions);
		st.setString(6, signature);
		
		st.executeUpdate();
		
	}

	public byte[] getFileOpKeys(String user, String pass) throws Exception {
		
		byte[] fileSalt = getFileSalt(user);
		
		byte[] fileOpKey = getPBDKFkey(pass, fileSalt);
		
		return fileOpKey;
	}
	
	private byte[] generateSalt() {

		byte[] salt = new byte[32];

		new SecureRandom().nextBytes(salt);

		return salt;
	}

	private byte[] getHashedPass(String pass, byte[] salt) {

		byte[] hashedPass = sha256.digest(pass.getBytes());
		byte[] concatHashes = new byte[64];

		for (int i = 0; i < HASH_ROUNDS; i++) {
			System.arraycopy(hashedPass, 0, concatHashes, 0, hashedPass.length);
			System.arraycopy(salt, 0, concatHashes, hashedPass.length, salt.length);

			hashedPass = sha256.digest(concatHashes);
		}
		return hashedPass;
	}

	private byte[] getPBDKFkey(String pass, byte[] fileSalt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(pass.toCharArray(), fileSalt, ITERATION_COUNT, KEY_LENGTH);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
		
		return secret.getEncoded();
	}
	
	public boolean login(String user, String pass) throws NoSuchAlgorithmException {

		try {

			if (user == null || pass == null || user.trim().isEmpty() || pass.trim().isEmpty())
				return false;

			byte[] salt = getSalt(user);

			if (salt == null)
				return false;

			byte[] hash = getHash(user);

			if (hash == null)
				return false;

			return Arrays.equals(hash, getHashedPass(pass, salt));

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}

	private byte[] getFileSalt(String user) throws SQLException {

		PreparedStatement st = Connection.getInstance().getConnection()
				.prepareStatement("Select FILE_SALT from users where name = ?");

		st.setString(1, user);

		ResultSet rs = st.executeQuery();

		if (!rs.next())
			return null;

		String salt = rs.getString(1);

		if (salt == null)
			return null;

		byte[] saltDecoded = Base64.getDecoder().decode(salt.getBytes());

		return saltDecoded;

	}

	
	private byte[] getSalt(String user) throws SQLException {

		PreparedStatement st = Connection.getInstance().getConnection()
				.prepareStatement("Select SALT from users where name = ?");

		st.setString(1, user);

		ResultSet rs = st.executeQuery();

		if (!rs.next())
			return null;

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

		if (!rs.next())
			return null;

		String hash = rs.getString(1);

		if (hash == null)
			return null;

		byte[] hashDecoded = Base64.getDecoder().decode(hash.getBytes());

		return hashDecoded;

	}

}
