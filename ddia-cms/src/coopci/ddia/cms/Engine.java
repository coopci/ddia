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
import com.mongodb.DBCursor;
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


// 每一个内容被称为一个item。 每个item可以有任意多个属性。
// 有一些属性是"强制的": create_time, appid(表示出数据哪一个应用  例如 gift), type(表示在appid指明的范围内的作用。 例如 album, article)。 
// item可以作为容器包含其他的item。 容器中的子item有顺序。任何一个item都可以属于多个容器。 容器可以包含容器。
// item可以有名字也可以没有名字。在有名字的情况下，相同owner_id的item的名字不能重复。
// 
public class Engine implements IMongodbAspect {
	
	String mongoConnStr = "mongodb://localhost:27017/";
	String mongodbDBName = "cms";     // mongodb的库名字
	String mongodbDBCollItems = "cms_item"; // 存 内容的collection。
	
	String mongodbDBCollContain = "cms_contain"; // 存 容器内容的collection。 container_id 是 容器item的_id, member_id 是成员item的_id, order是成员在容器中的顺序。 按container_id part。
	
	
	public static String FIELD_NAME_OWNER_ID = "owner_id";
	public static String FIELD_NAME_CREATE_TIME = "create_time";
	public static String FIELD_NAME_UPDATE_TIME = "update_time";
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

	
	
	public void initIndex() {
		Document fields = new Document();
		fields.append("owner_id", 1); 
		fields.append("name", 1);
		IndexOptions opt = new IndexOptions();
		opt.unique(true);
		opt.sparse(true);
		this.ensureIndex(this.mongodbDBName, this.mongodbDBCollItems, fields, opt);
		
	}
	public void init() throws Exception {
		connectMongo();
		this.initIndex();
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
	 * 	获取或创建uid名下 名为name的item。
	 *	每一个uid名下的item的name不能重复。
	 *  
	 *  @param uid 会被作为item的owner_id。
	 *  @param name 要创建或者获取的item的名字。
	 *  @param fields 要获取的字段。
	 * */
	public DictResult getOrCreateNamedItem(Long uid, String name, HashSet<String> fields) {
		DictResult res = new DictResult();
		Document query = new Document();
		
		query.append("owner_id", uid);
		query.append("name", name);
		LinkedList<Document> docs = this.getMongoDocuments(this.mongodbDBName, this.mongodbDBCollItems, query, 0, 1);
		
		if (docs.size() == 0) {
			// 创建一个新的。
			Date now = new Date();
			Document newdoc = new Document();
			newdoc.append("owner_id", uid);
			newdoc.append("name", name);
			newdoc.append("create_time", now);

			ObjectId oid = this.insertMongoDocument(this.mongodbDBName, this.mongodbDBCollItems, newdoc);
			this.put(res, newdoc, fields);
			res.put("id", oid.toHexString());
			return res;
		}
		Document doc = docs.getFirst();
		this.put(res, doc, fields);
		res.put("id", doc.getObjectId("_id").toHexString());
		return res;
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
	 *	content 中的 key如果以 set__开头，        那么表示要直接设置 set__后面部分为名字的字段的值为value； 	 
	 *  content 中的 key如果以  incrby__开头，那么表示要将 incrby后面部分为名字的字段的值数字增加value；
	 *  content 中的 key如果以  add__开头，那么表示要将  value作为元素添加到   add后面部分为名字所表示的 集合 中。 尚未实现。
	 *  
	 *  @param uid 必须和item的owner_id相同才允许这个操作。
	 * */
	public DictResult saveItem(Long uid, String item_id, HashMap<String, Object> content) {
		DictResult res = new DictResult();
		
		ObjectId oid = new ObjectId(item_id);
		Document filter = new Document();
		filter.append(FIELD_NAME_OWNER_ID, uid);
		filter.append("_id", oid);
		
		Document setData = new Document();
		Document incrbyData = new Document();
		
		
		for (Entry<String, Object> entry: content.entrySet()) {
			String k = entry.getKey();
			String fn = null;
			
			if (k.startsWith("set__")) {
				fn = k.substring("set__".length());
				setData.append(fn, entry.getValue());
			} else if (k.startsWith("incrby__")) {
				fn = k.substring("incrby__".length());
				Object v = entry.getValue(); 
				if( v instanceof String) {
					incrbyData.append(fn, 
							Long.parseLong((String)v)
							);
				} else if (v instanceof Long) {
					incrbyData.append(fn, 
							(Long)v);
				}
			} 
//			else if (k.startsWith("add__")) {
//				fn = k.substring("add__".length());
//			}
		}
		Document update = new Document();
		Date now = new Date();
		setData.append(FIELD_NAME_UPDATE_TIME, now);
		if (setData.size() > 0) 
			update.append("$set", setData);
		
		if (incrbyData.size() > 0) 
			update.append("$inc", incrbyData);
		
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollItems);
		
		
		UpdateOptions opt = new UpdateOptions();
		opt.upsert(false);
		
		UpdateResult ur = collection.updateOne(filter, update, opt);
		
		if(ur.getModifiedCount() == 0) {
			res.code = 404;
			res.msg = "No such item.";
			return res;
		}
		
		return res;
	}
	
	public Document getOneItem(Long uid) {
		
		Document query = new Document();
		query.append("owner_id", uid);
		Document doc = this.getOneMongoDocument(this.mongodbDBName, this.mongodbDBCollItems, query, 0, 1);
		return doc;
	}

	public Document getItem(String item_id) {
		
		Document doc = this.getMongoDocumentById(this.mongodbDBName, this.mongodbDBCollItems, new ObjectId(item_id));
		return doc;
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
		Document container = this.getItem(container_id);
		if (container == null) {
			res.code = 404;
			res.msg = "No such container.";
			return res;
		}
		
		Document member = this.getItem(member_id);
		if (member == null) {
			res.code = 404;
			res.msg = "No such member.";
			return res;
		}
		
		if (order < 0) {
			 // 找到已有的最大的order， 让新member的order = 目前最大的order + 1;
			 // 如果目前还没有member，则让order = 1。
			 MongoCollection<Document> coll = this.getMongoColl(this.mongodbDBName, this.mongodbDBCollContain);
			 Document filter = new Document();
			 filter.append("container_id", container_id);
			 MongoCursor<Document> cur = coll.find(filter).sort(new Document("order", -1)).iterator();
			 if (cur.hasNext()) {
				 Document last = cur.next();
				 order = last.getLong("order") + 1L;
			 } else {
				 order  = 1L;
			 }
		} else {
			Document inc = new Document();
			inc.append("order", 1L);
			Document update = new Document();
			update.append("$inc", inc);
			Document filter = new Document();
			filter.append("container_id", container_id);
			filter.append("order", new Document("$gte", order));
			
			MongoCollection<Document> coll = this.getMongoColl(this.mongodbDBName, this.mongodbDBCollContain);
			UpdateResult ur = coll.updateMany(filter, update);
		}
		
		Document newdoc = new Document();
		newdoc.append("container_id", container_id);
		newdoc.append("member_id", member_id);
		newdoc.append("order", order);
		this.insertMongoDocument(this.mongodbDBName, this.mongodbDBCollContain, newdoc);
		
		return res;
	}
	
	static class MemberLocator {
		ObjectId id;
		Long owner_id = -1L;
		Long order = -1L;
	}
	
	/**	
	 *	获取按成员id列表。
	 *  
	 *  @param start 只获取order大于等于start的成员。
	 *  @param limit 如果 limit > 0， 那么最多获取limit个成员。
	 * */
	public List<MemberLocator> getMembersLocatorlist(String container_id, int start, int limit) {
		LinkedList<MemberLocator> ret = new LinkedList<MemberLocator>();
		Document query = new Document();
		query.append("container_id", container_id);
		Document sort = new Document();
		sort.append("order", 1);
		LinkedList<Document> docs = this.getMongoDocuments(this.mongodbDBName, this.mongodbDBCollContain, query, sort,start, limit);
		for (Document doc : docs) {
			MemberLocator ml = new MemberLocator();
			
			Object memberid = doc.get("member_id");
			if (memberid instanceof String) {
				ml.id = new ObjectId((String)memberid);	
			} else if (memberid instanceof String) {
				ml.id = (ObjectId)memberid;
			}
			
			if (doc.containsKey("member_owner_id")) {
				ml.owner_id = doc.getLong("member_owner_id");
			}
			
			if (doc.containsKey("order")) {
				ml.order = doc.getLong("order");
			}
			ml.order = doc.getLong("order");
			ret.add(ml);
		}
		
		return ret;
	}
	
	
	

	public Document getItem(MemberLocator ml) {
		// TODO 如果 mongodbDBCollItems 是按owner_id part，那么这里还可以"优化" 一下。
		// TODO 另外还可以用内存cache"优化"一下。
		Document doc = this.getMongoDocumentById(this.mongodbDBName, this.mongodbDBCollItems, ml.id);
		return doc;
	}
	
	
	/**	
	 *	获取按成员顺序 获取成员的内容。
	 *  @param uid 检查权限用，而不是简单的筛选条件。
	 *  @param start 只获取order大于等于start的成员。
	 *  @param limit 如果 limit > 0， 那么最多获取limit个成员。
	 * */
	public ListResult getMembers(Long uid, String container_id, HashSet<String> fields, long start, long limit) {
		ListResult res = new ListResult();
		
		List<MemberLocator> memberlocs = this.getMembersLocatorlist(container_id, (int)start, (int)limit);
		for (MemberLocator ml : memberlocs) {
			Document item = this.getItem(ml);
			KVItem kvitem = new KVItem(); 
			this.put(kvitem, item, fields);
			kvitem.put("order", ml.order);
			kvitem.put("id", ml.id.toHexString());
			res.add(kvitem);
		}
		return res;
	}
	
	/**	
	 *	获取内容。
	 *
	 * @param uid 要获取内容用户的id。 检查权限用，而不是简单的筛选条件。 
	 * */
	public DictResult getItem(Long uid, String item_id, HashSet<String> fields) {
		DictResult res = new DictResult();
		Document item = getItem(item_id);
		if (item == null) {
			res.code = 404;
			res.msg = "No such item.";
			return res;
		}

		if (item.containsKey("private") && item.getLong(FIELD_NAME_OWNER_ID) != uid) {
			res.code = 403;
			res.msg = "Permission denied.";
			return res;
		}
		
		this.put(res, item, fields);
		res.put("id", item_id);
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
