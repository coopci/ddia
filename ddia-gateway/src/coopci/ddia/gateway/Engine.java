package coopci.ddia.gateway;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import coopci.ddia.LoginResult;
import coopci.ddia.Result;
import coopci.ddia.SessionId;
import coopci.ddia.notify.IPublisher;
import coopci.ddia.notify.ISubscriber;
import coopci.ddia.notify.rabbitmq.RabbitmqPublisher;
import coopci.ddia.notify.rabbitmq.RabbitmqSubscriber;
import coopci.ddia.results.DictResult;
import coopci.ddia.results.UserInfosResult;
import coopci.ddia.util.SessidPacker;
import coopci.ddia.util.Vcode;

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
	IPublisher publisher = null;

	public IPublisher getPublisher(){
		return this.publisher;
	}
	
	
	public void initPublisher() throws Exception {
		publisher = new RabbitmqPublisher();
		publisher.start();
	}
	public void init() throws Exception {
		initSessidPacker();
		initHttpClientConfigBuilder();
		initSubscriber();
		initPublisher();
		return;
	}
	String deskeyPath = "../triple-des.key";
	SessidPacker sessidPacker = new SessidPacker();
	public void initSessidPacker() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		sessidPacker.initDes(deskeyPath);
	}
	
	public long getUidFromSessid(String sessid) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		
		if (sessid.startsWith("test-sess-")) {
			return Long.parseLong(sessid.replace("test-sess-", ""));
		}
		
		if (this.cachedSessionUid.containsKey(sessid)) {
			return this.cachedSessionUid.get(sessid);
		}
		SessionId sessionid = sessidPacker.unpack(sessid);
		return sessionid.uid;
	}
	
	
	
	
	
	public static String USER_BASIC_HTTP_PREFIX = "http://localhost:8888/";
	public static String USER_RELATION_HTTP_PREFIX = "http://localhost:8889/";
	public static String VIRTUAL_ASSETS_HTTP_PREFIX = "http://localhost:8893/";
	public static String THIRD_PARTY_PAY_HTTP_PREFIX = "http://localhost:8892/";
	
	public static String MICROSERVICE_NAME_USER_BASIC = "user-basic";
	public static String MICROSERVICE_NAME_USER_RELATION = "user-relation";
	public static String MICROSERVICE_NAME_VIRTUAL_ASSETS = "virtual-assets";
	public static String MICROSERVICE_NAME_THIRD_PARTY_PAY = "third-party-pay";
	
	String getMicroserviceHttpPrefix(String serviceName, String partKey) {
		String ret = "";
		if (MICROSERVICE_NAME_USER_BASIC.equals(serviceName)) {
			return USER_BASIC_HTTP_PREFIX;
		} else if (MICROSERVICE_NAME_USER_BASIC.equals(serviceName)) {
			return USER_RELATION_HTTP_PREFIX;
		} else if (MICROSERVICE_NAME_VIRTUAL_ASSETS.equals(serviceName)) {
			return VIRTUAL_ASSETS_HTTP_PREFIX;
		} else if (MICROSERVICE_NAME_THIRD_PARTY_PAY.equals(serviceName)) {
			return THIRD_PARTY_PAY_HTTP_PREFIX;
		}
		
		return ret;
	}
	String getMicroserviceHttpPrefix(String serviceName, long partKey) {
		String ret = "";
		if (MICROSERVICE_NAME_USER_BASIC.equals(serviceName)) {
			return USER_BASIC_HTTP_PREFIX;
		} else if (MICROSERVICE_NAME_USER_BASIC.equals(serviceName)) {
			return USER_RELATION_HTTP_PREFIX;
		} else if (MICROSERVICE_NAME_VIRTUAL_ASSETS.equals(serviceName)) {
			return VIRTUAL_ASSETS_HTTP_PREFIX;
		} else if (MICROSERVICE_NAME_THIRD_PARTY_PAY.equals(serviceName)) {
			return THIRD_PARTY_PAY_HTTP_PREFIX;
		}
		
		
		return ret;
	}
	
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
	
	
	/**
	 *  创建一个新的sessid并返回给客户端。 
	 * */
	public DictResult startNewSession() {
		DictResult result = new DictResult();
		String newSessid=  UUID.randomUUID().toString();
		result.put("session_id", newSessid);
		return result;
	}
	
	

	/**
	 *  把请求转发给user-basic 去获取手机验证码。
	 *  如果需要captcha之类的方式防御攻击，就可以做在这个方法调用captcha微服务里。 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * */
	public Result loginSubmitPhone(String sessid, String phone) throws ClientProtocolException, IOException {
		
		HashMap<String, String> args = new HashMap<String, String> (); 
		args.put("phone", phone);
		String httpPrefix = this.getMicroserviceHttpPrefix(MICROSERVICE_NAME_USER_BASIC, phone);		
		byte[] response = HttpClientUtil.post(httpPrefix + "login/submit_phone", args);
		Result res = getObjectMapper().readValue(response, Result.class);
		return res;
	}
	
	
	HashMap<String, Long> cachedSessionUid = new HashMap<String, Long>(); 
	public void saveSessionUid(String sessid, long uid) {
		cachedSessionUid.put(sessid, uid);
		// TODO 把这个对应关系存到共享数据里。
		return;
	}
	/**
	 *  把请求转发给user-basic 去验证。
	 *  @param sessid 由 startNewSession 生成的sessid。
	 *  
	 * */
	public LoginResult loginSubmitVcode(String sessid, String phone, String vcode) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		LoginResult res = new LoginResult();
		HashMap<String, String> args = new HashMap<String, String> (); 
		args.put("phone", phone);
		args.put("vcode", vcode);
		String httpPrefix = this.getMicroserviceHttpPrefix(MICROSERVICE_NAME_USER_BASIC, phone);		
		byte[] response = HttpClientUtil.post(httpPrefix + "login/submit_vcode", args);
		res = getObjectMapper().readValue(response, LoginResult.class);
		if (res.code == Result.CODE_OK && res.uid > 0) {
			this.saveSessionUid(sessid, res.uid);
		}
		res.sessid = sessid;
		
		return res;
	}
	
	// 获取nickname指明的用户的公开信息。
	public Result getPublicUserinfo(String sessid, String nickname) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InterruptedException, ExecutionException {
		// Result result = new Result();
		//long uid = getUidFromSessid(sessid);
		long followeeid = -1;
		
		HashMap<String, String> args = new HashMap<String, String> (); 
		args.put("fieldname", "nickname");
		args.put("fieldvalue", "gubo");
		
		String httpPrefix = this.getMicroserviceHttpPrefix(MICROSERVICE_NAME_USER_BASIC, nickname);		
		byte[] response = HttpClientUtil.get(httpPrefix + "user-basic/lookup_userinfo", args);
		
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
		// engine.follow("", "");
	}
}
