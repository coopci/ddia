package coopci.ddia.gateway.cms.handlers;

import java.util.HashSet;

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

/**

 * */
public class GetCMSItemHandler extends HttpHandler {
	public ICMSAspect getEngine() {
		return engine;
	}
	public void setEngine(ICMSAspect engine) {
		this.engine = engine;
	}
	ICMSAspect engine;
	public void service(Request request, Response response) throws Exception {
		Method method = request.getMethod();
		if (!method.getMethodString().equals("GET")) {
			response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
			response.getWriter().write(HttpStatus.METHOD_NOT_ALLOWED_405.getReasonPhrase());
			return;
		}
        String sessid = request.getParameter("sessid");
        String fields = request.getParameter("fields");
        String id = request.getParameter("id");
        Result res = this.engine.getCmsItem(sessid, fields, id);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

