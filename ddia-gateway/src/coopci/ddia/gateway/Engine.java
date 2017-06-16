package coopci.ddia.gateway;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Response;
import org.bson.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

import coopci.ddia.Result;
import coopci.ddia.SessionId;
import coopci.ddia.notify.ISubscriber;
import coopci.ddia.notify.rabbitmq.RabbitmqSubscriber;
import coopci.ddia.results.ListResult;
import coopci.ddia.results.RentingStatusResult;
import coopci.ddia.results.UserInfo;
import coopci.ddia.results.UserInfosResult;
import coopci.ddia.util.SessidPacker;

public class Engine {
	
	
	DefaultAsyncHttpClientConfig.Builder httpclientconfigbuilder = new DefaultAsyncHttpClientConfig.Builder();
    
	public void initHttpClientConfigBuilder() {
//	    builder.setCompressionEnabled(true)
//        .setAllowPoolingConnection(true)
//        .setRequestTimesout(30000)
//        .build();
//
		httpclientconfigbuilder.setRequestTimeout(30000);
		
	}
	
	ISubscriber subscriber = null;

	public ISubscriber getSubscriber(){
		return this.subscriber;
	}
	
	public void initSubscriber() throws Exception {
		
		subscriber = new RabbitmqSubscriber();
		subscriber.start();
		
		
	}
	public void init() throws Exception {
		initSessidPacker();
		initHttpClientConfigBuilder();
		initSubscriber();
		return;
	}
	String deskeyPath = "../triple-des.key";
	SessidPacker sessidPacker = new SessidPacker();
	public void initSessidPacker() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		sessidPacker.initDes(deskeyPath);
	}
	
	public long getUidFromSessid(String sessid) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		// 也可能改成去user-basic问，并cache住。
		
		if (sessid.startsWith("test-sess-")) {
			return Long.parseLong(sessid.replace("test-sess-", ""));
		}
		
		
		SessionId sessionid = sessidPacker.unpack(sessid);
		return sessionid.uid;
		
	}
	
	
	
	
	
	public static String USER_BASIC_HTTP_PREFIX = "http://localhost:8888/";
	public static String USER_RELATION_HTTP_PREFIX = "http://localhost:8889/";
	
	Long[] longArray = new Long[0];
	
	
	public ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper;
	}
	
	
	public long lookupUidByUniqueField(String fieldname, String fieldvalue) throws ClientProtocolException, IOException {
		long ret = -1;

		HashMap<String, String> args = new HashMap<String, String> (); 
		args.put("fieldname", fieldname);
		args.put("fieldvalue", fieldvalue);
		
		byte[] response = HttpClientUtil.get(USER_BASIC_HTTP_PREFIX + "user-basic/lookup_userinfo", args);
		UserInfosResult userinfoResult = getObjectMapper().readValue(response, UserInfosResult.class);
		if (userinfoResult.code == 200) {
			ret = userinfoResult.data.keySet().toArray(longArray)[0];
		}
		else {
			ret = -1;
		}
		return ret;
	}
	
	
	// followee 是昵称或者 其他 用户可以从界面上看到的 能标出 跟随目标的 字符串
	public Result follow(String sessid, String followee) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InterruptedException, ExecutionException {
		Result result = new Result();
		long uid = getUidFromSessid(sessid);
		long followeeid = -1;
		
		followeeid = this.lookupUidByUniqueField("nickname", followee);
		
		if (followeeid > 0 ) {
			HashMap<String, String> args = new HashMap<String, String> (); 
			args.put("uid", Long.toString(uid));
			args.put("followee", Long.toString(followeeid));
			byte[] followResponse = HttpClientUtil.post(USER_RELATION_HTTP_PREFIX + "user-relation/follow", args);			
			result = getObjectMapper().readValue(followResponse, Result.class);
		} else {
			result.code = 404;
			result.msg = "Couldn't find user: " + followee;
		}
		return result;
	}
	
	// followee 是昵称或者 其他 用户可以从界面上看到的 能标出 跟随目标的 字符串
	public Result unfollow(String sessid, String followee) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InterruptedException, ExecutionException {
		Result result = new Result();
		long uid = getUidFromSessid(sessid);
		long followeeid = -1;
		
		followeeid = this.lookupUidByUniqueField("nickname", followee);
		
		if (followeeid > 0 ) {
			HashMap<String, String> args = new HashMap<String, String> (); 
			args.put("uid", Long.toString(uid));
			args.put("followee", Long.toString(followeeid));
			byte[] followResponse = HttpClientUtil.post(USER_RELATION_HTTP_PREFIX + "user-relation/unfollow", args);			
			result = getObjectMapper().readValue(followResponse, Result.class);
		} else {
			result.code = 404;
			result.msg = "Couldn't find user: " + followee;
		}
		return result;
	}
	
	// 获取nickname指明的用户的公开信息。
	public Result getPublicUserinfo(String sessid, String nickname) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InterruptedException, ExecutionException {
		// Result result = new Result();
		//long uid = getUidFromSessid(sessid);
		long followeeid = -1;
		
		HashMap<String, String> args = new HashMap<String, String> (); 
		args.put("fieldname", "nickname");
		args.put("fieldvalue", "gubo");
		
		byte[] response = HttpClientUtil.get(USER_BASIC_HTTP_PREFIX + "user-basic/lookup_userinfo", args);
		// UserInfosResult.
		ObjectMapper objectMapper = new ObjectMapper();
		UserInfosResult result = objectMapper.readValue(response, UserInfosResult.class);
		return result;
		
	}
	
	public void f() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        
        try {
            HttpGet httpget = new HttpGet("http://www.baidu.com/");

            System.out.println("Executing request " + httpget.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            
            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
        } finally {
            httpclient.close();
        }
    }
	
	
	public static void main(String[] args) throws Exception {
		Engine engine = new Engine();
		engine.init();
		// engine.f();
		engine.follow("", "");
	}
	
}
