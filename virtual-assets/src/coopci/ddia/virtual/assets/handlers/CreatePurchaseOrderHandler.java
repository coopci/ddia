package coopci.ddia.virtual.assets.handlers;

import java.util.HashMap;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.Result;
import coopci.ddia.virtual.assets.Engine;
import coopci.ddia.GrizzlyUtils;

/**
 * 当需要通过其他（本微服务以外）的方式购买虚拟资产时， 调用这个方法生成订单 。
 * */
public class CreatePurchaseOrderHandler extends HttpHandler {
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
        String appid = request.getParameter("appid");
        HashMap<String, Long> items = new HashMap<String, Long>();
        for (String pname : request.getParameterNames()) {
        	if (!pname.startsWith("va_")) {
        		continue;
        	}
        	String v = request.getParameter(pname);
        	try{
        		items.put(pname, Long.parseLong(v));
        	} catch (Exception ex) {
        		
        	}
        }
        
        Result res = this.engine.createPurchaseOrderHandler(uid, appid, items);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

