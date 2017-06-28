package coopci.ddia;

import java.util.LinkedList;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

public interface IMongodbAspect {

	public static Document NON_EXISTS_FILTER = new Document( "$exists", false);
	public static Document EXISTS_FILTER = new Document( "$exists", true);
	
	String getMongoConnStr();
	
	void setMongoClient(MongoClient mc);
	MongoClient getMongoClient();
	
	default public void connectMongo() {
		try{
			
			if (getMongoConnStr() == null) {
				//logger.error("mongoConnStr == null in connectMongo.");
				this.setMongoClient(null);
				return;
			}
			if (getMongoConnStr().length() == 0) {
				//logger.error("mongoConnStr.length() == 0 in connectMongo.");
				this.setMongoClient(null);
				return;
			}
			MongoClientURI uri = new MongoClientURI(getMongoConnStr(),
					MongoClientOptions.builder().cursorFinalizerEnabled(false));
			this.setMongoClient(new MongoClient(uri));
		} catch(Exception ex) {
			// logger.error("Exception in connectMongo, mongoConnStr = {} ", mongoConnStr, ex);
		}
	}
	
	default public void closeMongoClient() {
		if (this.getMongoClient() == null)
			return;
		this.getMongoClient().close();
		this.setMongoClient(null);
	}
	
	
	

	default LinkedList<Document> getMongoDocuments(String dbname, String collname, Document query, int skip, int limit) {
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


	default Document getMongoDocumentById(String dbname, String collname, Object id) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		Document filter = new Document();
		filter.append("_id", id);
		FindIterable<Document> iter = collection.find(filter);
		Document doc = iter.first();
		return doc;
	}
	/*
	default Document getMongoDocumentById(String dbname, String collname, ObjectId id) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		Document filter = new Document();
		filter.append("_id", id);
		FindIterable<Document> iter = collection.find(filter);
		Document doc = iter.first();
		return doc;
	}
	
	default Document getMongoDocumentById(String dbname, String collname, String id) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		Document filter = new Document();
		filter.append("_id", id);
		FindIterable<Document> iter = collection.find(filter);
		Document doc = iter.first();
		return doc;
	}
	
	default Document getMongoDocumentById(String dbname, String collname, long id) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		Document filter = new Document();
		filter.append("_id", id);
		FindIterable<Document> iter = collection.find(filter);
		Document doc = iter.first();
		return doc;
	}
	*/
	// upsert : true
	default UpdateResult saveMongoDocumentById(String dbname, String collname, Document data, String id) {
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
	default void updateMongoDocumentById(String dbname, String collname, Document data, String id) {
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

	// upsert : false
	default UpdateResult updateMongoDocumentByFilter(String dbname, String collname, Document data, Document filter) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		
		Document update = new Document();
		update.append("$set", data);
		UpdateOptions opt = new UpdateOptions();
		opt.upsert(false);
		
		UpdateResult ur = collection.updateOne(filter, update, opt);
		
		return ur;
	}
	default ObjectId insertMongoDocument(String dbname, String collname, Document doc) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		collection.insertOne(doc);
		ObjectId id = (ObjectId)doc.get( "_id" );
		return id;
	}

	

	// upsert : false
	default void updateMongoDocumentById(String dbname, String collname, Document data, ObjectId id) {
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
	

	default void removeMongoDocumentById(String dbname, String collname, String id) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		Document filter = new Document();
		filter.append("_id", id);
		collection.deleteOne(filter);
		return;
	}
	
	default boolean ensureIndex(String dbname, String collname, Bson fields, IndexOptions opt) {
		try {
			MongoClient mc = this.getMongoClient();
			MongoDatabase db = mc.getDatabase(dbname);
			MongoCollection<Document> collection = db.getCollection(collname);
			String s = collection.createIndex(fields, opt);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

}
