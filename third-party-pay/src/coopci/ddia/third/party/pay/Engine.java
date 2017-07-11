package coopci.ddia.third.party.pay;

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

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

import coopci.ddia.IMongodbAspect;
import coopci.ddia.LoginResult;
import coopci.ddia.Result;
import coopci.ddia.SessionId;
import coopci.ddia.UidResult;
import coopci.ddia.requests.CheckOrderRequest;
import coopci.ddia.requests.CreateOrderRequest;
import coopci.ddia.results.DictResult;
import coopci.ddia.results.ListResult;
import coopci.ddia.results.KVItem;
import coopci.ddia.util.SessidPacker;
import coopci.ddia.util.Vcode;

public class Engine implements IMongodbAspect {
	
	String mongoConnStr = "mongodb://localhost:27017/";
	String mongodbDBName = "third_party_pay";     // mongodb的库名字
	String mongodbDBCollOrders = "orders";    // appid 和 apptranxid 联合唯一, 按 uid shard
	
	
	public static String PAY_CHANNEL_BACKDOOR = "backdoor";
	public static String PAY_CHANNEL_WEIXIN = "weixin";
	
	
	//public static String ORDER_STATUS_NEW = "new";
	//public static String ORDER_STATUS_PAID = "paid";
	//public static String ORDER_STATUS_FAILED = "failed";
	
	
	// 据http://mongodb.github.io/mongo-java-driver/2.13/getting-started/quick-tour/ :
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

		Document fields = new Document();
		fields.append("appid", 1);
		fields.append("apptranxid", 1);
		IndexOptions opt = new IndexOptions();
		opt.unique(true);
		this.ensureIndex(this.mongodbDBName, this.mongodbDBCollOrders, fields, opt);

		
		fields = new Document();
		fields.append("uid", 1);
		opt = new IndexOptions();
		opt.unique(false);
		this.ensureIndex(this.mongodbDBName, this.mongodbDBCollOrders, fields, opt);
		
		
		
		return;
	}
	
	
	public static void main(String[] args) throws Exception {
		
		return;
		
		
	}
	

	
	public Result checkOrder(CheckOrderRequest req){
		
		
		DictResult res = new DictResult();
		
		Document doc = this.lookupOrder(req);
		
		if (doc == null) {
			res.code = 404;
			res.msg = "No such order.";
			return res;
		}
		docToDictResult(res, doc);
		
		return res;
	}
	public Result createOrder(CreateOrderRequest req){
		DictResult res = new DictResult();
		
		if (PAY_CHANNEL_BACKDOOR.equals(req.payChannel)) {
			return this.backdoorCreateOrder(req);
		} else if(PAY_CHANNEL_WEIXIN.equals(req.payChannel)) {
			return this.weixinCreateOrder(req);
		}  
		res.code = 400;
		res.msg = "Unsupported pay channel: " + req.payChannel;
		return res;
	}
	
	
	public Result weixinCheckOrder(CheckOrderRequest req){
		Result res = new Result();
		return res;
	}
	public Result weixinCreateOrder(CreateOrderRequest req){
		Result res = new Result();
		return res;
	}
	
	
	
	
	public Document lookupOrder(CheckOrderRequest req) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollOrders);
		Document filter = new Document();
		filter.append("uid", req.uid);
		filter.append("appid", req.appid);
		filter.append("apptranxid", req.apptranxid);
		
		FindIterable<Document> iter = collection.find(filter);
		Document doc = iter.first();
		return doc;
	}
	
	
	
	public static void docToDictResult(DictResult res, Document doc) {

		for (Entry<String, Object> entry: doc.entrySet()) {
			if (entry.getKey().equals("uid"))
				continue;
			if (entry.getKey().equals("_id"))
				continue;
			res.put(entry.getKey(), entry.getValue());
		}
		
	}
	
	public Result backdoorCreateOrder(CreateOrderRequest req){
		DictResult res = new DictResult();
		Date now = new Date();
		Document doc = new Document();
		doc.append("uid", req.uid);
		doc.append("pay_channel", req.payChannel);
		doc.append("appid", req.appid);
		doc.append("apptranxid", req.apptranxid);
		doc.append("total_amount", req.totalAmount);
		doc.append("desc", req.desc);
		doc.append("created_time", now);
		doc.append("status", coopci.ddia.Consts.PAY_RESULT_PAID);
		this.insertMongoDocument(this.mongodbDBName, this.mongodbDBCollOrders, doc);
		
		docToDictResult(res, doc);
		
		return res;
	}
	
	@Override
	public String getMongoConnStr() {
		return mongoConnStr;
	}
}
