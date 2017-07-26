package coopci.ddia.gateway.virtual.assets.handlers;

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
 * 获取自己的资产。
 * */
public class GetMyVirtualAssetsHandler extends HttpHandler {
	public IVirtualAssetAspect getEngine() {
		return engine;
	}
	public void setEngine(IVirtualAssetAspect engine) {
		this.engine = engine;
	}
	IVirtualAssetAspect engine;
	public void service(Request request, Response response) throws Exception {
		Method method = request.getMethod();
		if (!method.getMethodString().equals("GET")) {
			response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
			response.getWriter().write(HttpStatus.METHOD_NOT_ALLOWED_405.getReasonPhrase());
			return;
		}
        String sessid = request.getParameter("sessid");
        String assetNames = request.getParameter("asset_names");
        Result res = this.engine.getMyAssets(sessid, assetNames);
        GrizzlyUtils.writeJson(response, res);
		return;
    }
}

