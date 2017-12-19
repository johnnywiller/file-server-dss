package br.furb.dss;

import java.io.Serializable;

public class EncryptedMessage implements Serializable {

	private byte[] content;
	private byte[] iv;
	private byte[] mac;
	
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	public byte[] getIv() {
		return iv;
	}
	public void setIv(byte[] iv) {
		this.iv = iv;
	}
	public byte[] getMac() {
		return mac;
	}
	public void setMac(byte[] mac) {
		this.mac = mac;
	}
	
}
