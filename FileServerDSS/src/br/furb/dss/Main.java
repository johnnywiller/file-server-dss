package br.furb.dss;

import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {

		System.out.println("[STARTED SERVER]");

		ListeningSocket listen = new ListeningSocket();

		listen.start();

	}

}
