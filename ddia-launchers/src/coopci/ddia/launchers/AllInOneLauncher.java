package coopci.ddia.launchers;

import coopci.ddia.user.relation.HttpServer;

// 在同一个jvm中  启动gateway和所有的微服务。
public class AllInOneLauncher {
	public void launch() throws Exception {
		coopci.ddia.gateway.HttpServer httpGateway = new coopci.ddia.gateway.HttpServer();
		coopci.ddia.user.basic.HttpServer httpUserbasic = new coopci.ddia.user.basic.HttpServer();
		coopci.ddia.user.relation.HttpServer httpUserelation = new coopci.ddia.user.relation.HttpServer();
		coopci.ddia.b2c.renting.HttpServer httpB2cRenting = new coopci.ddia.b2c.renting.HttpServer();
		coopci.ddia.virtual.assets.HttpServer httpVirualAssets = new coopci.ddia.virtual.assets.HttpServer();
		coopci.ddia.third.party.pay.HttpServer httpThirdPartyPay = new coopci.ddia.third.party.pay.HttpServer();
		coopci.ddia.chat.HttpServer httpChat = new coopci.ddia.chat.HttpServer();
		
		httpGateway.start();
		httpUserbasic.start();
		httpUserelation.start();
		httpB2cRenting.start();
		httpVirualAssets.start();
		httpThirdPartyPay.start();
		httpChat.start();
	}
	
	
	public static void main(String[] argv) throws Exception {
		AllInOneLauncher launcher = new AllInOneLauncher();
		try {
			launcher.launch();
		} catch (Exception e) {
		    System.err.println(e);
		}
		System.out.println("Press any key to stop the servers...");
	    System.in.read();
		return;
	}
}
