package coopci.ddia.gateway;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.http.client.ClientProtocolException;

import com.fasterxml.jackson.databind.ObjectMapper;

import coopci.ddia.Result;
import coopci.ddia.results.DictResult;
import coopci.ddia.results.ListResult;

/**
 * 和CMS有关的操作。
 * */
public interface ICMSAspect extends IGatewayEngine {

	public long getUidFromSessid(String sessid) throws Exception;
	
	default public Result cmsRoot(String sessid, String fields, int start, int limit) throws Exception {
		ListResult result = new ListResult();
		long uid = getUidFromSessid(sessid);
		if (fields==null) {
			fields = "";
		}
		String httpPrefix = this.getMicroserviceHttpPrefix(Engine.MICROSERVICE_NAME_CMS, uid);
		HashMap<String, String> args = new HashMap<String, String> (); 
		args.put("uid", Long.toString(uid));
		args.put("fields", fields);
		args.put("start", Integer.toString(start));
		args.put("limit", Integer.toString(limit));
		byte[] cmsRootResponse = HttpClientUtil.get(httpPrefix + "cms/get_global_named_items", args);	
		result = getObjectMapper().readValue(cmsRootResponse, ListResult.class);
		return result;
	}
		
	
	default public Result getCmsItem(String sessid, String fields, String id) throws Exception {
		DictResult result = new DictResult();
		long uid = getUidFromSessid(sessid);
		if (fields==null) {
			fields = "";
		}
		
		String httpPrefix = this.getMicroserviceHttpPrefix(Engine.MICROSERVICE_NAME_CMS, id);
		HashMap<String, String> args = new HashMap<String, String> (); 
		args.put("uid", Long.toString(uid));
		args.put("fields", fields);
		args.put("id", id);
		
		byte[] cmsRootResponse = HttpClientUtil.get(httpPrefix + "cms/get_item", args);	
		
		result = getObjectMapper().readValue(cmsRootResponse, DictResult.class);
		
		
		return result;
	}
		
	default public Result cmsMembers(String sessid, String container_id, String fields, int start, int limit) throws Exception {
		ListResult result = new ListResult();
		long uid = getUidFromSessid(sessid);
		if (fields==null) {
			fields = "";
		}
		String httpPrefix = this.getMicroserviceHttpPrefix(Engine.MICROSERVICE_NAME_CMS, uid);
		HashMap<String, String> args = new HashMap<String, String> (); 
		args.put("uid", Long.toString(uid));
		args.put("fields", fields);
		args.put("container_id", container_id);
		args.put("start", Integer.toString(start));
		args.put("limit", Integer.toString(limit));
		byte[] cmsResponse = HttpClientUtil.get(httpPrefix + "cms/get_members", args);	
		result = getObjectMapper().readValue(cmsResponse, ListResult.class);
		return result;
	}
	
	
	default Result setGlobalName(String sessid, String globalName, String itemId, boolean replaceOnExist) throws Exception {
		
		long uid = getUidFromSessid(sessid);
		
		// TODO　检查　uid 有没有设置cms的global name的权限。
		
		String httpPrefix = this.getMicroserviceHttpPrefix(Engine.MICROSERVICE_NAME_CMS, uid);
		HashMap<String, String> args = new HashMap<String, String> (); 
		args.put("uid", Long.toString(uid));
		args.put("global_name", globalName);
		args.put("item_id", itemId);
		args.put("replace_on_exist", Boolean.toString(replaceOnExist));
		byte[] cmsResponse = HttpClientUtil.post(httpPrefix + "cms/set_global_name", args);
		String s = new String(cmsResponse);
		Result result = getObjectMapper().readValue(cmsResponse, Result.class);
		return result;
	}
}
