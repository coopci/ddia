package coopci.ddia.gateway.handlers;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.Result;
import coopci.ddia.gateway.Engine;
import coopci.ddia.GrizzlyUtils;

/**
 * 把nickname指明的用户的公开信息返回给客户端。
 * 这个是用来向用户展示 其他用户的 信息的。
 * */
public class GetPublicUsrinfoHandler extends HttpHandler {
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
        String sessid = request.getParameter("sessid");
        String nickname = request.getParameter("nickname");
        Result res = this.engine.getPublicUserinfo(sessid, nickname);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

