package coopci.ddia.user.basic;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.DigestException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import coopci.ddia.util.Vcode;

public class PasswordUtils {


	// Thread local variable containing each thread's ID
    private final ThreadLocal<MessageDigest> _md =
        new ThreadLocal<MessageDigest>() {
            @Override protected MessageDigest initialValue() {
                try {
					return MessageDigest.getInstance("SHA-256");
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
        }
    };
    
    MessageDigest getMessageDigest() {
    	return _md.get();
    }
    
    
    
    /**
	 * 是线程安全的。
	 * @param salt
	 * @param password
	 * @return
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchAlgorithmException
	 */
	public String genStoredPassword(String plainPassword)
			throws IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException {
		
		String salt = genSalt(6);
		return this.genStoredPassword(salt, plainPassword);
	}
	
	
	
	/**
	 * 是线程安全的。
	 * @param salt
	 * @param password
	 * @return
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchAlgorithmException
	 */
	public String genStoredPassword(String salt, String plainPassword)
			throws IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException {
		
		
		MessageDigest md = getMessageDigest();
		md.update((salt + plainPassword).getBytes());
		byte[] digest = md.digest();
		return "sha256:" + salt + ":" + Base64.getEncoder().encodeToString(digest);
	}
	/**
	 * 
	 * 是线程安全的。
	 * 
	 */
	public boolean checkPassword(String plainPassword, String storedPassword)
			throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException {
		if (storedPassword == null)
			return false;
		if (plainPassword == null)
			return false;
		String[] fields = storedPassword.split(":");
		if (fields.length != 3)
			return false;
		String algorithm = fields[0];
		String salt = fields[1];
		String storedDigest = fields[2];

		String calcedPassword = this.genStoredPassword(salt, plainPassword);

		return calcedPassword.equals(storedPassword);

	}

	static public String genSalt(int len) {
		String SALTCHARS = "1234567890abcdefghijklmnopqrstuv";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		while (salt.length() < len) {
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		String saltStr = salt.toString();
		return saltStr;
	}

}
