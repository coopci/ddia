package coopci.ddia.b2c.renting;

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
import java.util.LinkedList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bson.Document;

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

import coopci.ddia.LoginResult;
import coopci.ddia.Result;
import coopci.ddia.SessionId;
import coopci.ddia.UidResult;
import coopci.ddia.results.ListResult;
import coopci.ddia.results.UserInfo;
import coopci.ddia.util.SessidPacker;
import coopci.ddia.util.Vcode;

public class Engine {
	
	String mongoConnStr = "mongodb://localhost:27017/";
	String mongodbDBName = "b2c_renting";     
	String mongodbDBCollModels = "models";    // 物品的型号。每一个型号对应一个mongodb文档。
	String mongodbDBCollItems = "items";          // 所有可被出借的物品。每一个物品对应一个mongodb文档。每个物品都应该有型号。
	
	// 记录各个用户押金和当前租借情况的collection。每个document对应一个用户。
	// _id是用户id
	// pledge 是 当前押金总额。
	// 
	String mongodbDBCollPledge = "renting"; 
	
	
	
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
	
	
	public void init() throws Exception {
		connectMongo();
		
		return;
	}
	
	LinkedList<Document> getMongoDocuments(String dbname, String collname, Document query, int skip, int limit) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		FindIterable<Document> iterable = collection.find(query).skip(skip).limit(limit);
		LinkedList<Document> ret = new LinkedList<Document>();
		MongoCursor<Document> cur = iterable.iterator();
		
		while(cur.hasNext()){
			ret.add(cur.next());
		}
		return ret;
	}
	
	Document getMongoDocumentById(String dbname, String collname, String id) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		Document filter = new Document();
		filter.append("_id", id);
		FindIterable<Document> iter = collection.find(filter);
		Document doc = iter.first();
		return doc;
	}
	Document getMongoDocumentById(String dbname, String collname, long id) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		Document filter = new Document();
		filter.append("_id", id);
		FindIterable<Document> iter = collection.find(filter);
		Document doc = iter.first();
		return doc;
	}

	void removeMongoDocumentById(String dbname, String collname, String id) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		Document filter = new Document();
		filter.append("_id", id);
		collection.deleteOne(filter);
		return;
	}
	
	// upsert : true
	UpdateResult saveMongoDocumentById(String dbname, String collname, Document data, String id) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		
		
		
		Document filter = new Document();
		filter.append("_id", id);
		
		Document update = new Document();
		update.append("$set", data);
		UpdateOptions opt = new UpdateOptions();
		opt.upsert(true);
		
		UpdateResult r = collection.updateOne(filter, update, opt);
		
		
		return r;
	}
	
	// upsert : false
	void updateMongoDocumentById(String dbname, String collname, Document data, String id) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		
		
		
		Document filter = new Document();
		filter.append("_id", id);
		
		Document update = new Document();
		update.append("$set", data);
		UpdateOptions opt = new UpdateOptions();
		opt.upsert(false);
		
		collection.updateOne(filter, update, opt);
		
		
		return;
	}
	
	public Result startRenting(long user_id, String item_id) {
		// user_id 要开始租用 item_id。
		// 检查押金，记录 "开始租借"。交付使用权。
		Result ret = new Result();
		Date now =  new Date();
		
		
		
		
		
		
		return ret;
	} 
	
	public Result finishRenting(long user_id, String item_id) {
		// user_id 要结束租用 item_id
		// 计算费用，记录 "结束租借"，从押金里扣除费用。收回使用权。
		Result ret = new Result();
		Date now =  new Date();
		
		return ret;
	} 
	
	/**
	 * 给uid表示的用户增加押金
	 * */
	public Result addPledge(long uid, long amount) {
		Result ret = new Result();
		Date now =  new Date();
		
		return ret;
	}
	
	
	/**
	 * 给uid表示的用户 冻结押金。 
	 * 只有在当前冻结量是0的情况下才能冻结。
	 * 这功能是 用户提现的时候用的。
	 * 
	 * 提现成功的流程是:
	 * 1、 freezePledge
	 * 2、 执行真正的支付操作(成功)。
	 * 3、 withdrawFrozen。
	 * 
	 * 提现失败的流程是:
	 * 1、 freezePledge
	 * 2、 执行真正的支付操作(失败)。
	 * 3、 cancelFrozen。
	 * */
	public Result freezePledge(long uid, long amount) {
		Result ret = new Result();
		Date now =  new Date();
		
		return ret;
	}
	
	/**
	 * 用于提现成功出账后，将冻结的押金从账户减去。见freezePledge的注释。
	 * 
	 * */
	public Result withdrawFrozen(long uid) {
		Result ret = new Result();
		Date now =  new Date();
		
		return ret;
	}
	
	
	/**
	 * 解冻 被冻结的 押金，用于 提现失败后 又暂时不想提现的情况。
	 * 
	 * */
	public Result cancelFrozen(long uid) {
		Result ret = new Result();
		Date now =  new Date();
		
		return ret;
	}
	
	
	
	
	
}
