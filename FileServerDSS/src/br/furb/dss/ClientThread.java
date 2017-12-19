package br.furb.dss;

import java.io.IOException;

public class ClientThread extends Thread {

	private SocketClient thisClient;
	private MessageEncryptor encryptor;

	private ClientKeys keys;

	public ClientThread(SocketClient client) throws Exception {
		this.thisClient = client;	
	}

	@Override
	public void run() {

		try {

			keys = ClientSessionInitiation.getInstance(thisClient).startSession();

			this.encryptor = new MessageEncryptor(keys);
			
			String welcome = "Seja bem vindo, por favor faca login para utilizar os servicos";

			EncryptedMessage encMsg = encryptor.encryptedMessage(welcome);

			thisClient.enviar(encMsg);

			while (true) {

				EncryptedMessage received = (EncryptedMessage) thisClient.getIn().readObject();

				String msg = encryptor.decryptMessage(received);
				
				System.out.println(msg);

			}

		} catch (Exception e1) {
			e1.printStackTrace();
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

	}

}
