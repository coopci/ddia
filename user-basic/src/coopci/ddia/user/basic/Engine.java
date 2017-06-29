package coopci.ddia.user.basic;

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




import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;

import coopci.ddia.LoginResult;
import coopci.ddia.Result;
import coopci.ddia.SessionId;
import coopci.ddia.UidResult;
import coopci.ddia.results.UserInfo;
import coopci.ddia.results.UserInfosResult;
import coopci.ddia.util.SessidPacker;
import coopci.ddia.util.Vcode;

public class Engine {
	
	String RSAPrivateKeyFile = "private_key.pem";
	String RSAPublicKeyFile = "public_key.pem";
	
	String mongoConnStr = "mongodb://localhost:27017/";
	String mongodbDBName = "user_basic";     // mongodb的库名字
	String mongodbDBCollVcode = "sms_vcode";    // 存短信验证码的collection, _id是手机号。
	String mongodbDBCollUserInfo = "user_info"; // 存用户信息的collection, _id是用户id。
	
	String mongodbDBCollPhoneUser = "phone_user"; // 用来维护 手机号的唯一性以及  手机号和user_id的对应关系。 _id是phone，  uid 字段存用户id。
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
		doc.append("seq", 0L);
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
	
	
	public long genNewUserid() {
		
		MongoClient client = this.getMongoClient();
		
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollCounters);
		
		Document query  = new Document();
		query.append("_id", "userid");
		
		Document update = new Document();
		update.append("$inc", new Document("seq", 1L));
		
		FindOneAndUpdateOptions opt = new FindOneAndUpdateOptions();
		opt.returnDocument(ReturnDocument.AFTER);
		
		Document retdoc = collection.findOneAndUpdate(query , update, opt);
		long newuid = retdoc.getLong("seq");
		
		return newuid;
	}
	public PrivateKey getPemPrivateKey(String filename, String algorithm) throws Exception {
	  File f = new File(filename);
	  FileInputStream fis = new FileInputStream(f);
	  DataInputStream dis = new DataInputStream(fis);
	  byte[] keyBytes = new byte[(int) f.length()];
	  dis.readFully(keyBytes);
	  dis.close();
	
	  String temp = new String(keyBytes);
	  String privKeyPEM = temp.replace("-----BEGIN PRIVATE KEY-----\n", "");
	  privKeyPEM = privKeyPEM.replace("-----END PRIVATE KEY-----", "");
	  privKeyPEM = privKeyPEM.replace("\n", "");
	  //System.out.println("Private key\n"+privKeyPEM);
	
	   
	      // byte [] decoded = b64.decode(privKeyPEM);
	  byte [] decoded = Base64.getDecoder().decode(privKeyPEM);
	  PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
	  KeyFactory kf = KeyFactory.getInstance(algorithm);
	  return kf.generatePrivate(spec);
	}

	   public PublicKey getPemPublicKey(String filename, String algorithm) throws Exception {
	      File f = new File(filename);
	      FileInputStream fis = new FileInputStream(f);
	      DataInputStream dis = new DataInputStream(fis);
	      byte[] keyBytes = new byte[(int) f.length()];
	      dis.readFully(keyBytes);
	      dis.close();

	      String temp = new String(keyBytes);
	      String publicKeyPEM = temp.replace("-----BEGIN PUBLIC KEY-----\n", "");
	      publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
	      publicKeyPEM = publicKeyPEM.replace("\n", "");

	      //Base64.Decoder b64 = new Base64.Decoder(false, false);
	      //byte [] decoded = b64.decode(publicKeyPEM);
	      
	      byte [] decoded = Base64.getDecoder().decode(publicKeyPEM);
	      
	      X509EncodedKeySpec spec =
	            new X509EncodedKeySpec(decoded);
	      KeyFactory kf = KeyFactory.getInstance(algorithm);
	      return kf.generatePublic(spec);
	      }
	   
	PrivateKey privkey;
	PublicKey pubkey;
	private Cipher encrypCipher;
	private Cipher decrypCipher;
	
	public void initRSAKeys() throws Exception {
		pubkey = getPemPublicKey(this.RSAPublicKeyFile, "rsa");
		privkey = getPemPrivateKey(this.RSAPrivateKeyFile, "rsa");
		
		this.encrypCipher = Cipher.getInstance("RSA");
		this.encrypCipher.init(Cipher.ENCRYPT_MODE, privkey);
		
		
		this.decrypCipher = Cipher.getInstance("RSA");
		this.decrypCipher.init(Cipher.DECRYPT_MODE, pubkey);
		
		String s = "345-167851331";
		byte[] encrypted = encrypCipher.doFinal(s.getBytes());
		
		String sessid = Base64.getEncoder().encodeToString(encrypted);
		byte[] decrypted = decrypCipher.doFinal(encrypted);
		
		return;
	}
	
	String deskeyPath = "triple-des.key";
	SessidPacker sessidPacker = new SessidPacker();
	public void initSessidPacker() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		sessidPacker.initDes(deskeyPath);
	}
	public void init() throws Exception {
		// initRSAKeys();
		
		
		
		initSessidPacker();
		connectMongo();
		initUseridSeq();
		initUniqueFields();
		
		//saveSmsVcode("34", "234");
		//long uid1 = genNewUserid();
		//long uid2 = genNewUserid();
		
		return;
	}
	public void initUniqueFields() {
		this.uniqueFields = new HashSet<String>();
		uniqueFields.add("phone");
		uniqueFields.add("nickname");
		
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
	void addPhoneUseridToMongo(String dbname, String collname, String phone, long uid) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		Document doc = new Document();
		doc.append("_id", phone);
		doc.append("uid", uid);
		collection.insertOne(doc);
		return;
	}
	void addNewUserToMongo(String dbname, String collname, long uid, Document userdata) {
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(dbname);
		MongoCollection<Document> collection = db.getCollection(collname);
		userdata.append("_id", uid);
		collection.insertOne(userdata);
		return;
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
	
	
	
	
	
	
	
	
	
	


	public Result loginSubmitPhone(String phone) throws ApiException {
		Result res = new Result();
		phone = phone.trim();
		String vcode = Vcode.genNumVcode(6);

		// TODO 把下面这行注释掉。
		vcode = "111111";


		this.saveSmsVcode(vcode, phone);
		// 把vcode用短信发到用户手机上。
		AlibabaAliqinFcSmsNumSendResponse resp = Utils.sendVcodeViaAlidayuy(phone, vcode);
		
		return res;
	}
	
	public boolean checkVcode(String phone, String vcode) {
		Document doc = getMongoDocumentById(this.mongodbDBName, this.mongodbDBCollVcode, phone);
		if (doc == null) {
			return false;
		}
		String vcodeInStore = doc.getString("vcode");
		if (vcode.equals(vcodeInStore)) {
			return true;
		}
		return false;
	}
	
	public void removeVcode(String phone) {
		removeMongoDocumentById(this.mongodbDBName, this.mongodbDBCollVcode, phone);
		
	}
	
	
	public long getOrCreateUidByPhone(String phone) {
		// 从phone找uid
		// 如果找不到，则生成新的uid，并把phone和生成的uid 关联起来。
		Document doc = this.getMongoDocumentById(this.mongodbDBName, this.mongodbDBCollPhoneUser, phone);
		if (doc != null) {
			long uid = doc.getLong("uid");
			return uid;
		}
		long uid = this.genNewUserid();
		this.addPhoneUseridToMongo(this.mongodbDBName, this.mongodbDBCollPhoneUser, phone, uid);
		Document userdata = new Document();
		userdata.append("phone", phone);
		this.addNewUserToMongo(this.mongodbDBName, this.mongodbDBCollUserInfo, uid, userdata);
		return uid;
	}
	public LoginResult loginSubmitVcode(String phone, String vcode) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		LoginResult res = new LoginResult();
		
		boolean checkResult = this.checkVcode(phone, vcode);
		if (!checkResult) {
			res.code = 401;
			res.msg = "提交的短信验证码有误。";
			return res;
		}
		
		long uid = this.getOrCreateUidByPhone(phone);
		
		SessionId sessid = new SessionId();
		sessid.uid = uid;
		String sessidstr = sessidPacker.pack(sessid);
		res.sessid = sessidstr;
		this.removeVcode(phone);
		res.uid = uid;
		return res;
	}
	
	public UidResult getUidBySessid(String sessidstr) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		UidResult res = new UidResult();
		SessionId sessid = this.sessidPacker.unpack(sessidstr);
		res.uid = sessid.uid;
		return res;
	}
	
	
	public Result getUserinfoByUids(String uidslistStr, String fieldslistStr) {
		UserInfosResult ret = new UserInfosResult();
		String[] fieldslist = fieldslistStr.split(",");
		for (String s : uidslistStr.split(",")) { 
			try {
				Long uid = Long.parseLong(s);
				Document doc = this.getMongoDocumentById(this.mongodbDBName, this.mongodbDBCollUserInfo, uid);
				if (doc != null) {
					UserInfo ui = ret.addEmpty(uid);
					
					for (String fieldname : fieldslist) {
						Object value = doc.get(fieldname);
						if (value != null) {
							ui.put(fieldname, value);
						}
					}
				}
			} catch (Exception ex){}
		}
		
		
		return ret;
	}
	
	
	Set<String> uniqueFields;
	// 根据 fieldname指定的字段名和 fieldvalue 指定的字段值  找到对应的用户信息。
	// fields是要获取的字段。
	public Result lookupUserinfoByUniqueField(String fieldname, String fieldvalue, Set<String> fields) {
		UserInfosResult result = new UserInfosResult();
		
		if (!uniqueFields.contains(fieldname)) {
			result.code = 400;
			result.msg = "Specified field is not unique. fieldname: " + fieldname;
			return result;
		}
		
		
		MongoClient client = this.getMongoClient();
		MongoDatabase db = client.getDatabase(this.mongodbDBName);
		MongoCollection<Document> collection = db.getCollection(this.mongodbDBCollUserInfo);
		Document filter = new Document();
		filter.append(fieldname, fieldvalue);
		FindIterable<Document> iter = collection.find(filter);
		MongoCursor<Document> cur = iter.iterator();
		if (!cur.hasNext()) {
			result.code = 404;
			result.msg = "Could not find such user.";
			return result;
		}
		Document first = cur.next();
		
		if (cur.hasNext()) {
			result.code = 400;
			result.msg = "More than 1 result.";
			return result;
		}
		
		long uid = first.getLong("_id");
		UserInfo ui = result.addEmpty(uid);
		for (String f : fields) {
			if (first.containsKey(f)) {
				Object v = first.get(f);
				ui.put(f,  v);
			}
		}
		return result;
	}
	
	
	
	// upsert : false
	void updateMongoDocumentById(String dbname, String collname, Document data, long id) {
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
		
		
		
	public Result setUserinfo(long uid, HashMap<String, Object> info) {
		UserInfosResult result = new UserInfosResult();
		Document data = new Document(info);
		this.updateMongoDocumentById(this.mongodbDBName, this.mongodbDBCollUserInfo, data, uid);
		
		return result;
	}
	
}
