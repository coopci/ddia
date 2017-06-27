package coopci.ddia.virtual.assets;

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
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

import coopci.ddia.IMongodbAspect;
import coopci.ddia.LoginResult;
import coopci.ddia.Result;
import coopci.ddia.SessionId;
import coopci.ddia.UidResult;
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
		return;
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
	/**
	 *	从fromuid名下减少 fromassets指出的资产，给touid名下增加toassets指出的资产。
	 *  保证原子性。
	 *  保证不因为这个事务导致fromuid名下任何资产的数量小于0。
	 *  典型应用: 两个用户之间交易。两个用户之间转移资产。
	 *  
	 *  
	 *  @param fromassets 指明的资产数 必须是正的。
	 *  @param toassets 指明的资产数 必须是正的。
	 *  @param appid   表示产生这笔事务的app，例如用"gift"表示给主播送礼的功能。
	 *  @param apptranxid  这个笔事务在app内的事务id。用来以后记录和查找。
	 *  
	 * */
	public Result transfer(String appid, String apptranxid, long fromuid, HashMap<String, Long> fromassets, long touid, HashMap<String, Long> toassets) {
		Result res = new Result();
		res.code = 200;
		if (fromuid < 0) {
			res.setError(400, "from_uid must be greater than 0.");
		}
		
		if (touid < 0) {
			res.setError(400, "to_uid must be greater than 0.");
		}
		
		if (fromassets.size() == 0) {
			res.setError(400, "At least 1 from_assets must be specified.");
		}
		
		if (toassets.size() == 0) {
			res.setError(400, "At least 1 to_assets must be specified.");
		}
		if (res.code != 200)
			return res;
		res = checkAllValuesPositive(fromassets);
		if (res.code != 200)
			return res;
		res = checkAllValuesPositive(toassets);
		if (res.code != 200)
			return res;
		
		
		String tranxid = generateTransferTranx(appid, apptranxid, fromuid, fromassets, touid, toassets);
		
		res = execTransferTranx(tranxid);
		return res;
	}
	
	/**
	 * 产生一个 uid之间转移资产的事务记录。
	 * */
	public String generateTransferTranx(String appid, String apptranxid, long fromuid, HashMap<String, Long> fromassets, long touid, HashMap<String, Long> toassets) {
		Date now = new Date();
		Document doc = new Document();
		doc.append("appid", appid);
		doc.append("apptranxid", apptranxid);
		doc.append("status", TRANSFER_TRANX_STATUS_NEW);
		doc.append("from_uid", fromuid);
		doc.append("to_uid", touid);
		doc.append("from_assets", fromassets);
		doc.append("to_assets", toassets);
		doc.append("create_time", now);
		
		ObjectId oid = insertMongoDocument(mongodbDBName, mongodbDBCollTransferTranx, doc);
		return oid.toHexString();
	}
	

	void unsetTransferTranx(String tranx_id, long uid) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollAssets);
		
		Document filter = new Document();
		filter.append("_id", uid);
		
		Document update = new Document();
		update.append("$unset", new Document(FIELD_NAME_PENDING_TRANSFER_TRANX + "." + tranx_id, ""));
		UpdateOptions opt = new UpdateOptions();
		opt.upsert(false);
		
		collection.updateOne(filter, update, opt);
		return;
	}

	// upsert : false
	void setTransferTranxStatus(ObjectId id, String newStatus) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollTransferTranx);
		
		Document filter = new Document();
		filter.append("_id", id);
		
		Document data = new Document();
		data.append("status", newStatus);
		Document update = new Document();
		update.append("$set", data);
		UpdateOptions opt = new UpdateOptions();
		opt.upsert(false);
		
		collection.updateOne(filter, update, opt);
		return;
	}
	
	public static Document NON_EXISTS_FILTER = new Document( "$exists", false);
	public static Document EXISTS_FILTER = new Document( "$exists", true);
	
	protected Document getUserAssetById(long id) {
		return this.getMongoDocumentById(this.mongodbDBName, this.mongodbDBCollAssets, id);
	}
	
	public boolean hasPendingTranx(Document doc, String hextranxid) {
		if (doc == null)
			return false;
		
		Document pendingDict = (Document)doc.get(FIELD_NAME_PENDING_TRANSFER_TRANX);
		if (pendingDict == null)
			return false;
		
		if (!pendingDict.containsKey(hextranxid))
			return false;
		
		return true;
	}
	
	//	对 from_uid的资产进行改变，并设置pending_tranx。如果本事务已经在fromuid的pending_tranx里，就跳过这步。 
	//			如果fromuid名下资产不足，则将本事务状态设置为aborted。
	//		对 to_uid的资产进行改变，并设置pending_tranx。前提是本事务当前不在fromuid的pending_tranx里，若在，就跳过这步。
	public Result applyTransferFromStep(Document tranx) {
		Result res = new Result();
		String hextranxid = tranx.getObjectId("_id").toHexString();
		Long fromuid = tranx.getLong("from_uid");
		
		Document filter = new Document();
		filter.append("_id", fromuid);
		filter.append(FIELD_NAME_PENDING_TRANSFER_TRANX + "." + hextranxid, NON_EXISTS_FILTER );
		
		
		Document from_assets = (Document) tranx.get("from_assets");
		
		Document updateValue = new Document();
		for (Entry<String, Object> entry : from_assets.entrySet()) {
			String key = entry.getKey();
			Long value = from_assets.getLong(key);
			
			updateValue.append(key, -value);
			
			filter.append(key, new Document("$gte", value));
		}
		
		
		
		Document update = new Document();
		update.append("$inc", updateValue);
		update.append("$set", new Document(FIELD_NAME_PENDING_TRANSFER_TRANX + "." + hextranxid, new Date()));
		
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollAssets);
		
		UpdateOptions opt = new UpdateOptions();
		opt.upsert(false);
		
		UpdateResult ur = collection.updateOne(filter, update, opt);
		if (ur.getModifiedCount() == 0) {
			// 扣钱失败。
			// TODO 把this.mongodbDBCollAssets里的doc读出来看 失败的原因是什么。
			Document doc = this.getMongoDocumentById(this.mongodbDBName, this.mongodbDBCollAssets, fromuid);
			if (doc==null) {
				res.code = 404;
				res.msg = "No such account.";
				return res;
			} else if (hasPendingTranx(doc, hextranxid)){
				// 上次执行被打断，实际已经执行成功了。
				return res;
			} else {
				
				res.code = 402;
				String msg = "Insufficient assets: ";
				for (Entry<String, Object> entry : from_assets.entrySet()) {
					String key = entry.getKey();
					Long value = from_assets.getLong(key);
					Long valueInAccount = doc.getLong(key);
					if (valueInAccount == null)
						valueInAccount = 0L;
					if (value > valueInAccount) {
						msg += key + ", ";
					}
				}
				res.msg = msg;
				return res;
			}
		}
		return res;
	}
	

	public int applyTransferToStep(Document tranx) {
		String hextranxid = tranx.getObjectId("_id").toHexString();
		Long touid = tranx.getLong("to_uid");
		
		Document filter = new Document();
		filter.append("_id", touid);
		filter.append(FIELD_NAME_PENDING_TRANSFER_TRANX + "." + hextranxid, NON_EXISTS_FILTER );
		
		
		Document updateValue = (Document) tranx.get("to_assets");
		Document update = new Document();
		update.append("$inc", updateValue);
		update.append("$set", new Document(FIELD_NAME_PENDING_TRANSFER_TRANX + "." + hextranxid, new Date()));
		
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollAssets);
		
		UpdateOptions opt = new UpdateOptions();
		opt.upsert(true);
		
		collection.updateOne(filter, update, opt);
		return 0;
	}
	/**
	 * 执行  uid之间转移资产的事务。
	 * */
	public Result execTransferTranx(String hextranxid) {
		Result res = new Result();
		
		ObjectId tranxid = new ObjectId(hextranxid);
		Document tranx = getMongoDocumentById(this.mongodbDBName, this.mongodbDBCollTransferTranx, tranxid);
		if (tranx == null) {
			res.code = 404;
			res.msg = "No such tranx.";
			return res;
		} 
		String status = tranx.getString("status");
		Long fromuid = tranx.getLong("from_uid");
		Long touid = tranx.getLong("to_uid");
		if (TRANSFER_TRANX_STATUS_NEW.equals(status)) {
			
			Result result = applyTransferFromStep(tranx);
			if (result.code != 200) {
				return result;
			}
			applyTransferToStep(tranx);
			
			
			
			// 		把事务状态设置为applied。
			setTransferTranxStatus(tranxid, TRANSFER_TRANX_STATUS_APPLIED);
			unsetTransferTranx(hextranxid, fromuid);
			unsetTransferTranx(hextranxid, touid);
			setTransferTranxStatus(tranxid, TRANSFER_TRANX_STATUS_DONE);
			
		} else if (TRANSFER_TRANX_STATUS_APPLIED.equals(status)) {
			//		把本事务从 fromuid的pending_tranx里删掉。
			//		把本事务从 touid的pending_tranx里删掉。
			// 		把事务状态设置为done。
			unsetTransferTranx(hextranxid, fromuid);
			unsetTransferTranx(hextranxid, touid);
			setTransferTranxStatus(tranxid, TRANSFER_TRANX_STATUS_DONE);
			
		} else if (TRANSFER_TRANX_STATUS_DONE.equals(status)) {
			//		把本事务从 fromuid的pending_tranx里删掉。
			//		把本事务从 touid的pending_tranx里删掉。
			unsetTransferTranx(hextranxid, fromuid);
			unsetTransferTranx(hextranxid, touid);
			
		}  
		
		// 如果事务状态是new， 则要
		//		对 from_uid的资产进行改变，并设置pending_tranx。如果本事务已经在fromuid的pending_tranx里，就跳过这步。 
		//			如果fromuid名下资产不足，则将本事务状态设置为aborted。
		//		对 to_uid的资产进行改变，并设置pending_tranx。前提是本事务当前不在fromuid的pending_tranx里，若在，就跳过这步。
		// 		把事务状态设置为applied。
		// 如果事务状态是applied， 则要
		//		把本事务从 fromuid的pending_tranx里删掉。
		//		把本事务从 touid的pending_tranx里删掉。
		// 		把事务状态设置为done。
		// 如果事务状态是done， 则要
		//		把本事务从 fromuid的pending_tranx里删掉。
		//		把本事务从 touid的pending_tranx里删掉。
		
		
		return res;
		
	}
	
	
	
	protected void insertAssetsDocForUid(long uid) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollAssets);
		Document data = new Document();
		data.append("_id", uid);
		collection.insertOne(data);
		return ;
	} 
	
	public static void main(String[] args) throws Exception {
		Engine engine = new Engine();
		
		engine.init();
		
		
		String appid = "test-appid";
		String apptranxid = "test-apptranxid-" + UUID.randomUUID().toString();
		
		long fromuid = 100;
		long touid = 200;
		
		HashMap<String, Long> fromassets = new HashMap<String, Long>();
		HashMap<String, Long> toassets = new HashMap<String, Long>();
		
		
		fromassets.put("diamond", 10L);
		toassets.put("hi_coin", 10L);
		
//		engine.transfer(appid, apptranxid, fromuid, fromassets, touid, toassets);
		// String hextranxid = engine.generateTransferTranx(apptranxid, appid, fromuid, fromassets, touid, toassets);
//		ObjectId oid = new ObjectId(hextranxid);
//		System.out.println("oid: " + oid.toHexString());
//		engine.execTransferTranx(hextranxid);
		
		
		
		try {
			engine.insertAssetsDocForUid(fromuid);
			
		} catch(Exception ex){}
		HashMap<String, Long> testArgs = new HashMap<String, Long>();
		testArgs.put("diamond", 100L);
		// Result incrRes = engine.incrby(appid, "test-apptranxid-" + UUID.randomUUID().toString(), fromuid, testArgs);
		// System.out.println("incrRes: " + incrRes.code + "  " + incrRes.msg);
		
		
		LinkedList<Document> tranxList = engine.findNewTransferTranx(1);
		
		Document tranx = tranxList.get(0);
		Result result = engine.execTransferTranx(tranx.getObjectId("_id").toHexString());
		
		return;
		
		
	}
	
	protected Document getTransferTranxDoc(String appid, String apptranxid) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollTransferTranx);
		
		Document filter = new Document();
		filter.append("appid", appid);
		filter.append("apptranxid", apptranxid);
		FindIterable<Document> r = collection.find(filter);
		MongoCursor<Document> cur = r.iterator();
		if (!cur.hasNext())
			return null;
		
		return cur.next();
	}
	public LinkedList<Document> findNewTransferTranx(int limit) {
		LinkedList<Document> ret = new LinkedList<Document>();
		Document query = new Document();
		query.append("status", TRANSFER_TRANX_STATUS_NEW);
		ret = getMongoDocuments(this.mongodbDBName, this.mongodbDBCollTransferTranx, query, 0, limit);
		return ret;
	}
	
	
	
	
	
	/**
	 * 返回uid名下的资产。
	 * fields 指明需要返回的资产项目  ， 以0为默认值填充的不存在的资产数。
	 *
	 * */
	public Result getAssets(long uid, HashSet<String> fields) {
		DictResult res = new DictResult();
		
		if (fields!=null) {
			for (String f : fields) {
				res.put(f, 0L);
			}
		}
		
		Document doc = this.getMongoDocumentById(this.mongodbDBName, this.mongodbDBCollAssets, uid);
		if (doc == null)
			return res;
		for(Entry<String, Object> entry : doc.entrySet()) {
			String k = entry.getKey();
			if (!k.startsWith("va_"))
				continue;
			if (!fields.contains(k))
				continue;
			if (! (entry.getValue() instanceof Long))
				continue;
			Long v = (Long)entry.getValue();
			res.put(k, v);
		}
		
		return res;
	}
	
	
	
	@Override
	public String getMongoConnStr() {
		return mongoConnStr;
	}
}
