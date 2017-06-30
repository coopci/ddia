package coopci.ddia.user.basic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.glassfish.grizzly.http.server.ErrorPageGenerator;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;

import coopci.ddia.user.basic.handlers.LookupUserinfoHandler;
import coopci.ddia.user.basic.handlers.SetUserinfoHandler;
import coopci.ddia.user.basic.handlers.sms.GetUidBySessidHandler;
import coopci.ddia.user.basic.handlers.sms.GetUserinfoByUidsHandler;
import coopci.ddia.user.basic.handlers.sms.LoginSubmitPhoneHandler;
import coopci.ddia.user.basic.handlers.sms.LoginSubmitVcodeHandler;
import coopci.ddia.user.basic.handlers.weixin.LoginWithWeixinHandler;


public class HttpServer {
	public static int listenPort = 8888;
	
	public void start() throws Exception {
		
		Engine engine = new Engine();
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
	            String content = "ddia/user-basic";
	            response.setContentType("text/html;charset=utf-8");
	            response.getWriter().write(content);
	        }
	    },
	    "/");
		
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
		
		GetUidBySessidHandler getUidBySessidHandler = new GetUidBySessidHandler();
		getUidBySessidHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				getUidBySessidHandler,
				"/user-basic/get_uid_by_sessid");
		
		GetUserinfoByUidsHandler getUserinfoByUidsHandler = new GetUserinfoByUidsHandler();
		getUserinfoByUidsHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				getUserinfoByUidsHandler,
				"/user-basic/get_user_info_by_uids");
		
		
		
		
		LookupUserinfoHandler lookupUserinfoHandler = new LookupUserinfoHandler();
		lookupUserinfoHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				lookupUserinfoHandler,
				"/user-basic/lookup_userinfo");
		
		
		SetUserinfoHandler setUserinfoHandler = new SetUserinfoHandler();
		setUserinfoHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				setUserinfoHandler,
				"/user-basic/set_user_info_by_uid");
		
		
		LoginWithWeixinHandler loginWithWeixinHandler = new LoginWithWeixinHandler();
		loginWithWeixinHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				loginWithWeixinHandler,
				"/user-basic/login/weixin");
		
		// "/user-basic/set_user_info_by_uid"
		// "/user-basic/get_user_info_by_session_id"
		// "/user-basic/get_user_info_by_uids"
		
		try {
			server.removeListener("grizzly"); // ɾ��Ĭ�ϵ�Listener��
			
			NetworkListener nl = new NetworkListener("ddia-user-basic", "0.0.0.0", listenPort);
			ThreadPoolConfig threadPoolConfig = ThreadPoolConfig
			        .defaultConfig();
			        //.setCorePoolSize(16)
			        //.setMaxPoolSize(64);
			nl.getTransport().setWorkerThreadPoolConfig(threadPoolConfig);
			
			server.addListener(nl);
		    server.start();
		} catch (Exception e) {
		    System.err.println(e);
		}
		return;
		
	}
	public static void main(String[] argv) throws Exception {
		HttpServer server = new HttpServer();
		
		try {
			server.start();
		} catch (Exception e) {
		    System.err.println(e);
		}

	    System.out.println("Press any key to stop the server...");
	    System.in.read();
		return;
	}
}
