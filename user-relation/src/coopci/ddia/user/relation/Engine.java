package coopci.ddia.user.relation;

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
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;

import coopci.ddia.LoginResult;
import coopci.ddia.Result;
import coopci.ddia.SessionId;
import coopci.ddia.UidResult;
import coopci.ddia.UserInfosResult;
import coopci.ddia.util.SessidPacker;
import coopci.ddia.util.Vcode;

public class Engine {
	
	String mongoConnStr = "mongodb://localhost:27017/";
	String mongodbDBName = "user_relation";     // mongodb的库名字
	String mongodbDBCollFollows = "follows";    // 记录关注关系的collection， uid是关注别人的人， followee是被uid关注的人, _id是 uid=>followee, mutual表示是否为互相关注。  按uid做sharding。 对uid索引; 对mutual索引。
	String mongodbDBCollFans = "fans";          // 记录关注关系的collection， uid是被关注的人，     fan是关注uid的人,        _id是 uid=>fan。							 按uid做sharding。  对uid索引。
	
	
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
	
	
	void saveMongoDocumentById(String dbname, String collname, Document doc, String id) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		doc.append("_id", id);
		try {
			collection.insertOne(doc);
		} catch (com.mongodb.MongoWriteException ex) {
			// https://github.com/mongodb/mongo/blob/master/src/mongo/base/error_codes.err
			if (ex.getCode() == 11000 ) {
				// 11000 表示 DuplicateKey
				return;
			} else {
				throw ex;
			}
		}
		
		return;
	}
	
	
	public Result follow(long fan, long followee) {
		Result ret = new Result();
		
		String coll_follows_id = Long.toString(fan) + "=>" + Long.toString(followee);
		String coll_fan_id = Long.toString(followee) + "=>" + Long.toString(fan);
		
		Document docFollow = new Document();
		docFollow.append("_id", coll_follows_id);
		docFollow.append("uid", fan);
		docFollow.append("followee", followee);
		docFollow.append("mutual", false);

		this.saveMongoDocumentById(this.mongodbDBName, this.mongodbDBCollFollows, docFollow, coll_follows_id);
		
		
		Document docFan = new Document();
		docFan.append("fan", fan);
		docFan.append("uid", followee);
		docFan.append("_id", coll_fan_id);
		this.saveMongoDocumentById(this.mongodbDBName, this.mongodbDBCollFans, docFan, coll_fan_id);
		
		
		String coll_follows_id_reverse = Long.toString(followee) + "=>" + Long.toString(fan);
		Document doc = this.getMongoDocumentById(this.mongodbDBName, this.mongodbDBCollFollows, coll_follows_id_reverse);
		if (doc != null) {
			// 说明是互相关注
			MongoClient client = this.getMongoClient();
			MongoDatabase db = client.getDatabase(this.mongodbDBName);
			MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollFollows);
			
			
			Document filter = new Document();
			filter.append("_id", coll_follows_id);
			Document update = new Document();
			update.append("$set", new Document("mutual", true));
			
			
			collection.updateOne(filter, update);
			
			
			Document filter2 = new Document();
			filter2.append("_id", coll_follows_id_reverse);
			
			
			
			collection.updateOne(filter2, update);
		}
		
		
		return ret;
	} 
	
	
	public Result unfollow(long fan, long followee) {
		Result ret = new Result();
		
		String coll_follows_id = Long.toString(fan) + "=>" + Long.toString(followee);
		String coll_fan_id = Long.toString(followee) + "=>" + Long.toString(fan);
		String coll_follows_id_reverse = Long.toString(followee) + "=>" + Long.toString(fan);
		
		
		this.removeMongoDocumentById(this.mongodbDBName, this.mongodbDBCollFollows, coll_follows_id);
		this.removeMongoDocumentById(this.mongodbDBName, this.mongodbDBCollFans, coll_fan_id);
		
		
		
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollFollows);
		Document filter = new Document();
		filter.append("_id", coll_follows_id_reverse);
		Document update = new Document();
		update.append("$set", new Document("mutual", false));
		collection.updateOne(filter, update);
		
		
		
		return ret;
	}
	
	public Result getFollowsList(long uid, int start, int end) {
		throw new UnsupportedOperationException();
		// Result ret = new Result();
		// return ret;
	}
	
	
	public Result getFansList(long uid, int start, int end) {
		throw new UnsupportedOperationException();
		// Result ret = new Result();
		// return ret;
	}
	
	
	public Result getMutualFollowsList(long uid, int start, int end) {
		throw new UnsupportedOperationException();
		// Result ret = new Result();
		// return ret;
	}
}
