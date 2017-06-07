package util;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import coopci.ddia.user.basic.SessionId;

public class SessidPacker {

	DESUtil desUtil = new DESUtil();
	
	
	public void initDes(String deskeyPath) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		desUtil.readKey(deskeyPath);
	}
	
	protected String getSaltString(int len) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < len) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
	
	
	
	public String pack(SessionId ssid) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		String salt = getSaltString(8);
		ssid.salt = salt;
		String plaintext = ssid.toString();
		byte[] cipher = this.desUtil.encrypt(plaintext.getBytes());
		String ret = java.util.Base64.getEncoder().encodeToString(cipher);
		return ret;
	}
	
	public SessionId unpack(String ssidstr) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		SessionId ret = new SessionId();
		
		byte[] cipher = java.util.Base64.getDecoder().decode(ssidstr);
		String plaintext = new String(this.desUtil.decrypt(cipher));
		ret.parse(plaintext);
		return ret;
	}
	

	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		SessidPacker packer = new SessidPacker();
		packer.initDes("triple-des.key");
		long uid = 123123;
		
		
		
		SessionId sessidBefore = new SessionId();
		sessidBefore.uid = uid;
		
		long loops = 1000000;
		Date before = new Date();
		for (long i = 0 ; i < loops; ++i) {
			String sessidstr = packer.pack(sessidBefore);
			SessionId sessidAfter = packer.unpack(sessidstr);
			long tmp = sessidAfter.uid;
		}
		Date after = new Date();
		
		
		long ms = after.getTime() - before.getTime();
		
		
		System.out.println("loops: " + loops + "    ms: " + ms);
		return;
	}
	
	
	
}
