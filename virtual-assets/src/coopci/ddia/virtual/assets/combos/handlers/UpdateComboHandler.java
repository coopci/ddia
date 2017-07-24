package coopci.ddia.virtual.assets.combos.handlers;

import java.util.HashMap;

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
public class UpdateComboHandler extends HttpHandler {
	public Engine getEngine() {
		return engine;
	}
	public void setEngine(Engine engine) {
		this.engine = engine;
	}
	Engine engine;
	public void service(Request request, Response response) throws Exception {
		request.setCharacterEncoding("utf-8");
		Method method = request.getMethod();
		if (!method.getMethodString().equals("POST")) {
			response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
			response.getWriter().write(HttpStatus.METHOD_NOT_ALLOWED_405.getReasonPhrase());
			return;
		}
        
		long uid = Long.parseLong(request.getParameter("uid"));
        String id = request.getParameter("id");
        HashMap<String, Long> price = new HashMap<String, Long>();
        HashMap<String, String> fields = new HashMap<String, String>();
        
        for (String fn : request.getParameterNames()) {
        	String[] segs = fn.split("\\.");
        	if (segs.length == 2) {
        		if (segs[0].equals("price")) {
        			String currency = segs[1].toUpperCase();
        			long value = Long.parseLong(request.getParameter(fn));
        			price.put(currency, value);
        		} else if (segs[0].equals("field")) {
        			String k = segs[1];
        			String v = request.getParameter(fn);
        			fields.put(k, v);
        		} else {
        			
        		}
        	}
        }
        
        Result res = this.engine.updateCombo(uid, id, price, fields);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

