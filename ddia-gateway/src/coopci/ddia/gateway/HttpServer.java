package coopci.ddia.gateway;

import org.glassfish.grizzly.http.server.ErrorPageGenerator;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.websockets.OptimizedBroadcaster;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;

import coopci.ddia.gateway.cms.handlers.GetCMSItemHandler;
import coopci.ddia.gateway.cms.handlers.GetCMSMembersHandler;
import coopci.ddia.gateway.cms.handlers.GetCMSRootHandler;
import coopci.ddia.gateway.cms.handlers.SetGlobalNameHandler;
import coopci.ddia.gateway.handlers.BuyDiamondsCheckOrderHandler;
import coopci.ddia.gateway.handlers.BuyDiamondsCreateOrderHandler;
import coopci.ddia.gateway.handlers.FollowHandler;
import coopci.ddia.gateway.handlers.GetPublicUsrinfoHandler;
import coopci.ddia.gateway.handlers.LoginSubmitPhoneHandler;
import coopci.ddia.gateway.handlers.LoginSubmitVcodeHandler;
import coopci.ddia.gateway.handlers.LoginWithPasswordHandler;
import coopci.ddia.gateway.handlers.NewSessionHandler;
import coopci.ddia.gateway.handlers.SendChatMessageHandler;
import coopci.ddia.gateway.handlers.SendMsgHandler;
import coopci.ddia.gateway.handlers.UnfollowHandler;
import coopci.ddia.gateway.websocket.BroadcastApplication;
import coopci.ddia.gateway.websocket.GatewayApplication;



public class HttpServer {
	
	public static int listenPort = 8887;
	
	
	
	public void start () throws Exception {
		DemoEngine engine = new DemoEngine();
		engine.init();
		
		final org.glassfish.grizzly.http.server.HttpServer server = new org.glassfish.grizzly.http.server.HttpServer();
		org.glassfish.grizzly.http.server.DefaultErrorPageGenerator defaultEpg = new org.glassfish.grizzly.http.server.DefaultErrorPageGenerator();
		ErrorPageGenerator epg = server.getServerConfiguration().getDefaultErrorPageGenerator();
		
		//NetworkListener nl = new NetworkListener("test-listener", "localhost", 8181);
		//server.addListener(nl);
		//HttpServer server = HttpServer.createSimpleServer();
		server.getServerConfiguration().addHttpHandler(
			new HttpHandler() {
	    	// http://127.0.0.1:8080/time
	        public void service(Request request, Response response) throws Exception {
	        	String contextpath = request.getContextPath();
	        	String pathinfo = request.getPathInfo();
	            String content = "ddia/gateway";
	            response.setContentType("text/html;charset=utf-8");
	            response.getWriter().write(content);
	        }
	    },
	    "/");
		
		
		
		
		GetPublicUsrinfoHandler getPublicUsrinfoHandler = new GetPublicUsrinfoHandler();
		getPublicUsrinfoHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				getPublicUsrinfoHandler,
				"/get_public_usr_info");
		

		FollowHandler followHandler = new FollowHandler();
		followHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				followHandler,
				"/follow");
		
		UnfollowHandler unfollowHandler = new UnfollowHandler();
		unfollowHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				unfollowHandler,
				"/unfollow");
		
		
		SendMsgHandler sendMsgHandler = new SendMsgHandler();
		sendMsgHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				sendMsgHandler,
				"/sendmsg");
		
		NewSessionHandler newSessionHandler = new NewSessionHandler();
		newSessionHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				newSessionHandler,
				"/start_new_session");
		
		
		LoginSubmitPhoneHandler loginSubmitPhoneHandler = new LoginSubmitPhoneHandler();
		loginSubmitPhoneHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				loginSubmitPhoneHandler,
				"/login/submit_phone");
		
		LoginSubmitVcodeHandler loginSubmitVcodeHandler = new LoginSubmitVcodeHandler();
		loginSubmitVcodeHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				loginSubmitVcodeHandler,
				"/login/submit_vcode");
		
		BuyDiamondsCreateOrderHandler buyDiamondsCreateOrderHandler = new BuyDiamondsCreateOrderHandler();
		buyDiamondsCreateOrderHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				buyDiamondsCreateOrderHandler,
				"/buy_diamonds/create_order");
		
		BuyDiamondsCheckOrderHandler buyDiamondsCheckOrderHandler = new BuyDiamondsCheckOrderHandler();
		buyDiamondsCheckOrderHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				buyDiamondsCheckOrderHandler,
				"/buy_diamonds/check_order");
		
		SendChatMessageHandler sendChatMessageHandler = new SendChatMessageHandler();
		sendChatMessageHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				sendChatMessageHandler,
				"/send_chat_message");
		
		LoginWithPasswordHandler loginWithPasswordHandler = new LoginWithPasswordHandler();
		loginWithPasswordHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				loginWithPasswordHandler,
				"/login_with_password");
		
		
		GetCMSRootHandler getCMSRootHandler = new GetCMSRootHandler();
		getCMSRootHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				getCMSRootHandler,
				"/get_cms_root");
		
		GetCMSItemHandler getCMSItemHandler = new GetCMSItemHandler();
		getCMSItemHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				getCMSItemHandler,
				"/get_cms_item");
		
		GetCMSMembersHandler getCMSMembersHandler = new GetCMSMembersHandler();
		getCMSMembersHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				getCMSMembersHandler,
				"/get_cms_members");
		
		SetGlobalNameHandler setGlobalNameHandler = new SetGlobalNameHandler();
		setGlobalNameHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				setGlobalNameHandler,
				"/set_cms_global_name");
		
		
		
		server.getServerConfiguration().addHttpHandler(
		            new StaticHttpHandler("."), "/static");
		
		try {
			server.removeListener("grizzly");
			NetworkListener nl = new NetworkListener("ddia-gateway", "0.0.0.0", listenPort);
			
			
			ThreadPoolConfig threadPoolConfig = ThreadPoolConfig
			        .defaultConfig();
			        //.setCorePoolSize(16)
			        //.setMaxPoolSize(64);
			nl.getTransport().setWorkerThreadPoolConfig(threadPoolConfig);
			
			server.addListener(nl);
			

			final WebSocketAddOn addon = new WebSocketAddOn();
			for (NetworkListener listener : server.getListeners()) {
			    listener.registerAddOn(addon);
			}
			WebSocketEngine.getEngine().register("", "/broadcast", new BroadcastApplication(new OptimizedBroadcaster()));
			
			GatewayApplication gatewayApplication = new GatewayApplication();
			gatewayApplication.engine = engine;
			WebSocketEngine.getEngine().register("", "/gateway", gatewayApplication);
			engine.getSubscriber().setDownPublisher(gatewayApplication);
			
		    server.start();
		    
		} catch (Exception e) {
		    System.err.println(e);
		}
		return;
		
		
	}
	public static void main(String[] argv) throws Exception {
		HttpServer server = new HttpServer();
		server.start();
		System.out.println("Press any key to stop the server...");
	    System.in.read();
		return;
	}
}
