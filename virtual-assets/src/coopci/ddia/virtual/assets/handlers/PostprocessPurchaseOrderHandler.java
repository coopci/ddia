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
 * 用来接受 CreatePurchaseOrderHandler产生的订单 的 完成,失败等结果 的通知。
 * 这个流程 还会根据订单结果执行 相应的效果。
 * */
public class PostprocessPurchaseOrderHandler extends HttpHandler {
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
        String apptranxid = request.getParameter("apptranxid");
        String payResult = request.getParameter("pay_result");
        
        Result res = this.engine.postprocessPurchaseOrderHandler(uid, appid, apptranxid, payResult);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

