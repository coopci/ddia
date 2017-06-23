package coopci.ddia.third.party.pay.backdoor.handlers;

import java.util.HashMap;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.Result;
import coopci.ddia.third.party.pay.Engine;
import coopci.ddia.GrizzlyUtils;

/**
 * 在本地检查订单是否已经处于最终状态（例如支付成功，或者支付失败），如果不处于最终状态，
 * 则按 https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_2&index=4 的描述 去微信查询订单。
 * 按照查询结果更新本地的订单状态。
 * 
 * */
public class BackdoorCheckOrderHandler extends HttpHandler {
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
        
        Result res = this.engine.backdoorCheckOrder();
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

