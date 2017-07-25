package coopci.ddia.tools;


// java coopci.ddia.tools.ModifyPassword admin q298tgy9
// docker run --net="host" coopci/ddia java -classpath ./bin/ddia.jar coopci.ddia.tools.ModifyPassword admin q298tgy9
public class ModifyPassword {

	public static void help() throws Exception {
		
		System.out.println("This command takes exactly 2 parameters: ");
		System.out.println("java coopci.ddia.tools.ModifyPassword $nickname $newpassword");
		
	}
	public static void main(String[] argv) throws Exception {
		
		if (argv.length < 2) {
			
			help();
			System.exit(1);
		}
		String nickname = argv[0];
		String newPassword = argv[1];
		coopci.ddia.user.basic.Engine engine = new coopci.ddia.user.basic.Engine();
		engine.init();
		
		engine.addUser(nickname, newPassword, null);
		
		engine.modifyPassword(nickname, newPassword);
		System.out.println("OK");
	}
}
