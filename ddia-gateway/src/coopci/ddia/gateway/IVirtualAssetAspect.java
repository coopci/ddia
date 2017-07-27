package coopci.ddia.gateway;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.http.client.ClientProtocolException;
import org.bson.Document;

import com.fasterxml.jackson.databind.ObjectMapper;

import coopci.ddia.LoginResult;
import coopci.ddia.Result;
import coopci.ddia.results.DictResult;
import coopci.ddia.results.ListResult;
import coopci.ddia.util.Funcs;

/**
 * 和虚拟资产有关的操作。
 * */
public interface IVirtualAssetAspect extends IGatewayEngine {


	public long getUidFromSessid(String sessid) throws Exception;
	public boolean checkPermission(long uid, String perm);
	
	/**
	 * 获取自己的资产。
	 * */
	default Result getMyAssets(String sessid, String assetNames) throws Exception {
		DictResult res = new DictResult();
		
		long uid = getUidFromSessid(sessid);
		if (assetNames==null)
			assetNames = "";
		HashMap<String, String> args = new HashMap<String, String> (); 
		args.put("uid", Long.toString(uid));
		args.put("fields", assetNames);
		String httpPrefix = this.getMicroserviceHttpPrefix(Engine.MICROSERVICE_NAME_VIRTUAL_ASSETS, uid);		
		byte[] response = HttpClientUtil.get(httpPrefix + "virtual-assets/get", args);
		res = getObjectMapper().readValue(response, DictResult.class);
		return res;
	}
	
	public static String PERMISSION_INCR_VIRTUAL_ASSETS = "incr_virtual_assets";
	/**
	 * 增减某用户的虚拟资产。
	 * */
	default Result incrAssets(String sessid, long targetUid, HashMap<String, Long> delta) throws Exception {
		DictResult res = new DictResult();
		
		long uid = getUidFromSessid(sessid);
		// TODO 检查uid是否有干这件事的权限。
		boolean granted = this.checkPermission(uid, PERMISSION_INCR_VIRTUAL_ASSETS);
		if (!granted) {
			res.code = 403;
			res.msg = "Permission denied.";
			return res;
		}
		HashMap<String, String> args = new HashMap<String, String> (); 
		args.put("uid", Long.toString(targetUid));
		for (Entry<String, Long> entry: delta.entrySet()) {
			args.put(entry.getKey(), Long.toString(entry.getValue()));
		}
		
		String httpPrefix = this.getMicroserviceHttpPrefix(Engine.MICROSERVICE_NAME_VIRTUAL_ASSETS, uid);		
		byte[] response = HttpClientUtil.post(httpPrefix + "virtual-assets/incrby", args);
		res = getObjectMapper().readValue(response, DictResult.class);
		return res;
	}
}
