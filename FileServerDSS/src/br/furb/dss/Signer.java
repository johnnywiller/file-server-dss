package br.furb.dss;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Signer {

	private static Signer instance;

	private Signer() throws NoSuchAlgorithmException {

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

	public byte[] sign(byte[] plainText) throws Exception {
		Signature privateSignature = Signature.getInstance("SHA256withRSA");
		privateSignature.initSign(getServerPrivateKey());
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

	private PrivateKey getServerPrivateKey() throws Exception {

		byte[] keyBytes = Files.readAllBytes(Paths.get("/home/ec2-user/private_key.der"));

		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);

	}

}
