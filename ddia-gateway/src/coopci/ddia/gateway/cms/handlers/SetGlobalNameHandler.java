package coopci.ddia.gateway.cms.handlers;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.Result;
import coopci.ddia.gateway.DemoEngine;
import coopci.ddia.gateway.ICMSAspect;
import coopci.ddia.util.Funcs;
import coopci.ddia.GrizzlyUtils;

public class SetGlobalNameHandler extends HttpHandler {
	public ICMSAspect getEngine() {
		return engine;
	}
	public void setEngine(ICMSAspect engine) {
		this.engine = engine;
	}
	ICMSAspect engine;
	public void service(Request request, Response response) throws Exception {
		Method method = request.getMethod();
		if (!method.getMethodString().equals("POST")) {
			response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
			response.getWriter().write(HttpStatus.METHOD_NOT_ALLOWED_405.getReasonPhrase());
			return;
		}
		request.setCharacterEncoding("utf-8");
        String sessid = request.getParameter("sessid");    
        String globalName = request.getParameter("global_name");
        String itemId = request.getParameter("item_id");
        boolean replaceOnExist = Funcs.parseBoolean(request.getParameter("replace_on_exist"), false);
        Result res = this.engine.setGlobalName(sessid, globalName, itemId, replaceOnExist);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

