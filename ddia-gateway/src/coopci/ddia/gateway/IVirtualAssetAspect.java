package coopci.ddia.gateway;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
}
