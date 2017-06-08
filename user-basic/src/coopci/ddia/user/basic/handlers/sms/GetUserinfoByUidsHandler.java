package coopci.ddia.user.basic.handlers.sms;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.Result;
import coopci.ddia.UidResult;
import coopci.ddia.user.basic.Engine;
import coopci.ddia.GrizzlyUtils;

public class GetUserinfoByUidsHandler extends HttpHandler {
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
        String uidslistStr = request.getParameter("uidslist");
        String fieldslist = request.getParameter("fieldslist");
        
        
        Result res = this.engine.getUserinfoByUids(uidslistStr, fieldslist);
        GrizzlyUtils.writeJson(response, res);
		
		return;
        
    }
}