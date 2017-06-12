package coopci.ddia.b2c.renting.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import coopci.ddia.b2c.renting.Engine;

public class TestAddPledge {
	
	
	@Test
	public void testAddOK() throws Exception {
		Engine engine = new Engine();
		engine.init();
		long uid = 7;
		long amount = 100;
		String tranx_id = "tranx_id-2";
		
		engine.addPledge(uid, amount, tranx_id);
	}
}
