package coopci.ddia.virtual.assets;


import org.glassfish.grizzly.http.server.ErrorPageGenerator;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coopci.ddia.virtual.assets.handlers.CreatePurchaseOrderHandler;
import coopci.ddia.virtual.assets.handlers.DisableComboHandler;
import coopci.ddia.virtual.assets.handlers.EnableComboHandler;
import coopci.ddia.virtual.assets.handlers.GetAssetsHandler;
import coopci.ddia.virtual.assets.handlers.GetComboHandler;
import coopci.ddia.virtual.assets.handlers.GetCombosHandler;
import coopci.ddia.virtual.assets.handlers.IncrbyHandler;
import coopci.ddia.virtual.assets.handlers.PostprocessPurchaseOrderHandler;
import coopci.ddia.virtual.assets.handlers.TransferHandler;



public class HttpServer {
	
	public static int listenPort = 8893;
	
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
	            String content = "ddia/virtual-assets";
	            response.setContentType("text/html;charset=utf-8");
	            response.getWriter().write(content);
	        }
	    },
	    "/");
		
		
		
		IncrbyHandler incrbyHandler = new IncrbyHandler();
		incrbyHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				incrbyHandler,
				"/virtual-assets/incrby");
		


		TransferHandler transferHandler = new TransferHandler();
		transferHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				transferHandler,
				"/virtual-assets/transfer");
		
		GetAssetsHandler getAssetsHandler = new GetAssetsHandler();
		getAssetsHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				getAssetsHandler,
				"/virtual-assets/get");
		
		CreatePurchaseOrderHandler createPurchaseOrderHandler = new CreatePurchaseOrderHandler();
		createPurchaseOrderHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				createPurchaseOrderHandler,
				"/virtual-assets/create_purchase_order");
		
		PostprocessPurchaseOrderHandler postprocessPurchaseOrderHandler = new PostprocessPurchaseOrderHandler();
		postprocessPurchaseOrderHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				postprocessPurchaseOrderHandler,
				"/virtual-assets/postprocess_purchase_order");
		
		
		GetCombosHandler getCombosHandler = new GetCombosHandler();
		getCombosHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				getCombosHandler,
				"/virtual-assets/get_combos");
		

		GetComboHandler getComboHandler = new GetComboHandler();
		getComboHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				getComboHandler,
				"/virtual-assets/get_combo");
		
		
		DisableComboHandler disableComboHandler = new DisableComboHandler();
		disableComboHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				disableComboHandler,
				"/virtual-assets/disable_combo");
		

		EnableComboHandler enableComboHandler = new EnableComboHandler();
		enableComboHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				enableComboHandler,
				"/virtual-assets/enable_combo");
		
		
		try {
			server.removeListener("grizzly");
			logger.info("ddia-virtual-assets start listening:" + listenPort );
			NetworkListener nl = new NetworkListener("ddia-virtual-assets", "0.0.0.0", listenPort);
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
