package coopci.ddia.gateway.virtual.assets.handlers;

import java.util.HashMap;
import java.util.HashSet;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.Result;
import coopci.ddia.gateway.DemoEngine;
import coopci.ddia.gateway.ICMSAspect;
import coopci.ddia.gateway.IVirtualAssetAspect;
import coopci.ddia.util.Funcs;
import coopci.ddia.GrizzlyUtils;

/**
 * 增减某用户的虚拟资产。
 * */
public class IncrVirtualAssetsHandler extends HttpHandler {
	public IVirtualAssetAspect getEngine() {
		return engine;
	}
	public void setEngine(IVirtualAssetAspect engine) {
		this.engine = engine;
	}
	IVirtualAssetAspect engine;
	public void service(Request request, Response response) throws Exception {
		Method method = request.getMethod();
		if (!method.getMethodString().equals("POST")) {
			response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
			response.getWriter().write(HttpStatus.METHOD_NOT_ALLOWED_405.getReasonPhrase());
			return;
		}
        String sessid = request.getParameter("sessid");
        long uid = Long.parseLong(request.getParameter("uid")); // 要给这个用户增减资产。
        HashMap<String, Long> delta = new HashMap<String, Long>(); 
        for (String pname : request.getParameterNames()) {
        	if (!pname.startsWith("va_")) {
        		continue;
        	}
        	String v = request.getParameter(pname);
        	try{
        		delta.put(pname, Long.parseLong(v));
        	} catch (Exception ex) {
        		
        	}
        }
        Result res = this.engine.incrAssets(sessid, uid, delta);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

