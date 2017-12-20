package br.furb.dss;

public class Permissions {
	
	public final static long READ_OTHERS_DIR = 0b1;
	public final static long REMOVE_OTHERS_DIR = 0b10;
	public final static long REMOVE_USER = 0b100;
	public final static long CHANGE_OTHER_PERMISSIONS = 0b1000;
	
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
		
		return rolesFriendly;
	}
	
	public static boolean checkPermission(long permissions, long required) {
		return (required & permissions) != 0;
	}
}
