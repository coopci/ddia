package coopci.ddia.user.basic.handlers;

import java.util.HashMap;
import java.util.HashSet;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.GrizzlyUtils;
import coopci.ddia.Result;
import coopci.ddia.user.basic.Engine;
import coopci.ddia.util.*;

/**
 * 用于后台增加新用户。
 * */
public class AddUserHandler extends HttpHandler {
	
	public static HashSet<String> blockFields = new HashSet<String>();
	
	static {
		blockFields.add("password");
		blockFields.add("username");
		blockFields.add("uid");
		blockFields.add("_id");
		blockFields.add("create_time");
	}
	public Engine getEngine() {
		return engine;
	}
	public void setEngine(Engine engine) {
		this.engine = engine;
	}
	Engine engine;
	public void service(Request request, Response response) throws Exception {
		Method method = request.getMethod();
		if (!method.getMethodString().equals("POST")) {
			response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
			response.getWriter().write(HttpStatus.METHOD_NOT_ALLOWED_405.getReasonPhrase());
			return;
		}
		String password = request.getParameter("password");
		String username = request.getParameter("username");
		
		if (password == null) {
			password = "";
		}
		
		HashMap<String, Object> properties = Funcs.parametersToHashMap(request, blockFields);
		
        Result res = this.engine.addUser(username, password, properties);
        GrizzlyUtils.writeJson(response, res);
		
		return;
        
    }
}
