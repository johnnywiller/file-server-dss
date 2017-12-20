package br.furb.dss;

import java.io.IOException;
import java.sql.SQLException;

import br.furb.dss.db.SignerDAO;

public class Main {

	public static void main(String[] args) throws IOException {
		
		SignerDAO signer = new SignerDAO();
		
		try {
			signer.computeRowFingerPrint("teste");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		System.out.println("[STARTED SERVER]");
//
//		ListeningSocket listen = new ListeningSocket();
//		
//		listen.start();
		
	}

}
