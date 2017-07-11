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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bson.Document;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.grizzly.http.util.HttpStatus;

import coopci.ddia.user.basic.weixin.WeixinAPIClient;

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
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;

import coopci.ddia.IMongodbAspect;
import coopci.ddia.LoginResult;
import coopci.ddia.Result;
import coopci.ddia.SessionId;
import coopci.ddia.UidResult;
import coopci.ddia.results.DictResult;
import coopci.ddia.results.KVItem;
import coopci.ddia.results.UserInfosResult;
import coopci.ddia.util.SessidPacker;
import coopci.ddia.util.Vcode;

public class Engine implements IMongodbAspect {
	
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
	
	
	PasswordUtils passwordUtils = new PasswordUtils();

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
	
	
	
	/**
	 * 向数据库里 存测试用户。
	 * 
	 * */
	public long addTestUser() {
		
		long uid = this.genNewUserid();
		
		Document userdata = new Document();
		userdata.append("nickname", "user"+uid);
		this.addNewUserToMongo(this.mongodbDBName, this.mongodbDBCollUserInfo, uid, userdata);
		return uid;
		
	}
	public void init() throws Exception {
		// initRSAKeys();
		
		
		
		initSessidPacker();
		connectMongo();
		initUseridSeq();
		initUniqueFields();
		
		
		
		

		Document fields = new Document();
		fields.append("weixin.appid", 1); // 考虑到换appid的情况，用这两个字段表示联合唯一。
		fields.append("weixin.openid", 1);
		IndexOptions opt = new IndexOptions();
		opt.unique(true);
		opt.sparse(true);
		this.ensureIndex(this.mongodbDBName, this.mongodbDBCollUserInfo, fields, opt);
		
		fields = new Document();
		fields.append("phone", 1);
		opt = new IndexOptions();
		opt.unique(true);
		opt.sparse(true);
		this.ensureIndex(this.mongodbDBName, this.mongodbDBCollUserInfo, fields, opt);
		
		
		fields = new Document();
		fields.append("nickname", 1);
		opt = new IndexOptions();
		opt.unique(true);
		opt.sparse(true);
		this.ensureIndex(this.mongodbDBName, this.mongodbDBCollUserInfo, fields, opt);
		
		
		
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
	

	/**
	 * 用于后台程序 向数据库里 增加任意新用户。
	 * 如果没指定nickname，在把nickname设置为 "user"+uid;
	 * @throws NoSuchAlgorithmException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * */
	public DictResult addUser(String nickname, String password, HashMap<String, Object> properties) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException {
		DictResult ret = new DictResult();
		long uid = this.genNewUserid();
		if (nickname == null || nickname.length() == 0) {
			nickname = "user"+uid;
		}
		
		String storedPassword = this.passwordUtils.genStoredPassword(password);
		Document userdata = new Document();
		userdata.append("nickname", nickname);
		userdata.append("password", storedPassword);
		
		if (properties != null) {
			properties.remove("password");
			properties.remove("nickname");
			for (Entry<String, Object> entry : properties.entrySet()) {
				userdata.append(entry.getKey(), entry.getValue());
			}
			ret.data = properties;
		}
		
		this.addNewUserToMongo(this.mongodbDBName, this.mongodbDBCollUserInfo, uid, userdata);
		
		
		
		ret.put("uid", uid);
		ret.put("nickname", nickname);
		ret.data.remove("password");
		
		return ret;
		
	}
	
	public LinkedList<Document> getUserinfoDocsByNickname(String nickname) {
		Document query = new Document();
		query.append("nickname", nickname);
		LinkedList<Document>  ret = this.getMongoDocuments(this.mongodbDBName, this.mongodbDBCollUserInfo, query, 0, 10);
		return ret;	
	}
	
	
	
	/***
	 * 如果登陆成功，返回fields里面指定的字段。
	 * 
	 * */
	public LoginResult loginWithPassword(String ident, String password, HashSet<String> fields) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException {
		
		LoginResult res = new LoginResult();
		
		LinkedList<Document> docs = this.getUserinfoDocsByNickname(ident);
		
		if (docs == null || docs.isEmpty()) {
			res.code = 404;
			res.msg = "No such user.";
			return res;
		}
		
		Document doc = docs.getFirst();
		String storedPassword = doc.getString("password");
		boolean authed = this.passwordUtils.checkPassword(password, storedPassword);
		if (!authed) {
			res.code = 401;
			res.msg = "Incorrect password.";
			return res;
		}
		
		res.code = 200;
		res.msg = "OK";
		res.uid = doc.getLong("_id");
		
		if (fields!=null){
			for (String f : fields) {
				res.put(f, doc.get(f));	
			}
		}
		return res;
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
					KVItem ui = ret.addEmpty(uid);
					
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
		KVItem ui = result.addEmpty(uid);
		for (String f : fields) {
			if (first.containsKey(f)) {
				Object v = first.get(f);
				ui.put(f,  v);
			}
		}
		return result;
	}
	
		
	public Result loginWithWeixinSubmitCode(String code) throws JSONException {
		DictResult res = new DictResult();
		
		String aceessTokenResp = WeixinAPIClient.syncGetAccessToken(code, WeixinAPIClient.WX_APP_ID, WeixinAPIClient.WX_APP_SECRET);
		System.out.println(aceessTokenResp);
		
		JSONObject jsonObject = new JSONObject(aceessTokenResp);
		
		if (jsonObject.has("errcode")) {
			res.code = 400;
			res.msg = aceessTokenResp;
			return res;
		}
		
		String accessToken = jsonObject.getString("access_token");
		String openid = jsonObject.getString("openid");
		
		
		String userinfoResp = WeixinAPIClient.syncGetUserInfo(accessToken, openid);
		System.out.println("userinfoResp:");
		System.out.println(userinfoResp);
		JSONObject joUserInfo = new JSONObject(userinfoResp);
		if (joUserInfo.has("errcode")) {
			res.code = 400;
			res.msg = aceessTokenResp;
			return res;
		} else {
			
			// userinfoResp像下面这样:
			// {"openid":"oYPUVwO_O0lYh9dSXWYuw0CL9z_I","nickname":"无有无无","sex":1,"language":"en","city":"Haidian","province":"Beijing","country":"CN","headimgurl":"http:\/\/wx.qlogo.cn\/mmopen\/gfMNVzbTqyEib9s8REzAJgSvKwwqfSiaCTpw3r5wF2NRDZ6VPmepMlOoLMbqLG38jovpGhpV8ibj8AWXDZa5WK8qx5yfyCMODcU\/0","privilege":[],"unionid":"oU0F9v4oIZkMCDDrZC76MOOodyPY"}
			
		}
		
		Iterator iter = joUserInfo.keys();
		while(iter.hasNext()) {
			String key = (String)iter.next();
			Object value = joUserInfo.get(key);
			if (value instanceof String) {
				System.out.println(key +":" + value);
			} else {
				System.out.println(key +":" + value);
			}
		}
			
		String nickname = joUserInfo.getString("nickname");
		
		Document ret = this.getOrCreateUserByWeixin(WeixinAPIClient.WX_APP_ID, openid, nickname, accessToken);
		
		res.put("uid", ret.getLong("_id"));
		res.put("nickname", ret.getString("nickname"));
		
		return res;
	}
	
	
	
	/** 按openid找用户，如果找不到就创建一个新的 并把openid, nickname, accessToken 设置上。
	 *                   如果找到，就只把 accessToken 设置上。	
	 * 从phone找uid
	 * 如果找不到，则生成新的uid，并把phone和生成的uid 关联起来。 
	 * @param appid  微信的appid
	 * @param openid	微信的openid
	 * @param nickname
	 * @param accessToken 微信的accessToken
	 * @return
	 */
	public Document getOrCreateUserByWeixin(String appid, String openid, String nickname, String accessToken) {
		
		
		Document filter = new Document();
		
		filter.append("weixin.openid", openid);
		filter.append("weixin.appid", appid);
		
		Document doc= this.getOneMongoDocument(this.mongodbDBName, this.mongodbDBCollUserInfo, filter, 0, 1);
		if(doc != null) {
			Document update = new Document();
			update.append("weixin.accessToken", accessToken);
			this.updateMongoDocumentById(this.mongodbDBName, this.mongodbDBCollUserInfo, update, doc.getLong("_id"));
			return doc;
		} else {
			Long uid = this.genNewUserid();
			Document dataToInsert = new Document();
			dataToInsert.append("_id", uid);
			dataToInsert.append("nickname", nickname);
			Document weixinData = new Document();
			weixinData.append("appid", appid);
			weixinData.append("openid", openid);
			weixinData.append("accessToken", accessToken);
			dataToInsert.append("weixin", weixinData);	
			this.insertMongoDocumentWithId(this.mongodbDBName, this.mongodbDBCollUserInfo, dataToInsert);
			return dataToInsert;
		}
	}	
		
	public Result setUserinfo(long uid, HashMap<String, Object> info) {
		UserInfosResult result = new UserInfosResult();
		Document data = new Document(info);
		this.updateMongoDocumentById(this.mongodbDBName, this.mongodbDBCollUserInfo, data, uid);
		
		return result;
	}
	@Override
	public void setMongoClient(MongoClient mc) {
		this.mongoClient = mc;
	}
	@Override
	public MongoClient getMongoClient() {
		return mongoClient;
	}
	
	@Override
	public String getMongoConnStr() {
		return mongoConnStr;
	}
	
	public static void main(String[] argv) throws Exception {
		Engine engine = new Engine();
		engine.init();
		engine.addTestUser();
		engine.addTestUser();
		
	}
}
