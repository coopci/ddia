package coopci.ddia.user.basic.tests;

import static org.junit.Assert.*;

import java.util.UUID;

import org.bson.Document;
import org.junit.Test;

import coopci.ddia.user.basic.Engine;
import coopci.ddia.user.basic.PasswordUtils;

public class TestPasswordUtils {
	@Test
	public void testOK() throws Exception {
		PasswordUtils pu = new PasswordUtils();
		
		String salt = PasswordUtils.genSalt(6);
		String plainPassword = "123456";
		String storedPassword = pu.genStoredPassword(salt, plainPassword);
		
		
		boolean authed = pu.checkPassword(plainPassword, storedPassword);
		authed = pu.checkPassword(plainPassword, storedPassword);
		authed = pu.checkPassword(plainPassword, storedPassword);
		authed = pu.checkPassword(plainPassword, storedPassword);
		
		assertTrue(authed);
		
		
		boolean authed2 = pu.checkPassword(plainPassword+"123", storedPassword);
		authed2 = pu.checkPassword(plainPassword+"123", storedPassword);
		authed2 = pu.checkPassword(plainPassword+"123", storedPassword);
		
		assertFalse(authed2);
		
	}
}
