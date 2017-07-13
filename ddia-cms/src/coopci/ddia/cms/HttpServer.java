package coopci.ddia.cms;

import org.glassfish.grizzly.http.server.ErrorPageGenerator;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coopci.ddia.cms.handlers.CreateItemHandler;
import coopci.ddia.cms.handlers.GetItemHandler;
import coopci.ddia.cms.handlers.GetMembersByNameHandler;
import coopci.ddia.cms.handlers.GetMembersHandler;
import coopci.ddia.cms.handlers.GetOrCreateNamedItemHandler;
import coopci.ddia.cms.handlers.SaveItemHandler;
import coopci.ddia.cms.handlers.SetContainerHandler;



public class HttpServer {
	
	public static int listenPort = 8895;
	Logger logger = LoggerFactory.getLogger(HttpServer.class);
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
	            String content = "ddia/cms";
	            response.setContentType("text/html;charset=utf-8");
	            response.getWriter().write(content);
	        }
	    },
	    "/");
		
		// create
		
		

		GetOrCreateNamedItemHandler getOrCreateNamedItemHandler = new GetOrCreateNamedItemHandler();
		getOrCreateNamedItemHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				getOrCreateNamedItemHandler,
				"/cms/get_or_create_named_item");
		
		
		GetItemHandler getItemHandler = new GetItemHandler();
		getItemHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				getItemHandler,
				"/cms/get_item");
		
		
		GetMembersHandler getMembersHandler = new GetMembersHandler();
		getMembersHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				getMembersHandler,
				"/cms/get_members");
		
		GetMembersByNameHandler getMembersByNameHandler = new GetMembersByNameHandler();
		getMembersByNameHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				getMembersByNameHandler,
				"/cms/get_members_by_name");
		
		
		CreateItemHandler createItemHandler = new CreateItemHandler();
		createItemHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				createItemHandler,
				"/cms/create_item");
		

		SaveItemHandler saveItemHandler = new SaveItemHandler();
		saveItemHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				saveItemHandler,
				"/cms/save_item");
		
		

		SetContainerHandler setContainerHandler = new SetContainerHandler();
		setContainerHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				setContainerHandler,
				"/cms/set_container");
		
		// set_container
		
		try {
			server.removeListener("grizzly");
			
			NetworkListener nl = new NetworkListener("ddia-third-party-pay", "0.0.0.0", listenPort);
			ThreadPoolConfig threadPoolConfig = ThreadPoolConfig
			        .defaultConfig();
			        //.setCorePoolSize(16)
			        //.setMaxPoolSize(64);
			nl.getTransport().setWorkerThreadPoolConfig(threadPoolConfig);
			
			server.addListener(nl);
		    server.start();
		} catch (Exception e) {
		    System.err.println(e);
		    // logger.error(arg0);
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
