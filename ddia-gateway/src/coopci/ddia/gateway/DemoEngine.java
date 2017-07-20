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

import coopci.ddia.Result;
import coopci.ddia.results.DictResult;
import coopci.ddia.results.ListResult;

public class DemoEngine extends Engine implements ICMSAspect {
	
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
	
	
	public Result buyDiamondsCreateOrder(String sessid, Long number, String payChannel) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		// Result res = new Result();
		Long uid = this.getUidFromSessid(sessid);
		String appid = "buy_diamonds";
		String virutalAssetsHttpPrefix = this.getMicroserviceHttpPrefix(MICROSERVICE_NAME_VIRTUAL_ASSETS, uid);
		HashMap<String, String> avArgs = new HashMap<String, String>();
		avArgs.put("uid", Long.toString(uid));
		avArgs.put("appid", appid);
		avArgs.put("va_diamonds", Long.toString(number));
		
		byte[] followResponse = HttpClientUtil.post(virutalAssetsHttpPrefix + "virtual-assets/create_purchase_order", avArgs);			
		DictResult avResult = getObjectMapper().readValue(followResponse, DictResult.class);
		
		
		
		
		String thirdPartyPayHttpPrefix = this.getMicroserviceHttpPrefix(MICROSERVICE_NAME_THIRD_PARTY_PAY, uid);
		HashMap<String, String> payArgs = new HashMap<String, String>();
		payArgs.put("uid", Long.toString(uid));
		payArgs.put("appid", appid);
		payArgs.put("desc", avResult.data.get("desc").toString());
		payArgs.put("apptranxid", avResult.data.get("apptranxid").toString());
		payArgs.put("total_amount", Double.toString(  (Double)avResult.data.get("totalAmount"))   );
		payArgs.put("pay_channel", payChannel );
		
		
		followResponse = HttpClientUtil.post(thirdPartyPayHttpPrefix + "pay/create_order", payArgs);			
		DictResult payResult = getObjectMapper().readValue(followResponse, DictResult.class);
		
		return payResult;
	}
	
	public Result buyDiamondsCheckOrder(String sessid, String apptranxid) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		// Result res = new Result();
		Long uid = this.getUidFromSessid(sessid);
		String appid = "buy_diamonds";
		
		// 调用 MICROSERVICE_NAME_THIRD_PARTY_PAY 的检查订单
		String thirdPartyPayHttpPrefix = this.getMicroserviceHttpPrefix(MICROSERVICE_NAME_THIRD_PARTY_PAY, uid);
		HashMap<String, String> payArgs = new HashMap<String, String>();
		payArgs.put("uid", Long.toString(uid));
		payArgs.put("appid", appid);
		payArgs.put("apptranxid", apptranxid);
		
		byte[] followResponse = HttpClientUtil.post(thirdPartyPayHttpPrefix + "pay/check_order", payArgs);			
		DictResult chkResult = getObjectMapper().readValue(followResponse, DictResult.class);
		
		
		// 第三方支付 的 status表示的是支付结果。
		String payResult = chkResult.data.get("status").toString();
		
		if (coopci.ddia.Consts.PAY_RESULT_NEW.equals(payResult)) {
			return chkResult;
		}
		
		// 调用 MICROSERVICE_NAME_VIRTUAL_ASSETS 的处理订单
		String virtualAssetsHttpPrefix = this.getMicroserviceHttpPrefix(MICROSERVICE_NAME_VIRTUAL_ASSETS, uid);
		HashMap<String, String> postprocArgs = new HashMap<String, String>();
		postprocArgs.put("uid", Long.toString(uid));
		postprocArgs.put("appid", appid);
		postprocArgs.put("apptranxid", apptranxid);
		postprocArgs.put("pay_result", payResult);
		
		followResponse = HttpClientUtil.post(virtualAssetsHttpPrefix + "virtual-assets/postprocess_purchase_order", postprocArgs);			
		DictResult postprocResult = getObjectMapper().readValue(followResponse, DictResult.class);
		
		return postprocResult;
	}
	
	
	/**
	 * 
	 * @param targetNickname 这个聊天消息的接受方的nickname
	 * */
	public Result sendChatMessage(String sessid, String targetNickname, String message) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InterruptedException, ExecutionException {
		Result result = new Result();
		long uid = getUidFromSessid(sessid);
		long targetuid = -1;
		
		targetuid = this.lookupUidByUniqueField("nickname", targetNickname);
		
		if (targetuid < 0 ) {
			result.code = 404;
			result.msg = "Couldn't find user: " + targetNickname;
			return result;
		}
		Date now = new Date();
		HashMap<String, String> args = new HashMap<String, String> (); 
		args.put("from", Long.toString(uid));
		args.put("to_uid", Long.toString(targetuid));
		args.put("message", message);
		args.put("time", now.toString());
		args.put("appid", "chat");
		
		String httpPrefix = this.getMicroserviceHttpPrefix(MICROSERVICE_NAME_CHAT, uid);
		byte[] followResponse = HttpClientUtil.post(httpPrefix + "chat/send_message", args);			
		result = getObjectMapper().readValue(followResponse, Result.class);
	
		return result;
	}
	
//	
//	public Result cmsRoot(String sessid, String fields, int start, int limit) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InterruptedException, ExecutionException {
//		ListResult result = new ListResult();
//		long uid = getUidFromSessid(sessid);
//		if (fields==null) {
//			fields = "";
//		}
//		String httpPrefix = this.getMicroserviceHttpPrefix(MICROSERVICE_NAME_CMS, uid);
//		HashMap<String, String> args = new HashMap<String, String> (); 
//		args.put("uid", Long.toString(uid));
//		args.put("fields", fields);
//		args.put("start", Integer.toString(start));
//		args.put("limit", Integer.toString(limit));
//		byte[] cmsRootResponse = HttpClientUtil.get(httpPrefix + "cms/get_global_named_items", args);	
//		result = getObjectMapper().readValue(cmsRootResponse, ListResult.class);
//		return result;
//	}
//		
//	
//	public Result getCmsItem(String sessid, String fields, String id) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InterruptedException, ExecutionException {
//		DictResult result = new DictResult();
//		long uid = getUidFromSessid(sessid);
//		if (fields==null) {
//			fields = "";
//		}
//		
//		String httpPrefix = this.getMicroserviceHttpPrefix(MICROSERVICE_NAME_CMS, id);
//		HashMap<String, String> args = new HashMap<String, String> (); 
//		args.put("uid", Long.toString(uid));
//		args.put("fields", fields);
//		args.put("id", id);
//		
//		byte[] cmsRootResponse = HttpClientUtil.get(httpPrefix + "cms/get_item", args);	
//		
//		result = getObjectMapper().readValue(cmsRootResponse, DictResult.class);
//		
//		
//		return result;
//	}
//		
//	public Result cmsMembers(String sessid, String container_id, String fields, int start, int limit) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InterruptedException, ExecutionException {
//		ListResult result = new ListResult();
//		long uid = getUidFromSessid(sessid);
//		if (fields==null) {
//			fields = "";
//		}
//		String httpPrefix = this.getMicroserviceHttpPrefix(MICROSERVICE_NAME_CMS, uid);
//		HashMap<String, String> args = new HashMap<String, String> (); 
//		args.put("uid", Long.toString(uid));
//		args.put("fields", fields);
//		args.put("container_id", container_id);
//		args.put("start", Integer.toString(start));
//		args.put("limit", Integer.toString(limit));
//		byte[] cmsResponse = HttpClientUtil.get(httpPrefix + "cms/get_members", args);	
//		result = getObjectMapper().readValue(cmsResponse, ListResult.class);
//		return result;
//	}
	
	public static void main(String[] args) throws Exception {
		DemoEngine engine = new DemoEngine();
		engine.init();
		// engine.f();
		engine.follow("", "");
	}
	
}
