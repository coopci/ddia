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
 * 用于在两个uid之间转移资产。
 * */
public class TransferHandler extends HttpHandler {
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
        long fromuid = Long.parseLong(request.getParameter("from_uid"));
        long touid = Long.parseLong(request.getParameter("to_uid"));
        String appid = request.getParameter("appid");
        String apptranxid = request.getParameter("apptranxid");
        HashMap<String, Long> fromassets = new HashMap<String, Long>(); 
        HashMap<String, Long> toassets = new HashMap<String, Long>();
        for (String pname : request.getParameterNames()) {
        	if (pname.startsWith("from.va_")) {
        		String v = request.getParameter(pname);
            	try{
            		fromassets.put(pname.substring(5), Long.parseLong(v));
            	} catch (Exception ex) {
            	}
        	} else if (pname.startsWith("to.va_")) {
        		String v = request.getParameter(pname);
            	try{
            		toassets.put(pname.substring(3), Long.parseLong(v));
            	} catch (Exception ex) {
            	}
        	}
        }
        
        Result res = this.engine.transfer(appid, apptranxid, fromuid, fromassets, touid, toassets);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

