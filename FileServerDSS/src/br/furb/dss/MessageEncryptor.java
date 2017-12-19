package br.furb.dss;

import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MessageEncryptor {

	private Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	private Mac hasher = Mac.getInstance("HmacSHA256");
	private ClientKeys keys;

	public MessageEncryptor(ClientKeys keys) throws Exception {
		this.keys = keys;

	}

	public EncryptedMessage encryptedMessage(String msg) throws Exception {

		EncryptedMessage message = new EncryptedMessage();

		byte[] iv = new byte[16];

		SecureRandom random = new SecureRandom();
		random.nextBytes(iv);

		SecretKeySpec secretKeySpec = new SecretKeySpec(keys.getSymmetricKey(), "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

		byte[] cipherText = cipher.doFinal(msg.getBytes());

		byte[] hash = generateHMAC(iv, cipherText, keys.getMacKey());

		message.setContent(cipherText);
		message.setIv(iv);
		message.setMac(hash);

		return message;
	}

	public String decryptMessage(EncryptedMessage msg) throws Exception {

		SecretKeySpec secretKeySpec = new SecretKeySpec(keys.getSymmetricKey(), "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(msg.getIv());

		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

		byte[] plainText = cipher.doFinal(msg.getContent());

		// check packet integrity, throws an exception if not satisfied
		requireIntegrity(msg.getIv(), msg.getContent(), msg.getMac(), keys.getMacKey());

		return new String(plainText);

	}

	private byte[] generateHMAC(byte[] iv, byte[] cipherText, byte[] macKey) throws InvalidKeyException {

		byte[] packet = new byte[iv.length + cipherText.length];
		System.arraycopy(iv, 0, packet, 0, iv.length);
		System.arraycopy(cipherText, 0, packet, iv.length, cipherText.length);

		hasher.init(new SecretKeySpec(macKey, "HmacSHA256"));

		byte[] hash = hasher.doFinal(packet);

		return hash;
	}

	private void requireIntegrity(byte[] iv, byte[] cipherText, byte[] hash, byte[] macKey) throws Exception {

		byte[] wanted = generateHMAC(iv, cipherText, macKey);

		if (!Arrays.equals(wanted, hash)) {
			throw new Exception("Hash of packet didn't match... MitM attack?");
		}

	}

}
