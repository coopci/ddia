package coopci.ddia.chat.handlers;

import java.util.HashMap;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.Result;
import coopci.ddia.chat.Engine;
import coopci.ddia.GrizzlyUtils;

/**
 * 同一个uid的多个资产的incrby操作。 例如用于 用某种资产买另一种资产的 场景。
 * */
public class SendMessageHandler extends HttpHandler {
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
        long fromuid = Long.parseLong(request.getParameter("from_uid"));
        long touid = Long.parseLong(request.getParameter("to_uid"));
        String msg = request.getParameter("message");
        Result res = this.engine.sendMessage(fromuid, touid, msg);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

