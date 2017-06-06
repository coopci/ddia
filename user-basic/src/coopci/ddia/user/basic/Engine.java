package coopci.ddia.user.basic;

import java.util.UUID;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import coopci.ddia.LoginResult;
import coopci.ddia.Result;

public class Engine {
	String mongoConnStr = "mongodb://localhost:27017/";
	String mongodbDBName = "user_basic";     // mongodb的库名字
	String mongodbDBCollVcode = "sms_vcode";    // 存短信验证码的collection, _id是手机号。
	String mongodbDBCollUserInfo = "user_info"; // 存用户信息的collection, _id是用户id。
	
	String mongodbDBCollCounters = "counters"; // 用来维护 自增的 用户id。
	
	// 据http://mongodb.github.io/mongo-java-driver/2.13/getting-started/quick-tour/ 说:
	// The MongoClient class is designed to be thread safe and shared among threads. Typically you create only 1 instance for a given database cluster and use it across your application.
	MongoClient mongoClient = null;
	// mongodb://host:27017/?replicaSet=rs0&maxPoolSize=200
	public void connectMongo() {
		try{
			
			if (mongoConnStr == null) {
				//logger.error("mongoConnStr == null in connectMongo.");
				this.mongoClient = null;
				return;
			}
			if (mongoConnStr.length() == 0) {
				//logger.error("mongoConnStr.length() == 0 in connectMongo.");
				this.mongoClient = null;
				return;
			}
			MongoClientURI uri = new MongoClientURI(mongoConnStr,
					MongoClientOptions.builder().cursorFinalizerEnabled(false));
			mongoClient = new MongoClient(uri);
		} catch(Exception ex) {
			// logger.error("Exception in connectMongo, mongoConnStr = {} ", mongoConnStr, ex);
		}
	}
	public MongoClient getMongoClient() {
		return mongoClient;
	}

	public void close() {
		if (this.mongoClient == null)
			return;
		this.mongoClient.close();
	}
	
	
	public void initUseridSeq() {
		
		MongoClient client = this.getMongoClient();
		
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollCounters);
		
		Document doc = new Document();
		doc.append("_id", "userid");
		doc.append("seq", 0);
		try {
			collection.insertOne(doc);
		} catch (com.mongodb.MongoWriteException ex) {
			// https://github.com/mongodb/mongo/blob/master/src/mongo/base/error_codes.err
			if (ex.getCode() == 11000 ) {
				// 11000 表示 DuplicateKey
			} else {
				throw ex;
			}
		}
		return;
		
	}
	public void init() {
		connectMongo();
		
		initUseridSeq();
		
		
		saveSmsVcode("34", "234");
		return;
	}
	
	public void saveSmsVcode(String vcode, String phone) {
		MongoClient client = this.getMongoClient();
		
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollVcode);
		
		Document filter = new Document();
		filter.append("_id", phone);
		
		Document update = new Document();
		update.append("$set", new Document("vcode", vcode));
		
		UpdateOptions opt = new UpdateOptions();
		opt.upsert(true);
		
		collection.updateOne(filter, update, opt);
		return;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public Result loginSubmitPhone(String phone) {
		Result res = new Result();
		String vcode = "111111";
		// TODO 生成一个 随机验证码 赋给vcode。

		this.saveSmsVcode(vcode, phone);
		// TODO 把vcode用短信发到用户手机上。
		// 
		
		return res;
	}
	
	public boolean checkVcode(String phone, String vcode) {
		// TODO 做检查。
		return false;
	}
	
	public void login(String phone, String sessid) {
		// TODO 执行登陆。
		
		return;
	}
	public LoginResult loginSubmitVcode(String phone, String vcode) {
		LoginResult res = new LoginResult();
		
		boolean checkResult = this.checkVcode(phone, vcode);
		if (!checkResult) {
			res.code = 400;
			res.msg = "提交的短信验证码有误。";
			return res;
		} 
		
		String sessid = UUID.randomUUID().toString();
		this.login(phone, sessid);
		res.sessid = sessid;
		return res;
	}
}
