package coopci.ddia.chat;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;





import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

import coopci.ddia.Consts;
import coopci.ddia.IMongodbAspect;
import coopci.ddia.LoginResult;
import coopci.ddia.Result;
import coopci.ddia.SessionId;
import coopci.ddia.UidResult;
import coopci.ddia.notify.IPublisher;
import coopci.ddia.notify.rabbitmq.RabbitmqPublisher;
import coopci.ddia.results.DictResult;
import coopci.ddia.results.ListResult;
import coopci.ddia.results.UserInfo;
import coopci.ddia.util.SessidPacker;
import coopci.ddia.util.Vcode;

public class Engine implements IMongodbAspect {
	
	String mongoConnStr = "mongodb://localhost:27017/";
	String mongodbDBName = "virtual_assets";     // mongodb的库名字
	String mongodbDBCollAssets = "assets";    // 记录资产的collection， _id是uid。 以"va_"开头的字段表示一中虚拟资产。
	String mongodbDBCollTransferTranx = "transfer_tranx";    // 记录转帐事务的。
	
	
	String mongodbDBCollPurchaseOrders = "purchase_orders";    // 记录购买虚拟资产的订单。 pay_result记录支付结果，status记录的是这个订单的状态。
	
	
	public static String PURCHASE_ORDER_STATUS_NEW = "new";  // 表示是新订单。
	public static String PURCHASE_ORDER_STATUS_DONE = "done";  // 表示已经处理完毕。
															   // pay_result 字段的结果是paid， 那么表示要买的东西已经给到uid的assets里了。
	
	public static String FIELD_NAME_PENDING_TRANSFER_TRANX = "pending_transfer_tranx";
	public static String TRANSFER_TRANX_STATUS_NEW = "new";
	public static String TRANSFER_TRANX_STATUS_APPLIED = "applied";
	public static String TRANSFER_TRANX_STATUS_DONE = "done";
	
	// 据http://mongodb.github.io/mongo-java-driver/2.13/getting-started/quick-tour/ 说:
	// The MongoClient class is designed to be thread safe and shared among threads. Typically you create only 1 instance for a given database cluster and use it across your application.
	MongoClient mongoClient = null;
	// mongodb://host:27017/?replicaSet=rs0&maxPoolSize=200
	
	@Override
	public void setMongoClient(MongoClient mc) {
		this.mongoClient = mc;
	}
	@Override
	public MongoClient getMongoClient() {
		return mongoClient;
	}

	
	public void init() throws Exception {
		connectMongo();
		initPublisher();
		return;
	}
	IPublisher publisher = null;

	public IPublisher getPublisher(){
		return this.publisher;
	}
	
	
	public void initPublisher() throws Exception {
		publisher = new RabbitmqPublisher();
		publisher.start();
	}
	/**
	 * 发聊天信息给指定用户
	 * @throws Exception 
	 * */
	public Result sendMessage(long fromuid, long touid, String message) throws Exception {
		
		Result res = new Result();
		this.getPublisher().publish(touid, message);
		return res;
	}
	
	/**
	 * 发聊天信息给指定用户
	 * @throws Exception 
	 * */
	public Result sendMessage(long touid, HashMap<String, String> args) throws Exception {
		Result res = new Result();
		ObjectMapper objectMapper = new ObjectMapper();
		String message = objectMapper.writeValueAsString(args);
		this.getPublisher().publish(touid, message);
		return res;
	}
	
	/**
	 * 把args指出的资产变动 应用到对uid名下。
	 * 保证原子性， 保证不因为这个事务导致任何资产的数量小于0。
	 * 典型应用: 单个用户与系统之间的交易。系统赠与用户奖励。
	 * 
	 * @param args 指明的资产数 可正可负。
	 * */
	public Result incrby(String appid, String apptranxid, long uid, HashMap<String, Long> args) {
		Result res = new Result();
		
		Document filter = new Document();
		filter.append("_id", uid);
		
		boolean includeDecrease = false;
		for (Entry<String, Long> entry : args.entrySet()) {
			if (entry.getValue() < 0 ) {
				filter.append(entry.getKey(), new Document("$gte", entry.getValue()));
				includeDecrease = true;
			}
		}
		
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(mongodbDBCollAssets);
		
		Document updateValues = new Document();
		for (Entry<String, Long> entry : args.entrySet()) {
			updateValues.append(entry.getKey(), entry.getValue());
		}
		Document update = new Document();
		update.append("$inc", updateValues);
		UpdateOptions opt = new UpdateOptions();
		if (!includeDecrease)
			opt.upsert(true);
		else
			opt.upsert(false);
		
		UpdateResult ur = collection.updateOne(filter, update, opt);
		
		if (ur.getUpsertedId() == null && ur.getModifiedCount() == 0) {
			// 说明有 资产的数量不足。
			res.code = 402;
		}
		
		return res;
	}
	
	
	Result checkAllValuesPositive(HashMap<String, Long> args) {
		Result res = new Result();
		for (Entry<String, Long> entry : args.entrySet()) {
			if (entry.getValue() < 0 ) {
				res.setError(400, "Every value must be greater than 0, but " + entry.getKey() + " is not.");
				return res;
			}
		}
		
		
		return res;
	}
	
	
	public static Document NON_EXISTS_FILTER = new Document( "$exists", false);
	public static Document EXISTS_FILTER = new Document( "$exists", true);
	
	
	
	public static void main(String[] args) throws Exception {
		Engine engine = new Engine();
		
		engine.init();
		
		return;
		
		
	}
	
	
	@Override
	public String getMongoConnStr() {
		return mongoConnStr;
	}
}
