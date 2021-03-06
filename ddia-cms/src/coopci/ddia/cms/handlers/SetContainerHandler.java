package coopci.ddia.cms.handlers;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.Result;
import coopci.ddia.cms.Engine;
import coopci.ddia.util.Funcs;
import coopci.ddia.GrizzlyUtils;

/**
 * 把member_id表示的内容放入container_id表达的容器。
 * container_id 必须是自己名下的。
 * */
public class SetContainerHandler extends HttpHandler {
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
        String member_id = request.getParameter("member_id");
        String container_id = request.getParameter("container_id");
        int order = Funcs.parseInt(request.getParameter("order"), -1);
        Result res = this.engine.setContainer(uid, member_id, container_id, order);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

