package coopci.ddia.b2c.renting;

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

import coopci.ddia.b2c.renting.handlers.AddPledgeHandler;
import coopci.ddia.b2c.renting.handlers.GetRentingStatusHandler;




public class HttpServer {
	
    

	public static int listenPort = 8890;
	
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
	            String content = "ddia/b2c-renting";
	            response.setContentType("text/html;charset=utf-8");
	            response.getWriter().write(content);
	        }
	    },
	    "/");
		
		AddPledgeHandler addPledgeHandler = new AddPledgeHandler();
		addPledgeHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				addPledgeHandler,
				"/b2c-renting/add_pledge");
		
		
		GetRentingStatusHandler getRentingStatusHandler = new GetRentingStatusHandler();
		getRentingStatusHandler.setEngine(engine);
		server.getServerConfiguration().addHttpHandler(
				getRentingStatusHandler,
				"/b2c-renting/get_renting_status");
		
		
		try {
			server.removeListener("grizzly");
			
			NetworkListener nl = new NetworkListener("17wan8gateway", "0.0.0.0", listenPort);
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
