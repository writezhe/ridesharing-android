package org.beiwe.app.storage;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptionEngine {
	
	/** takes a string as input, outputs a hash. 
	 * @param input A String to hash
	 * @return a String of the hash result.
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException	 */
	public String hash (String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest hasher = MessageDigest.getInstance("SHA-256"); //make a hasher (verb?) object, include the hash algorithm name.
		hasher.update( input.getBytes("UTF-8") ); //pass the hash object data (the string passed in)
		return new String( hasher.digest(), "UTF-8") ; //return the "digest", the computed hash.
	}
	
}