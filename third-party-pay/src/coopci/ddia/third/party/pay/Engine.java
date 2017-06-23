package coopci.ddia.third.party.pay;

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
import coopci.ddia.requests.CheckOrderRequest;
import coopci.ddia.requests.CreateOrderRequest;
import coopci.ddia.results.ListResult;
import coopci.ddia.results.UserInfo;
import coopci.ddia.util.SessidPacker;
import coopci.ddia.util.Vcode;

public class Engine implements IMongodbAspect {
	
	String mongoConnStr = "mongodb://localhost:27017/";
	String mongodbDBName = "third_party_pay";     // mongodb的库名字
	String mongodbDBCollOrders = "orders";    
	
	
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
	

	
	public Result checkOrder(CheckOrderRequest req){
		Result res = new Result();
		return res;
	}
	public Result createOrder(CreateOrderRequest req){
		Result res = new Result();
		return res;
	}
	
	
	public Result weixinCheckOrder(CheckOrderRequest req){
		Result res = new Result();
		return res;
	}
	public Result weixinCreateOrder(CreateOrderRequest req){
		Result res = new Result();
		return res;
	}
	
	
	
	

	
	public Result backdoorCheckOrder(CheckOrderRequest req){
		Result res = new Result();
		return res;
	}
	public Result backdoorCreateOrder(CreateOrderRequest req){
		Result res = new Result();
		return res;
	}
	
	@Override
	public String getMongoConnStr() {
		return mongoConnStr;
	}
}
