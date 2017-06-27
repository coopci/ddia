package coopci.ddia.virtual.assets.handlers;

import java.util.HashMap;
import java.util.HashSet;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.Result;
import coopci.ddia.virtual.assets.Engine;
import coopci.ddia.GrizzlyUtils;

/**
 * 
 * */
public class GetAssetsHandler extends HttpHandler {
	public Engine getEngine() {
		return engine;
	}
	public void setEngine(Engine engine) {
		this.engine = engine;
	}
	Engine engine;
	public void service(Request request, Response response) throws Exception {
		Method method = request.getMethod();
		if (!method.getMethodString().equals("GET")) {
			response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
			response.getWriter().write(HttpStatus.METHOD_NOT_ALLOWED_405.getReasonPhrase());
			return;
		}
        long uid = Long.parseLong(request.getParameter("uid"));
        HashSet<String> fields = new HashSet<String>();
        
        if (request.getParameter("fields") != null) {
        	for (String seg : request.getParameter("fields").split(",")) {
        		seg = seg.trim();
        		if (seg.startsWith("va_")) {
        			fields.add(seg);
        		}
        	}
        }
        
        Result res = this.engine.getAssets(uid, fields);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

