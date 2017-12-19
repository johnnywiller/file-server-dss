package br.furb.dss;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import br.furb.dss.db.RolesDAO;
import br.furb.dss.db.UserDAO;

public class ClientThread extends Thread {

	private SocketClient thisClient;
	private MessageEncryptor encryptor;

	private ClientKeys keys;

	private UserDAO userDAO;
	private RolesDAO rolesDAO;

	public ClientThread(SocketClient client) throws Exception {
		this.thisClient = client;
	}

	@Override
	public void run() {

		try {

			keys = ClientSessionInitiation.getInstance(thisClient).startSession();

			this.encryptor = new MessageEncryptor(keys);

			this.userDAO = new UserDAO();
			this.rolesDAO = new RolesDAO();

			String welcome = "Seja bem vindo, por favor faca login para utilizar os servicos";

			EncryptedMessage encMsg = encryptor.encryptedMessage(welcome);

			thisClient.enviar(encMsg);

			while (true) {

				EncryptedMessage received = (EncryptedMessage) thisClient.getIn().readObject();

				String msg = encryptor.decryptMessage(received);

				System.out.println(msg);

			}

		} catch (Exception e1) {
			// e1.printStackTrace();
		}

		try {
			System.out.println("Connection " + thisClient.getSocket().getInetAddress().getHostAddress() + ":"
					+ thisClient.getSocket().getPort() + " has leaved the room");
			if (!thisClient.getSocket().isClosed()) {
				thisClient.getSocket().close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void parsePacket(String received) throws IOException, ClassNotFoundException, InterruptedException {

		if (received == null || received.isEmpty())
			return;

		String[] tokenized = received.split(" ");

		switch (tokenized[0]) {

		case "/login":

		}

	}

	private void doLogin(String user, String pass) throws Exception {

		boolean logged = userDAO.login(user, pass);

		if (!logged) {

			String msg = "Usuario ou senha invalidos, caso deseja cadastrar um novo usuario digite /adduser <user> <pass>";

			EncryptedMessage encMsg = encryptor.encryptedMessage(msg);

			thisClient.enviar(encMsg);

			return;
		}

	}

}
