package coopci.ddia.b2c.renting.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import coopci.ddia.Result;
import coopci.ddia.b2c.renting.Engine;

public class TestStartRenting {
	
	
	@Test
	public void testRentOK() throws Exception {
		Engine engine = new Engine();
		engine.init();
		long uid = 7;
		String item_id = "test-item-4";
		
		Result r = engine.startRenting(uid, item_id);
		
		
		return;
	}
}
