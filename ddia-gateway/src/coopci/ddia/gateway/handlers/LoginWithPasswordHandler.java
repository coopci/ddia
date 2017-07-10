package coopci.ddia.gateway.handlers;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.Result;
import coopci.ddia.gateway.Engine;
import coopci.ddia.GrizzlyUtils;

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
        String ident = request.getParameter("ident");
        String sessid = request.getParameter("session_id");
        String password = request.getParameter("password");
        if (password == null)
        	password = "";
        Result res = this.engine.loginWithPassword(sessid, ident, password);
        GrizzlyUtils.writeJson(response, res);
		
		return;
        
    }
}
