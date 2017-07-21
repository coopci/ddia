package coopci.ddia.virtual.assets.tests;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

import coopci.ddia.Result;
import coopci.ddia.results.ListResult;

public class TestCombo {
	@Test
	public void testCreateEmptyId() throws Exception {
		TestTransfer engine = new TestTransfer();
		long uid = 26;
		String comboId = "";
		engine.init();
		
		Result createRes = engine.createCombo(uid, comboId, null, null, null);
		assertEquals(400, createRes.code);
		
		
		createRes = engine.createCombo(uid, null, null, null, null);
		assertEquals(400, createRes.code);
	}
	
	@Test
	public void testCreate() throws Exception {
		TestTransfer engine = new TestTransfer();
		long uid = 26;
		String comboId = "test-combo-1";
		HashMap<String, Long> price = new HashMap<String, Long>();
		
		engine.init();
		
		Result createRes = engine.createCombo(uid, comboId, null, null, null);
		assertEquals(400, createRes.code);
		assertEquals("A combo must contain at least 1 item.", createRes.msg);
		
		createRes = engine.createCombo(uid, comboId, new HashMap<String, Long>(), null, null);
		assertEquals(400, createRes.code);
		assertEquals("A combo must contain at least 1 item.", createRes.msg);
		
		
		
		HashMap<String, Long> items = new HashMap<String, Long>();
		items.put("item1", 10L);

		createRes = engine.createCombo(uid, comboId, price, items, null);
		assertEquals(400, createRes.code);
		assertEquals("Items key must start with va_ : item1", createRes.msg);
		
		
		
		
		items.clear();
		items.put("va_item1", 10L);
		createRes = engine.createCombo(uid, comboId, price, items, null);
		assertEquals(200, createRes.code);
	}
	

	@Test
	public void testGet() throws Exception {
		TestTransfer engine = new TestTransfer();
		long uid = 26;
		String comboId = "test-combo-1";
		HashMap<String, Long> price = new HashMap<String, Long>();
		
		engine.init();
		
		Result res = engine.getCombos();
		
		
		
		return;
	}
	
	
	
	
	
}
