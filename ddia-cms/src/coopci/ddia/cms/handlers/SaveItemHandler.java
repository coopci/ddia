package coopci.ddia.cms.handlers;

import java.util.HashMap;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.Result;
import coopci.ddia.cms.Engine;
import coopci.ddia.GrizzlyUtils;

/**
 * 保存item的内容。
 * */
public class SaveItemHandler extends HttpHandler {
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
        String id = request.getParameter("id");
        HashMap<String, Object> content = new HashMap<String, Object>(); 
        for (String n : request.getParameterNames()) {
        	if (n.startsWith("set__") || n.startsWith("incrby__") )
        		content.put(n, request.getParameter(n));
        	
        }
        Result res = this.engine.saveItem(uid, id, content);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

