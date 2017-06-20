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
import coopci.ddia.notify.IPublisher;
import coopci.ddia.notify.ISubscriber;
import coopci.ddia.notify.rabbitmq.RabbitmqPublisher;
import coopci.ddia.notify.rabbitmq.RabbitmqSubscriber;
import coopci.ddia.results.ListResult;
import coopci.ddia.results.RentingStatusResult;
import coopci.ddia.results.UserInfo;
import coopci.ddia.results.UserInfosResult;
import coopci.ddia.util.SessidPacker;

public class DemoEngine extends Engine {
	
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
			String httpPrefix = this.getMicroserviceHttpPrefix(MICROSERVICE_NAME_USER_RELATION, uid);
			byte[] followResponse = HttpClientUtil.post(httpPrefix + "user-relation/follow", args);			
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
			String httpPrefix = this.getMicroserviceHttpPrefix(MICROSERVICE_NAME_USER_RELATION, uid);
			byte[] followResponse = HttpClientUtil.post(httpPrefix + "user-relation/unfollow", args);			
			result = getObjectMapper().readValue(followResponse, Result.class);
		} else {
			result.code = 404;
			result.msg = "Couldn't find user: " + followee;
		}
		return result;
	}
	
	
	public Result sendmsg(String sessid, String sendto, String msg) throws Exception {
		Result result = new Result();
		long uid = getUidFromSessid(sessid);
		long sentouid = -1;
		
		sentouid = this.lookupUidByUniqueField("nickname", sendto);
		
		if (sentouid > 0 ) {
			if (this.getPublisher() != null && this.getPublisher().isOpen()) {
				this.getPublisher().publish(sentouid, msg);	
			} else {
				result.code = 503;
				result.msg = "Messges are temporarily not available.";
			}
		} else {
			result.code = 404;
			result.msg = "Couldn't find user: " + sendto;
		}
		return result;
	}
	
	
	
	public static void main(String[] args) throws Exception {
		DemoEngine engine = new DemoEngine();
		engine.init();
		// engine.f();
		engine.follow("", "");
	}
	
}
