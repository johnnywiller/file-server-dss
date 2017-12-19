package br.furb.dss;

import java.io.Serializable;

public class DHParam implements Serializable {
	
	byte[] signature;
	byte[] content;
	public byte[] getSignature() {
		return signature;
	}
	public void setSignature(byte[] signature) {
		this.signature = signature;
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	
	
	
	
}
