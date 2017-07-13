package coopci.ddia.cms.handlers;

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
 * 获取自己名下的名为name的item。如果自己名下没有名字和参数name相同的item，则在自己名下创建一个名字为参数name的item。
 * */
public class GetOrCreateNamedItemHandler extends HttpHandler {
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
        String name = request.getParameter("name");
        
        HashSet<String> fields = Funcs.csvToHashSet(request.getParameter("fields"));
        Result res = this.engine.getOrCreateNamedItem(uid, name, fields);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

