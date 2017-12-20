package br.furb.dss;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import br.furb.dss.db.RolesDAO;
import br.furb.dss.db.UserDAO;

public class ClientThread extends Thread {

	private SocketClient thisClient;
	private MessageEncryptor encryptor;
	private FileOperations fileOp;
	private ClientKeys keys;

	private UserDAO userDAO;
	private RolesDAO rolesDAO;

	private boolean logged = false;
	private String activeUser = "";

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

			send("Seja bem vindo, por favor faca login ou registre-se para utilizar os servicos\n");
			send("Os comandos sao:\n" + "/adduser <user> <pass>\n" + "/login <user> <pass>\n"
					+ "/write <filename> <content>\n" + "/read <filename>\n" + "/lsfiles < - | user>\n" + "/lsusers\n"
					+ "/removeuser\n" + "/lsperm < - | user>\n" + "/help\n" + "/quit");

			while (true) {

				EncryptedMessage received = (EncryptedMessage) thisClient.getIn().readObject();
				String msg;

				try {

					msg = encryptor.decryptMessage(received);

					parsePacket(msg);

				} catch (Exception e) {

					e.printStackTrace();

					send("Erro: " + e.getMessage());

					System.out.println("Erro: " + e.getMessage());

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

		String[] tokenized = received.trim().split(" ");

		String msg;
		EncryptedMessage encMsg;

		switch (tokenized[0]) {

		case "/login":

			if (tokenized.length < 3) {
				send("Sintaxe invalida, digite /login <user> <pass>");
				break;
			}
			doLogin(tokenized[1], tokenized[2]);
			break;

		case "/adduser":

			if (tokenized.length < 3) {
				send("Sintaxe invalida, digite /adduser <user> <pass>");
				break;
			}

			addUser(tokenized[1], tokenized[2]);
			break;

		case "/write":
			if (requireLogin()) {

				if (tokenized.length < 3) {
					send("Sintaxe invalida, digite /write <filename> <content>");
					break;
				}

				boolean result = fileOp.createOrUpdateFile(tokenized[1], tokenized[2]);

				if (result) {
					send("Arquivo gravado com sucesso!");
				} else {
					send("Algum erro desconhecido ocorreu , contate o suporte.");
				}
			}
			break;

		case "/read":
			if (requireLogin()) {

				if (tokenized.length < 2) {
					send("Sintaxe invalida, digite /read <filename>");
					break;
				}

				String content = fileOp.readFile(tokenized[1]);

				send("O conteudo do arquivo eh:\n");
				send(content);

			}
			break;

		case "/lsfiles":
			if (requireLogin()) {

				// check if this user is trying to view other user dir without permission
				if (tokenized.length > 1 && !tokenized[1].equalsIgnoreCase(this.activeUser)
						&& !requiredPermission(Permissions.READ_OTHERS_DIR))
					break;

				List<String> files = this.fileOp.lsDir(tokenized.length < 2 ? null : tokenized[1]);

				if (files == null || files.isEmpty())
					msg = "Nenhum arquivo encontrado";
				else
					msg = "Os arquivos sao:\n" + String.join("\n", files);

				send(msg);
			}
			break;
		case "/help":
			send("Os comandos sao:\n" + "/adduser <user> <pass>\n" + "/login <user> <pass>\n"
					+ "/write <filename> <content>\n" + "/read <filename>\n" + "/lsfiles <user>\n" + "/lsusers\n"
					+ "/removeuser\n" + "/lsperm <user>\n" + "/help\n" + "/quit");

			break;

		case "/quit":
			if (requireLogin()) {
				this.activeUser = "";
				this.logged = false;
				send("Goodbye...");
			}
			break;
		default:
			send("Comando invalido, digite /help para ver os comandos possiveis");
			break;
		}
	}

	private void send(String msg) throws Exception {
		EncryptedMessage encMsg = encryptor.encryptedMessage(msg);
		thisClient.enviar(encMsg);
	}

	private boolean requiredPermission(long required) throws SQLException, IOException, Exception {

		if (!Permissions.checkPermission(rolesDAO.getPermissions(this.activeUser), required)) {
			send("Voce nao tem permissao para este tipo de acesso!");
			return false;
		}
		return true;
	}

	private boolean requireLogin() throws Exception {

		if (!logged) {
			send("Esta operacao exige que voce esteja logado, por favor faca login primeiro");
			return false;
		}
		return true;
	}

	private void doLogin(String user, String pass) throws Exception {

		boolean logged = userDAO.login(user, pass);
		if (!logged) {
			send("Usuario ou senha invalidos, caso deseja cadastrar um novo usuario digite /adduser <user> <pass>");
			return;
		} else {

			send("Voce esta autenticado!\nSua chave criptografica acaba de ser gerada em runtime\n\nSuas permissoes sao:\n"
					+ getPermissionsAsString(user));

			this.logged = true;
			this.activeUser = user;
			// create the file operations handler
			loadFileKeys(pass);

			// tries to create user dir and send a message, if dir is already created from
			// previous login, then no message is sended
			createInitialDir();
		}
	}

	private void loadFileKeys(String pass) throws Exception {
		byte[] key = this.userDAO.getFileOpKeys(activeUser, pass);
		this.fileOp = new FileOperations(key, activeUser);
	}

	private void addUser(String user, String pass) throws Exception {
		try {
			userDAO.addUser(user, pass);
			send("Usuario cadastrado com sucesso! faca login para utilizar os servicos");
		} catch (Exception e) {
			send("Erro ao cadastrar: " + e.getMessage());
		}
	}

	private void createInitialDir() throws Exception {
		if (fileOp.createDir(activeUser)) {
			send("Seu espaco de armazenamento em nuvem foi criado com sucesso!");
		}
	}

	private String getPermissionsAsString(String user) throws SQLException {
		long permissions = rolesDAO.getPermissions(user);
		return Permissions.getRolesFriendly(permissions);
	}
}
