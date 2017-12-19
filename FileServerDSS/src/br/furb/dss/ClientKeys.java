package br.furb.dss;

public class ClientKeys {
	
	private byte[] symmetricKey;
	private byte[] macKey;
	
	public ClientKeys(byte[] symmetricKey, byte[] macKey) {
		this.symmetricKey = symmetricKey;
		this.macKey = macKey;
	}
	
	public byte[] getSymmetricKey() {
		return symmetricKey;
	}
	public byte[] getMacKey() {
		return macKey;
	}
	
}
