package coopci.ddia.user.basic.tests;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.bson.Document;
import org.junit.Test;

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
}
