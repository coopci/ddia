package coopci.ddia.user.basic.handlers.weixin;


import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;




import coopci.ddia.GrizzlyUtils;
import coopci.ddia.Result;
import coopci.ddia.user.basic.Engine;
import coopci.ddia.user.basic.weixin.WeixinAPIClient;

public class LoginWithWeixinHandler extends HttpHandler {
	public Engine getEngine() {
		return engine;
	}
	public void setEngine(Engine engine) {
		this.engine = engine;
	}
	Engine engine;
	
	@Override
	public void service(Request request, Response response) throws Exception {
		Method method = request.getMethod();
		if (!method.getMethodString().equals("POST")) {
			response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
			response.getWriter().write(HttpStatus.METHOD_NOT_ALLOWED_405.getReasonPhrase());
			return;
		}
		
		// https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419317851&token=&lang=zh_CN
		// 这个code是 微信返回给客户端的code。
		String code = request.getParameter("code");
		if (code == null || code.length() == 0) {
			response.setStatus(HttpStatus.BAD_REQUEST_400);
			response.getWriter().write("code is required");
			return;
		}
		
		Result res = this.engine.loginWithWeixinSubmitCode(code);
        GrizzlyUtils.writeJson(response, res);
		
		return;
	}
	
	
}
