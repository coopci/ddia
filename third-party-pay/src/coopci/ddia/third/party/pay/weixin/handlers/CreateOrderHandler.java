package coopci.ddia.third.party.pay.weixin.handlers;

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
 * 在本地数据库保存订单信息，
 * 然后按 https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_1 的描述 去微信产生订单。
 * 
 * */
public class CreateOrderHandler extends HttpHandler {
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
		
        
        Result res = this.engine.weixinCreateOrder();
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

