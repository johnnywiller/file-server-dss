package br.furb.dss;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class FileOperations {

	private byte[] symmetricKey;
	private String user;

	private Cipher cipher;
	private Mac hasher;

	private final String baseDir = "/home/ec2-user/aula_dss_files/";

	public FileOperations(byte[] symmetricKey, String user)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		this.symmetricKey = symmetricKey;
		this.user = user;
		cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		hasher = Mac.getInstance("HmacSHA256");
		hasher.init(new SecretKeySpec(symmetricKey, "HmacSHA256"));
	}

	private void encryptToFile(File f, byte[] content) throws Exception {

		byte[] hash;
		byte[] iv;
		byte[] encryptedContent;

		iv = generateIV();

		byte[] toHash = new byte[iv.length + content.length];

		System.arraycopy(iv, 0, toHash, 0, iv.length);
		System.arraycopy(content, 0, toHash, iv.length, content.length);

		hash = hasher.doFinal(toHash);

		SecretKeySpec secretKeySpec = new SecretKeySpec(symmetricKey, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

		encryptedContent = cipher.doFinal(content);

		byte[] fullFileContent = new byte[hash.length + iv.length + encryptedContent.length];

		System.arraycopy(hash, 0, fullFileContent, 0, hash.length);
		System.arraycopy(iv, 0, fullFileContent, hash.length, iv.length);
		System.arraycopy(encryptedContent, 0, fullFileContent, hash.length + iv.length, encryptedContent.length);

		try (FileOutputStream fos = new FileOutputStream(f)) {
			fos.write(fullFileContent);
			fos.close();
		}

	}

	private byte[] generateIV() {
		byte[] iv = new byte[16];
		new SecureRandom().nextBytes(iv);
		return iv;
	}

	private byte[] decryptFile(File f) throws Exception {

		byte[] allBytes = Files.readAllBytes(f.toPath());

		byte[] hash;
		byte[] iv;
		byte[] content;
		byte[] decryptedContent;

		try {

			hash = Arrays.copyOf(allBytes, 32);
			iv = Arrays.copyOfRange(allBytes, hash.length, hash.length + 16);
			content = Arrays.copyOfRange(allBytes, hash.length + iv.length, allBytes.length);

			SecretKeySpec secretKeySpec = new SecretKeySpec(symmetricKey, "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

			decryptedContent = cipher.doFinal(content);

		} catch (Exception e) {
			//throw new Exception("Nao foi possivel descriptografar o arquivo."
			//		+ "\nProvavelmente o arquivo foi alterado ou esta corrompido!" );
			throw e;
		}

		if (!checkFileIntegrity(hash, iv, decryptedContent)) {
			throw new Exception("Nao foi possivel verificar a integridade ou autenticidade do arquivo."
					+ "\nProvavelmente o arquivo foi alterado ou esta corrompido!");
		}

		return decryptedContent;

	}

	private boolean checkFileIntegrity(byte[] hash, byte[] iv, byte[] content) {

		byte[] oldHash = hash;
		byte[] newHash;
		byte[] fileContent = new byte[content.length + iv.length];

		System.arraycopy(iv, 0, fileContent, 0, iv.length);
		System.arraycopy(content, 0, fileContent, iv.length, content.length);

		newHash = hasher.doFinal(fileContent);

		return Arrays.equals(oldHash, newHash);
	}

	public List<String> lsDir(String user) {

		if (user == null)
			user = this.user;

		// try to protect against directory traversal
		if (user.contains("./") || user.contains("../"))
			return null;

		List<String> results = new ArrayList<String>();

		File[] files = new File(baseDir + user).listFiles();

		if (files == null)
			return null;

		for (File file : files) {
			if (file.isFile()) {
				results.add(file.getName());
			}
		}

		return results;
	}

	public boolean createDir(String user) {
		return new File(baseDir + user).mkdir();
	}

	public String readFile(String filename) throws Exception {

		// try to protect against directory traversal
		if (user.contains("./") || user.contains("../"))
			return "";

		File f = new File(baseDir + user + "/" + filename);

		byte[] fileContent;

		if (f.exists()) {
			fileContent = decryptFile(f);
			return new String(fileContent);
		}

		return "";
	}

	public boolean createOrUpdateFile(String filename, String content) throws Exception {

		// try to protect against directory traversal
		if (user.contains("./") || user.contains("../"))
			return false;

		File f = new File(baseDir + user + "/" + filename);

		byte[] byteContent = content.getBytes();
		byte[] fileContent;
		byte[] toEncrypt;

		if (f.exists()) {
			fileContent = decryptFile(f);
			toEncrypt = new byte[byteContent.length + fileContent.length];
			System.arraycopy(byteContent, 0, toEncrypt, 0, byteContent.length);
			System.arraycopy(fileContent, 0, toEncrypt, byteContent.length, fileContent.length);
		} else {
			if (!f.createNewFile())
				return false;

			toEncrypt = byteContent;
		}

		encryptToFile(f, toEncrypt);

		return true;
	}

}
