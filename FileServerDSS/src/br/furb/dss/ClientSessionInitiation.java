package br.furb.dss;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;

public class ClientSessionInitiation {

	private DiffieHellmanUitls dh = new DiffieHellmanUitls();
	private SocketClient client;
	private static ClientSessionInitiation instance;

	public static ClientSessionInitiation getInstance(SocketClient client) {

		if (instance == null)
			instance = new ClientSessionInitiation(client);

		return instance;

	}

	private ClientSessionInitiation(SocketClient client) {
		this.client = client;
	}

	public ClientKeys startSession() throws Exception {

		System.out.println("STARTING SESSION (Kex process)");

		// request public key from user that we want to talk
		byte[] pubKey = getServerPrivateKey();

		DHPublicKey publicKey;
		KeyPair keyPair;
		byte[] secret;

		// --------- DH Kex PROCESS ---------

		// compute DH keys ('a' and A = g^a mod p)
		keyPair = dh.generateKeyPair();

		// pass A (A = g^a mod p) to the server
		dh.passPublicToServer((DHPublicKey) keyPair.getPublic(), client.getOut());

		// get B (g^b mod p) from the server
		publicKey = dh.getServerPublic(client.getIn(), pubKey);

		System.out.println("DH Public Key RECEIVED");

		// compute secret (s = B^a mod p)
		secret = dh.computeDHSecretKey((DHPrivateKey) keyPair.getPrivate(), publicKey);

		System.out.println("GENERATED SECRET FROM DH Kex");

		// use SHA2 to derive key
		secret = MessageDigest.getInstance("SHA-256").digest(secret);

		// 128 bits for symmetric key encryption and 128 bits for message authentication
		byte[] symmetricKey = Arrays.copyOf(secret, 16);
		byte[] macKey = Arrays.copyOfRange(secret, 16, 32);

		System.out.println("GENERATED SYMMETRIC AND H-MAC KEY");

		ClientKeys keys = new ClientKeys(symmetricKey, macKey);

		return keys;
	}

	private byte[] getServerPrivateKey() {

		return null;
	}

}
