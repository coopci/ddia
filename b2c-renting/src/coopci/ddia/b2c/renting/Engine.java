package coopci.ddia.b2c.renting;

import java.util.Date;
import java.util.LinkedList;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

import coopci.ddia.Result;
import coopci.ddia.results.ListResult;
import coopci.ddia.results.UserInfo;

public class Engine {
	public static String TRANX_LOG_STATUS_NEW = "new"; // 表示还没做。
	public static String TRANX_LOG_STATUS_APPLIED = "applied"; // 表示 这个事务的效果已经体现到帐户上了，离彻底做完，只差把这个事务的状态改成TRANX_LOG_STATUS_DONE。
	public static String TRANX_LOG_STATUS_DONE = "done"; // 表示已经彻底做完了。
	
	
	public static String TRANX_LOG_TYPE_DEPOSIT = "deposit";
	
	public static String FIELD_NAME_PENDING_TRANX = "pending_tranx";
	
	
	public static Document NON_EXISTS_FILTER = new Document( "$exists", false);
	
	
	String mongoConnStr = "mongodb://localhost:27017/";
	String mongodbDBName = "b2c_renting";
	String mongodbDBCollModels = "models";    // 物品的型号。每一个型号对应一个mongodb文档。
	String mongodbDBCollItems = "items";          // 所有可被出借的物品。每一个物品对应一个mongodb文档。每个物品都应该有型号。
	
	// 记录各个用户押金和当前租借情况的collection。每个document对应一个用户。
	// _id是用户id
	// 
	String mongodbDBCollRenting = "renting"; 
	
	
	// 记录每一笔 交易， 包括充值，提现和扣费。
	// 这个collection的作用除了时候查帐，还用来保证同一个事物不会被重复执行。
	String mongodbDBCollTranxlogs = "tranx_log"; // _id, uid(sharding), type(deposit, withdraw, consume), amount, status
	
	
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
	
	// 对于 uid 是sharding key 但是 uid 不是 _id 的collection，应该用这个函数。
	Document getMongoDocumentByIdAndUid(String collname, String id, long uid) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(collname);
		Document filter = new Document();
		filter.append("_id", id);
		filter.append("uid", uid);
		FindIterable<Document> iter = collection.find(filter);
		Document doc = iter.first();
		return doc;
	}

	
	// 对于 uid 是sharding key 但是 uid 不是 _id 的collection，应该用这个函数。
	// upsert: false
	void updateMongoDocumentByIdAndUid(String collname, String id, long uid, Document updatevalue) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(collname);
		
		Document filter = new Document();
		filter.append("_id", id);
		filter.append("uid", uid);
		
		Document update = new Document();
		update.append("$set", updatevalue);
		UpdateOptions opt = new UpdateOptions();
		opt.upsert(false);
		
		collection.updateOne(filter, update, opt);
		return;
	}
	
	
	
	void applyTranxLogToRentingColl(long uid, Document tranxlog) {
		String tranx_id = tranxlog.getString("_id");
		Long amount = tranxlog.getLong("amount");
		
		String type = tranxlog.getString("type");
		if (type == null) {
			// TODO throw
			return;
		} else if (type.equals("deposit")) {
			
		} else if (type.equals("withdraw")) {
			amount = -amount;
		} else if (type.equals("consume")) {
			amount = -amount;
		} else if (type.equals("freeze")) {
			amount = -amount;
		} else {
			return;
			// TODO throw;
		}
		
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollRenting);
		
		Document filter = new Document();
		filter.append("_id", uid);
		filter.append(FIELD_NAME_PENDING_TRANX + "." + tranx_id, NON_EXISTS_FILTER );
		
		
		
		Document update = new Document();
		update.append("$set", new Document(FIELD_NAME_PENDING_TRANX + "." + tranx_id, amount));
		update.append("$inc", new Document("pledge", amount));
		UpdateOptions opt = new UpdateOptions();
		opt.upsert(false);
		
		collection.updateOne(filter, update, opt);
		return;
		
		
	}
	
	
		
	void unsetPendingTranx(String tranx_id, long uid) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollRenting);
		
		Document filter = new Document();
		filter.append("_id", uid);
		
		Document update = new Document();
		update.append("$unset", new Document(FIELD_NAME_PENDING_TRANX + "." + tranx_id, ""));
		UpdateOptions opt = new UpdateOptions();
		opt.upsert(false);
		
		collection.updateOne(filter, update, opt);
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
	
	
	void insertMongoDocument(String dbname, String collname, Document data) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		collection.insertOne(data);
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
	
	
	// 执行tranx_id表示的事务。 也用与完成之前被打断的事务。
	public void applyTranxLog(long uid, String tranx_id) {
		
		Document tranxlog = this.getMongoDocumentByIdAndUid(this.mongodbDBCollTranxlogs, tranx_id, uid);
		String status = tranxlog.getString("status");
		
		if (TRANX_LOG_STATUS_NEW.equals(status)) {
			this.applyTranxLogToRentingColl(uid, tranxlog);
			this.updateMongoDocumentByIdAndUid(this.mongodbDBCollTranxlogs, tranx_id, uid, 
					new Document("status", TRANX_LOG_STATUS_APPLIED));
			this.unsetPendingTranx(tranx_id, uid);
			this.updateMongoDocumentByIdAndUid(this.mongodbDBCollTranxlogs, tranx_id, uid, 
					new Document("status", TRANX_LOG_STATUS_DONE));
		} else if (TRANX_LOG_STATUS_APPLIED.equals(status)) {
			this.unsetPendingTranx(tranx_id, uid);
			this.updateMongoDocumentByIdAndUid(this.mongodbDBCollTranxlogs, tranx_id, uid, 
					new Document("status", TRANX_LOG_STATUS_DONE));
		} else if (TRANX_LOG_STATUS_DONE.equals(status)) {
			this.unsetPendingTranx(tranx_id, uid);
		}
		// 如果 mongodbDBCollTranxlogs的status为new: 
		//     给 this.mongodbDBCollRenting 的pledge字段加 数， 并同时设置 pending_tranx.$tranx_id 为 存在。
		//     mongodbDBCollTranxlogs的status设置为applied。
		//     把 this.mongodbDBCollRenting pending_tranx.$tranx_id unset。
		//     mongodbDBCollTranxlogs的status设置为done。
		
		// 如果 mongodbDBCollTranxlogs的status为applied: 
		//     把 this.mongodbDBCollRenting pending_tranx.$tranx_id unset。
		//     mongodbDBCollTranxlogs的status设置为done。
		
		// 如果 mongodbDBCollTranxlogs的status为done: 
		//	    把 this.mongodbDBCollRenting pending_tranx.$tranx_id unset。
		
	}
	/**
	 * 给uid表示的用户增加押金。 
	 * 这个里面要创建 事务记录。
	 * */
	public Result addPledge(long uid, long amount, String tranx_id) {
		Result ret = new Result();
		
		if (tranx_id == null || tranx_id.length() == 0) {
			ret.code = 400;
			ret.msg = "tranx_id is required.";
			return ret;
		}
		Date now =  new Date();
		
		try {
			Document tranxLog = new Document();
			tranxLog.append("_id", tranx_id);
			tranxLog.append("amount", amount);
			tranxLog.append("uid", uid);
			tranxLog.append("status", TRANX_LOG_STATUS_NEW);
			tranxLog.append("type", TRANX_LOG_TYPE_DEPOSIT);
			this.insertMongoDocument(this.mongodbDBName, this.mongodbDBCollTranxlogs, tranxLog);
		} catch (com.mongodb.MongoWriteException ex) {
			if (ex.getCode() == 11000) {
				// 忽略这个错误，继续下面的 applyTranxLog 。因为applyTranxLog 也可以用来继续之前被打断的事务。
			}
		}
		
		
		this.applyTranxLog(uid, tranx_id);
		
		
		return ret;
	}
	
	
	public Result getRentingStatus(long uid) {
		ListResult ret = new ListResult();
		Date now =  new Date();
		
		Document doc = this.getMongoDocumentById(this.mongodbDBName, this.mongodbDBCollRenting, uid);
		if (doc != null) {
			UserInfo ui = new UserInfo();
			long pledge = 0;
			if (doc.containsKey("pledge")) {
				pledge = doc.getLong("pledge");
			} 
			ui.put("pledge", pledge);
			ret.add(ui);
		}
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
