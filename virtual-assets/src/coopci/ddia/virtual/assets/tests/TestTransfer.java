package coopci.ddia.virtual.assets.tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import org.bson.Document;
import org.junit.Test;

import coopci.ddia.Result;
import coopci.ddia.virtual.assets.Engine;

public class TestTransfer extends Engine {

	@Test
	public void testAddOK() throws Exception {
		TestTransfer engine = new TestTransfer();
		
		engine.init();
		
		
		String appid = "test-appid";
		String apptranxid = "test-apptranxid-" + UUID.randomUUID().toString();
		
		long fromuid = 100;
		long touid = 200;
		
		HashMap<String, Long> fromassets = new HashMap<String, Long>();
		HashMap<String, Long> toassets = new HashMap<String, Long>();
		
		
		fromassets.put("diamond", 10L);
		toassets.put("hi_coin", 10L);
		
		try {
			engine.insertAssetsDocForUid(fromuid);
			
		} catch(Exception ex){}
		
		
		Document fromUserAssetBeforeTransfer = engine.getUserAssetById(fromuid);
		Document toUserAssetBeforeTransfer = engine.getUserAssetById(touid);
		
		
		Result result = engine.transfer(appid, apptranxid, fromuid, fromassets, touid, toassets);
		

		Document fromUserAssetAfterTransfer = engine.getUserAssetById(fromuid);
		Document toUserAssetAfterTransfer = engine.getUserAssetById(touid);
		
		
		assertEquals(200, result.code);
		
		
		Document doc = engine.getTransferTranxDoc(appid, apptranxid);
		assertNotNull(doc);
		assertEquals("done", doc.getString("status"));
		assertEquals(fromUserAssetBeforeTransfer.getLong("diamond").longValue(), 
						fromUserAssetAfterTransfer.getLong("diamond").longValue() + 10L);
		assertEquals(toUserAssetBeforeTransfer.getLong("hi_coin").longValue(), 
				toUserAssetAfterTransfer.getLong("hi_coin").longValue() - 10L);
		return;
		
	}
}
