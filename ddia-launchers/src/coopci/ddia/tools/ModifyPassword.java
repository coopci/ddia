package coopci.ddia.tools;

public class ModifyPassword {

	public static void main(String[] argv) throws Exception {
		String nickname = "admin";
		String newPassword = "123qwe9";
		coopci.ddia.user.basic.Engine engine = new coopci.ddia.user.basic.Engine();
		engine.init();
		
		engine.addUser(nickname, newPassword, null);
		
		engine.modifyPassword(nickname, newPassword);
		
	}
}
