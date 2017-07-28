package coopci.ddia.gateway;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
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
 * 和注册，登陆有关的操作。
 * */
public interface ILoginAspect extends IGatewayEngine {


	public long getUidFromSessid(String sessid) throws Exception;
	
	public void saveSessionUid(String sessid, long uid);
	
	/**
	 * 提交用户名和密码注册。
	 * */
	default Result register(String sessid, String username, String password) throws Exception {
		DictResult res = new DictResult();
		if (Funcs.isEmpty(sessid)) {
			sessid = this.newSessid();
		} else if (sessid.equals("null")) {
			sessid = this.newSessid();
		}
		// TODO 以后可以在这里加验证captcha之类的。
		HashMap<String, String> args = new HashMap<String, String> (); 
		args.put("username", username);
		args.put("password", password);
		String httpPrefix = this.getMicroserviceHttpPrefix(Engine.MICROSERVICE_NAME_USER_BASIC, username);		
		byte[] response = HttpClientUtil.post(httpPrefix + "user-basic/add_user/", args);
		res = getObjectMapper().readValue(response, DictResult.class);
		
		if (res.code == Result.CODE_OK && res.data.containsKey("uid")) {
			long uid = Funcs.toLong(res.data.get("uid"), -1L); 
			this.saveSessionUid(sessid, uid);
		}
		res.put("sessid", sessid);
		return res;
	}
}
