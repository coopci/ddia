package coopci.ddia.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

public class DESUtil {
	
	
	public byte[] encrypt(SecretKey key, byte[] in)
	      throws NoSuchAlgorithmException, InvalidKeyException,
	      NoSuchPaddingException, IOException, IllegalBlockSizeException, BadPaddingException {
	    // Create and initialize the encryption engine
		Cipher cipher = Cipher.getInstance("DESede");
	    cipher.init(Cipher.ENCRYPT_MODE, key);
	    return cipher.doFinal(in);
	}
	
	
	
	public SecretKey deskey;
	
	
	ThreadLocal<Cipher> encrptCipher =
	         new ThreadLocal<Cipher>() {
	             @Override protected Cipher initialValue() {
	            	 
	            	try {
	            		 Cipher cipher = Cipher.getInstance("DESede");
						 cipher.init(Cipher.ENCRYPT_MODE, deskey);
						 return cipher;
					} catch (InvalidKeyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchPaddingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;	
	                 
	         }
	     };
	     
	     
	// Cipher encrptCipher = null;
	public byte[] encrypt(byte[] in)
	      throws NoSuchAlgorithmException, InvalidKeyException,
	      NoSuchPaddingException, IOException, IllegalBlockSizeException, BadPaddingException {
		
		
//		Cipher cipher = Cipher.getInstance("DESede");
//		cipher.init(Cipher.ENCRYPT_MODE, this.deskey);
//		return cipher.doFinal(in);
		
	    return encrptCipher.get().doFinal(in);
	}
	public static String keyname = "DESede";
	
	
	
	public void readKey(String path) throws IOException,
	      NoSuchAlgorithmException, InvalidKeyException,
	      InvalidKeySpecException {
		File f = new File(path);
	    // Read the raw bytes from the keyfile
	    DataInputStream in = new DataInputStream(new FileInputStream(f));
	    byte[] rawkey = new byte[(int) f.length()];
	    in.readFully(rawkey);
	    in.close();

	    // Convert the raw bytes to a secret key like this
	    DESedeKeySpec keyspec = new DESedeKeySpec(rawkey);
	    SecretKeyFactory keyfactory = SecretKeyFactory.getInstance(keyname);
	    deskey = keyfactory.generateSecret(keyspec);
	    
	    
	}
	

	ThreadLocal<Cipher> decrptCipher =
	         new ThreadLocal<Cipher>() {
	             @Override protected Cipher initialValue() {
	            	 
	            	try {
	            		 Cipher cipher = Cipher.getInstance("DESede");
						 cipher.init(Cipher.DECRYPT_MODE, deskey);
						 return cipher;
					} catch (InvalidKeyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchPaddingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;	
	                 
	         }
	     };
	     
	public byte[] decrypt(byte[] in)
		      throws NoSuchAlgorithmException, InvalidKeyException, IOException,
		      IllegalBlockSizeException, NoSuchPaddingException,
		      BadPaddingException {

//		Cipher cipher = Cipher.getInstance(keyname);
//		cipher.init(Cipher.DECRYPT_MODE, deskey);
//		return cipher.doFinal(in);
		
	    return decrptCipher.get().doFinal(in);
	}
	
	
	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		DESUtil desUtil = new DESUtil();
		desUtil.readKey("triple-des.key");
		
		
		String plaintext = "g8gohgiatgr23";
		byte[] encrypted = desUtil.encrypt(plaintext.getBytes());
		byte[] decrypted = desUtil.decrypt(encrypted);
	}
	
}
