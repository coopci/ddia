package coopci.ddia.cms;

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
import coopci.ddia.results.UserInfo;
import coopci.ddia.util.SessidPacker;
import coopci.ddia.util.Vcode;


// 每一个内容被称为一个item。 每个item可以有任意多个属性。
// 有一些属性是"强制的": create_time, appid(表示出数据哪一个应用  例如 gift), type(表示在appid指明的范围内的作用。 例如 album, article)。 
// item可以作为容器包含其他的item。 容器中的子item有顺序。任何一个item都可以属于多个容器。 容器可以包含容器。
// 
public class Engine implements IMongodbAspect {
	
	String mongoConnStr = "mongodb://localhost:27017/";
	String mongodbDBName = "cms";     // mongodb的库名字
	String mongodbDBCollItems = "cms_item"; // 存 内容的collection。
	
	String mongodbDBCollContain = "cms_contain"; // 存 容器内容的collection。 container_id 是 容器item的_id, member_id 是成员item的_id, order是成员在容器中的顺序。
	
	
	public static String FIELD_NAME_OWNER_ID = "owner_id";
	public static String FIELD_NAME_CREATE_TIME = "create_time";
	
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
		
		return;
	}
	
	
	public static void main(String[] args) throws Exception {
		
		return;
		
		
	}
	
	
	
	static HashSet<String> blockFields = new HashSet<String>();
	static {
		blockFields.add(FIELD_NAME_OWNER_ID);
		blockFields.add("id");
		blockFields.add("_id");
		blockFields.add("create_time");
		blockFields.add("update_time");
		
	}
	/**
	 * 	向 mongodbDBCollItems 增加新内容。
	 *	content表示要初始设置的字段。
	 *  @param uid 会被作为item的owner_id。
	 * */
	public DictResult createItem(Long uid, HashMap<String, Object> content) {
		DictResult res = new DictResult();
		Date now = new Date();
		Document doc = new Document();
		if (content != null) {
			for (Entry<String, Object> entry : content.entrySet()) {
				if (blockFields.contains(entry.getKey()))
					continue;
				doc.append(entry.getKey(), entry.getValue());
				res.put(entry.getKey(), entry.getValue());
			}
		}
		doc.append(FIELD_NAME_OWNER_ID, uid);
		res.put(FIELD_NAME_OWNER_ID, uid);
		doc.append(FIELD_NAME_CREATE_TIME, now);
		res.put(FIELD_NAME_CREATE_TIME, now);
		
		
		ObjectId oid = this.insertMongoDocument(this.mongodbDBName, this.mongodbDBCollItems, doc);
		
		res.put("id", oid.toHexString());
		
		return res;
	}
	

	/**
	 * 	更新item_id指定的item的内容。
	 *	content 中的 key如果以  __set结尾，        那么表示要直接设置 __set前面部分为名字的字段的值为value； 	 
	 *  content 中的 key如果以  __incrby结尾，那么表示要将 __incrby前面部分为名字的字段的值数字增加value；
	 *  content 中的 key如果以  __add结尾，那么表示要将  value作为元素添加到     __add前面部分为名字所表示的 集合 中。 尚未实现。
	 *  
	 *  @param uid 必须和item的owner_id相同才允许这个操作。
	 * */
	public Result saveItem(Long uid, String item_id, HashMap<String, Object> content) {
		Result res = new Result();
		
		return res;
	}
	
	

	/**
	 * 	把member_id表示的内容放入container_id表达的容器。 mongodbDBCollContain。
	 *  
	 *	
	 *  @param uid uid必须和container的owner_id相同才允许这个操作。
	 *  @param order 如果order<0， 则放在最后面，否则房子order指定的位置，并把原来 排在order和order以后的成员都向后挪。
	 * */
	public Result setContainer(Long uid, String member_id, String container_id, long order) {
		Result res = new Result();
		
		return res;
	}
	

	/**	
	 *	获取内容。
	 *
	 * @param uid 检查权限用，而不是简单的筛选条件。 
	 * */
	public Result getItem(Long uid, String item_id, HashSet<String> fields) {
		Result res = new Result();
		
		return res;
	}
	
	/**	
	 *	获取按成员顺序获取成员的内容。
	 *  @param uid 检查权限用，而不是简单的筛选条件。
	 *  @param start 只获取order大于等于start的成员。
	 *  @param limit 如果 limit > 0， 那么最多获取limit个成员。
	 * */
	public Result getMembers(Long uid, String container_id, HashSet<String> fields, long start, long limit) {
		Result res = new Result();
		
		return res;
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
	
	@Override
	public String getMongoConnStr() {
		return mongoConnStr;
	}
}
