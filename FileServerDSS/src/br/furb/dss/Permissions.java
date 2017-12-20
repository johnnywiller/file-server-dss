package br.furb.dss;

public class Permissions {
	
	public final static long READ_OTHERS_DIR = 0b1;
	public final static long REMOVE_OTHERS_DIR = 0b10;
	public final static long REMOVE_USER = 0b100;
	public final static long CHANGE_OTHER_PERMISSIONS = 0b1000;
	public final static long READ_OTHERS_PERM = 0b10000;
	public final static long LIST_USERS = 0b100000;
	
	public static String getRolesFriendly(long permissions) {
		
		String rolesFriendly = "Ler e escrever no seu diretorio";
		
		if ((permissions & READ_OTHERS_DIR) != 0)
			rolesFriendly += " | Ler diretorio dos outros";
		
		if ((permissions & REMOVE_OTHERS_DIR) != 0)
			rolesFriendly += " | Remover diretorio dos outros";
		
		if ((permissions & REMOVE_USER) != 0)
			rolesFriendly += " | Remover outros";
		
		if ((permissions & CHANGE_OTHER_PERMISSIONS) != 0)
			rolesFriendly += " | Modificar permissoes dos outros";
		
		if ((permissions & READ_OTHERS_PERM) != 0)
			rolesFriendly += " | Ler as permissoes dos outros";
		
		if ((permissions & LIST_USERS) != 0)
			rolesFriendly += " | Listar usuarios";
		
		return rolesFriendly;
	}
	
	public static String descAllPermissionsFriendly() {
		
		String perm = "Permissions:\n" +
					"READ_OTHERS_DIR            \t\t--list files of other users\n" +
					"REMOVE_OTHERS_DIR          \t\t--remove files of other users\n" +
					"REMOVE_USER                \t\t--remove other users\n" +
					"CHANGE_OTHER_PERMISSIONS   \t\t--change permissions of other users\n" +
					"READ_OTHERS_PERM           \t\t--list permissions of other users\n" +
					"LIST_USERS                 \t\t--list registered users in the system\n";
		
		return perm;
		
	}
	
	public static long getPermissionAsLong(String perm) {

		switch (perm.toUpperCase()) {
		case "READ_OTHERS_DIR":
			return READ_OTHERS_DIR;
		case "REMOVE_OTHERS_DIR":
			return REMOVE_OTHERS_DIR;
		case "REMOVE_USER":
			return REMOVE_USER;
		case "CHANGE_OTHER_PERMISSIONS":
			return CHANGE_OTHER_PERMISSIONS;
		case "READ_OTHERS_PERM":
			return READ_OTHERS_PERM;
		case "LIST_USERS":
			return LIST_USERS;
		}
		return 0;
	}

	public static boolean checkPermission(long permissions, long required) {
		return (required & permissions) != 0;
	}
}
