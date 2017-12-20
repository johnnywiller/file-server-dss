package br.furb.dss;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileOperations {

	private byte[] symmetricKey;
	private String user;
	
	private final String baseDir = "/home/ec2-user/aula_dss_files/";
	
	public FileOperations(byte[] symmetricKey, String user) {
		this.symmetricKey = symmetricKey;
		this.user = user;
	}

	public List<String> lsDir(String user) {

		if (user == null) user = this.user;
		
		// try to protect against directory traversal
		if (user.contains("./") || user.contains("../")) return null;
		
		List<String> results = new ArrayList<String>();

		File[] files = new File(baseDir + user).listFiles();

		if (files == null)
			return null;

		for (File file : files) {
			if (file.isFile()) {
				results.add(file.getName());
			}
		}
		
		return results;
	}
	
	public boolean createDir(String user) {
		return new File(baseDir + user).mkdir();
	}
	
	public boolean createFile(String filename, String content) {
		
		
		return false;
	}
	

}
