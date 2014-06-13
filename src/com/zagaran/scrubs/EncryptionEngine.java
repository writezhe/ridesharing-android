package com.zagaran.scrubs;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptionEngine {
	
	public String hash (String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest steve = MessageDigest.getInstance("SHA-256");
		steve.update( input.getBytes("UTF-8") );
		return new String( steve.digest(), "UTF-8") ;
	}
	
}