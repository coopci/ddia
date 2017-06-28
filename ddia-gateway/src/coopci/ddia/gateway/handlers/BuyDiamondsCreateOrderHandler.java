package coopci.ddia.gateway.handlers;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.Result;
import coopci.ddia.gateway.DemoEngine;
import coopci.ddia.GrizzlyUtils;


// 创建买钻石的订单，和BuyDiamondsCheckOrderHandler配合。
public class BuyDiamondsCreateOrderHandler extends HttpHandler {
	public DemoEngine getEngine() {
		return engine;
	}
	public void setEngine(DemoEngine engine) {
		this.engine = engine;
	}
	DemoEngine engine;
	public void service(Request request, Response response) throws Exception {
		Method method = request.getMethod();
		if (!method.getMethodString().equals("POST")) {
			response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
			response.getWriter().write(HttpStatus.METHOD_NOT_ALLOWED_405.getReasonPhrase());
			return;
		}
		request.setCharacterEncoding("utf-8");
        String sessid = request.getParameter("sessid");    
        Long number = Long.parseLong(request.getParameter("number")); // 要买的钻石数。
        String payChannel = request.getParameter("pay_channel");
        Result res = this.engine.buyDiamondsCreateOrder(sessid, number, payChannel);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

