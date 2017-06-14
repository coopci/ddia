package coopci.ddia.user.basic.handlers;

import java.util.HashSet;
import java.util.Set;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.GrizzlyUtils;
import coopci.ddia.Result;
import coopci.ddia.user.basic.Engine;

public class LookupUserinfoHandler extends HttpHandler {
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
		String fieldname = request.getParameter("fieldname");
		String fieldvalue = request.getParameter("fieldvalue");
		Set<String> fields = new HashSet<String>();
		for (String f : request.getParameter("fields").split(",")) {
			fields.add(f);
		}
      
        Result res = this.engine.lookupUserinfoByUniqueField(fieldname, fieldvalue, fields);
        GrizzlyUtils.writeJson(response, res);
		
		return;
        
    }
}
