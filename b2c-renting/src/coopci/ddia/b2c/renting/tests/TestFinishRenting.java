package coopci.ddia.b2c.renting.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import coopci.ddia.Result;
import coopci.ddia.b2c.renting.Engine;

public class TestFinishRenting {
	
	
	@Test
	public void testFinishOK() throws Exception {
		Engine engine = new Engine();
		engine.init();
		long uid = 7;
		String item_id = "test-item-3";
		
		Result r = engine.finishRenting(uid, item_id);
		
		
		return;
		
	}
}
