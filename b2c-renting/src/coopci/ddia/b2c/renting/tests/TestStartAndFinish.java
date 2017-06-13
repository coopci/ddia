package coopci.ddia.b2c.renting.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import coopci.ddia.Result;
import coopci.ddia.b2c.renting.Engine;

public class TestStartAndFinish {
	
	
	@Test
	public void testOK() throws Exception {
		Engine engine = new Engine();
		engine.init();
		long uid = 7;
		String item_id_3 = "test-item-3";
		String item_id_4 = "test-item-4";
		String item_id_5 = "test-item-5";
		
		engine.insertTestItem(item_id_3);
		engine.insertTestItem(item_id_4);
		engine.insertTestItem(item_id_5);
		
		Result startRentingResult = engine.startRenting(uid, item_id_4);
		Result r1 = engine.getRentingStatus(uid);
		Result finishRentingResult = engine.finishRenting(uid, item_id_4);
		Result r2 = engine.getRentingStatus(uid);
		
		return;
	}
	
	
	
	
	@Test
	public void testMissRenter() throws Exception {
		Engine engine = new Engine();
		engine.init();
		long uid = 7;
		String item_id_4 = "test-item-4";
		engine.insertTestItem(item_id_4);
		
		Result startRentingResult = engine.startRenting(uid, item_id_4);
		engine.unsetRenter(uid, item_id_4);
		Result r1 = engine.getRentingStatus(uid);
		Result finishRentingResult = engine.finishRenting(uid, item_id_4);
		Result r2 = engine.getRentingStatus(uid);
		
		return;
	}
	
	

	@Test
	public void testMissRenting() throws Exception {
		Engine engine = new Engine();
		engine.init();
		long uid = 7;
		String item_id_4 = "test-item-4";
		engine.insertTestItem(item_id_4);
		
		Result startRentingResult = engine.startRenting(uid, item_id_4);
		engine.unsetRenting(uid, item_id_4);
		Result r1 = engine.getRentingStatus(uid);
		Result finishRentingResult = engine.finishRenting(uid, item_id_4);
		Result r2 = engine.getRentingStatus(uid);
		
		return;
	}
	
}
