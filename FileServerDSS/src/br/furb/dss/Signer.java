package br.furb.dss;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

public class Signer {

	private static Signer instance;
	private KeyPair pair;
	// private byte[]

	private Signer() throws NoSuchAlgorithmException {

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);
		this.pair = keyGen.genKeyPair();

	}

	public static Signer getInstance() {

		if (instance == null)
			try {
				instance = new Signer();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}

		return instance;
	}

	public byte[] getPublicKey() {
		return pair.getPublic().getEncoded();
	}

	public byte[] sign(byte[] plainText) throws Exception {
		Signature privateSignature = Signature.getInstance("SHA256withRSA");
		privateSignature.initSign(pair.getPrivate());
		privateSignature.update(plainText);

		byte[] signature = privateSignature.sign();

		return signature;
	}

	public boolean verify(byte[] plainText, byte[] signature, byte[] pubKey) throws Exception {
		PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKey));
		Signature publicSignature = Signature.getInstance("SHA256withRSA");
		publicSignature.initVerify(publicKey);
		publicSignature.update(plainText);

		return publicSignature.verify(signature);
	}

}
