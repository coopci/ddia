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
 * 用密码登陆。
 * */
public class LoginWithPasswordHandler extends HttpHandler {
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
		String ident = request.getParameter("ident");
		
		
		if (password == null) {
			password = "";
		}
		
		if (ident == null) {
			ident = "";
		}
		
		HashSet<String> fields = Funcs.csvToHashSet(request.getParameter("fields"));
		
		
        Result res = this.engine.loginWithPassword(ident, password, fields);
        GrizzlyUtils.writeJson(response, res);
		
		return;
        
    }
}
