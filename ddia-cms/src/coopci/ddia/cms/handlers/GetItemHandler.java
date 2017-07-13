package coopci.ddia.cms.handlers;

import java.util.HashMap;
import java.util.HashSet;

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
 * 获取参数id表示的item的内容。可以获取不属于自己的item。
 * */
public class GetItemHandler extends HttpHandler {
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
        String item_id = request.getParameter("id");
        
        HashSet<String> fields = Funcs.csvToHashSet(request.getParameter("fields"));
        Result res = this.engine.getItem(uid, item_id, fields);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

