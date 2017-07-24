package coopci.ddia.virtual.assets.handlers;

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
public class DisableComboHandler extends HttpHandler {
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
        Result res = this.engine.disableCombo(uid, id);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

