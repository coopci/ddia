package coopci.ddia.user.basic.handlers;

import java.util.HashMap;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.GrizzlyUtils;
import coopci.ddia.Result;
import coopci.ddia.user.basic.Engine;

public class SetUserinfoHandler extends HttpHandler {
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
        long uid = Long.parseLong(request.getParameter("uid"));
        HashMap<String, Object> info = new HashMap<String, Object>(); 
        for (String p : request.getParameterNames()) {
        	if (p.equals("uid")) {
        		continue;
        	} else {
        		info.put(p, request.getParameter(p));
        	}
        }
        
        Result res = this.engine.setUserinfo(uid, info);
        GrizzlyUtils.writeJson(response, res);
		
		return;
        
    }
}
