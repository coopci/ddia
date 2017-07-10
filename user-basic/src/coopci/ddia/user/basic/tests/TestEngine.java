package coopci.ddia.user.basic.tests;

import static org.junit.Assert.assertEquals;

import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bson.Document;
import org.junit.Test;

import coopci.ddia.results.DictResult;
import coopci.ddia.user.basic.Engine;

public class TestEngine {
	@Test
	public void testGetOrCreateUserByWeixin() throws Exception {
		
		Engine engine = new Engine();
		engine.init();
		String appid = "appid-test";
		String code = UUID.randomUUID().toString();
		String nickname = "nickname_" + code;
		Document doc1 = engine.getOrCreateUserByWeixin(appid, code, nickname, "accessToken1");
		
		
		
		Document doc2 = engine.getOrCreateUserByWeixin(appid, code, nickname, "accessToken1");
		
		assertEquals(doc1.getLong("uid"), doc2.getLong("uid"));
		
		Document doc3 = engine.getOrCreateUserByWeixin(appid, code, nickname, "accessToken2");
		
		Document doc4 = engine.getOrCreateUserByWeixin(appid, code, nickname, "accessToken3");
		
		Document doc5 = engine.getOrCreateUserByWeixin(appid, code, nickname, "accessToken4");
		assertEquals(doc1.getLong("uid"), doc5.getLong("uid"));
		
	}
	
	
	@Test
	public void testAddUser() throws Exception {
		
		Engine engine = new Engine();
		engine.init();
		DictResult result = engine.addUser("", "", null);
		
		assertEquals(200, result.code);
	}
	
	
	
	@Test
	public void testLoginWithPassword() throws Exception {
		
		Engine engine = new Engine();
		engine.init();
		DictResult result = engine.loginWithPassword("user25", "", null);
		
		assertEquals(200, result.code);
	}
}
