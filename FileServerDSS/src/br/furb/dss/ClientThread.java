package br.furb.dss;

import java.io.IOException;
import java.sql.SQLException;

import br.furb.dss.db.RolesDAO;
import br.furb.dss.db.UserDAO;

public class ClientThread extends Thread {

	private SocketClient thisClient;
	private MessageEncryptor encryptor;

	private ClientKeys keys;

	private UserDAO userDAO;
	private RolesDAO rolesDAO;

	private boolean logged = false;

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

			String welcome = "Seja bem vindo, por favor faca login ou registre-se para utilizar os servicos";

			EncryptedMessage encMsg = encryptor.encryptedMessage(welcome);

			thisClient.enviar(encMsg);

			while (true) {

				EncryptedMessage received = (EncryptedMessage) thisClient.getIn().readObject();
				String msg;
				System.out.println("leu");
				try {

					msg = encryptor.decryptMessage(received);

					System.out.println("Message: " + msg);

					parsePacket(msg);

				} catch (Exception e) {

					e.printStackTrace();

					encMsg = encryptor.encryptedMessage("Erro: " + e.getMessage());

					System.out.println("Erro: " + e.getMessage());

					thisClient.enviar(encMsg);

				}
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

	private void parsePacket(String received) throws Exception {

		if (received == null || received.isEmpty())
			return;

		String[] tokenized = received.split(" ");

		switch (tokenized[0]) {

		case "/login":

			if (tokenized.length < 3) {

				String msg = "Sintaxe invalida, digite /login <user> <pass>";

				EncryptedMessage encMsg = encryptor.encryptedMessage(msg);

				thisClient.enviar(encMsg);

				break;
			}

			doLogin(tokenized[1], tokenized[2]);
			break;

		case "/adduser":

			if (tokenized.length < 3) {

				String msg = "Sintaxe invalida, digite /adduser <user> <pass>";

				EncryptedMessage encMsg = encryptor.encryptedMessage(msg);

				thisClient.enviar(encMsg);

				break;

			}

			addUser(tokenized[1], tokenized[2]);
			break;

		}

	}

	private void doLogin(String user, String pass) throws Exception {

		boolean logged = userDAO.login(user, pass);

		if (!logged) {

			String msg = "Usuario ou senha invalidos, caso deseja cadastrar um novo usuario digite /adduser <user> <pass>";

			EncryptedMessage encMsg = encryptor.encryptedMessage(msg);

			thisClient.enviar(encMsg);

			return;

		} else {

			String msg = "Voce esta autenticado!\nSuas permissoes sao:\n" + getPermissionsAsString(user);

			EncryptedMessage encMsg = encryptor.encryptedMessage(msg);

			thisClient.enviar(encMsg);

			logged = true;
		}

	}

	private void addUser(String user, String pass) throws Exception {

		try {

			userDAO.addUser(user, pass);

			String msg = "Usuario cadastrado com sucesso! faca login para utilizar os servicos";

			EncryptedMessage encMsg = encryptor.encryptedMessage(msg);

			thisClient.enviar(encMsg);

		} catch (Exception e) {

			String msg = "Erro ao cadastrar: " + e.getMessage();

			EncryptedMessage encMsg = encryptor.encryptedMessage(msg);

			thisClient.enviar(encMsg);

		}

	}

	private String getPermissionsAsString(String user) throws SQLException {
		long permissions = rolesDAO.getPermissions(user);
		return Permissions.getRolesFriendly(permissions);
	}

}
