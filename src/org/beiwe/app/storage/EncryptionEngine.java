package org.beiwe.app.storage;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.beiwe.app.storage.TextFileManager;

import javax.crypto.Cipher;

import android.telephony.PhoneNumberUtils;
import android.util.Base64;
import android.util.Log;
;


public class EncryptionEngine {
	
	private static PublicKey key = null;
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	/** takes a string as input, outputs a hash. 
	 * @param input A String to hash
	 * @return a UTF-8 String of the hash result.
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException	 */
	public static String hash (String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		/** takes a string as input, outputs a hash. */
		MessageDigest hasher = MessageDigest.getInstance("SHA-256"); //make a hasher (verb?) object, include the hash algorithm name.
		hasher.update( input.getBytes("UTF-8") ); //pass the hash object data (the string passed in)
		return new String( hasher.digest(), "UTF-8") ; //return the "digest", the computed hash.
	}
	
	
	/**Converts a phone number into a 64-character hexadecimal string
	 * @param phoneNumber
	 * @return a hexadecimal string, or an error message string */
	public static String hashPhoneNumber(String phoneNumber) {
		
		String standardizedPhoneNumber = standardizePhoneNumber(phoneNumber);
	
		// Functionality to test various phone number formats:
		//		String[] formats = {"1-617-123-4567", "+1-617-123-4567", "16171234567", "6171234567", "+16171234567", "1617-123-4567"};
		//		Log.i("EncryptionEngine.java", "formats.length = " + formats.length);
		//		for (int i = 0; i < formats.length; i++) {
		//			Log.i("EncryptionEngine.java", "String " + formats[i] + " converted to: " + standardizePhoneNumber(formats[i]));
		//		}
		
		try {
			MessageDigest hasher = MessageDigest.getInstance("SHA-256"); //make a hasher (verb?) object, include the hash algorithm name.
			hasher.update(standardizedPhoneNumber.getBytes("UTF-8")); //pass the hash object data (the string passed in)
			return bytesToHex(hasher.digest());
		}
		catch (Exception e) {
			e.printStackTrace();
			return "Phone number hashing failed";
		}
	}
	
	
	/**Put the phone number in a standardized format, so that, for example,
	 * 2345678901 and +1-234-567-8901 have the same hash
	 * @param rawNumber the not-yet-standardized number
	 * @return the hopefully standardized number */
	private static String standardizePhoneNumber(String rawNumber) {
		// TODO: check many cases, and see if this works for non-US phone numbers.
		// TODO: explore Eli's idea of just grabbing the last 10 numeric digits and using those
		// TODO: If it doesn't, make declaration about false negative phone number matches.
		String formattedNumber = PhoneNumberUtils.formatNumber(rawNumber);

		if (formattedNumber.startsWith("+1-")) {
			return formattedNumber;
		} else if (formattedNumber.startsWith("1-")) {
			return "+1-" + formattedNumber.substring(2);			
		} else {
			return "+1-" + formattedNumber;
		}
	}
	
	
	/**Converts a byteArray into a hexadecimal string.
	 * Based heavily on: http://stackoverflow.com/a/9855338
	 * @param byteArray
	 * @return a String composed only of characters 0-9 and A-F */
	private static String bytesToHex(byte[] byteArray) {
		char[] hexCharArray = new char[byteArray.length * 2];
		for (int i = 0; i < byteArray.length; i++) {
			int v = byteArray[i] & 0xFF;
			hexCharArray[i * 2] = hexArray[v >>> 4];
			hexCharArray[i * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexCharArray);
	}
	
	
	/**Encrypts data using the RSA cipher, makes 
	 * @param text
	 * @return */
	public static String encrypt(String text) {
//		if (key == null) { get_key(); }
		
		byte[] encryptedText = null;
		try {
			Cipher rsaCipher = Cipher.getInstance("RSA");
			
			rsaCipher.init(Cipher.ENCRYPT_MODE, key);
			encryptedText = rsaCipher.doFinal( text.getBytes() );
		} catch (Exception e) {
			Log.i("Encryption Engine", "Encryption Exception");
			e.printStackTrace();
		}
//		Log.i("enc",cipherText. );
		return new String(encryptedText);
	}

	
	public static void readKey() throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
//		byte[] keyFile_text = TextFileManager.getKeyFile().readDataFile();
//		String key_content = new String (keyFile_text);//, "UTF-8");
//		key_content = key_content.replaceAll("(-+BEGIN RSA PRIVATE KEY-+\\r?\\n|-+END RSA PRIVATE KEY-+\\r?\\n?)", "");
		
		String key_content = TextFileManager.getKeyFile().read();
		byte[] key_bytes = Base64.decode(key_content, Base64.DEFAULT);
		
		Log.i("key", key_content );
		Log.i( "key length", "" + key_bytes.length );
		
		X509EncodedKeySpec spec = new X509EncodedKeySpec( key_bytes );
//		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec( keyFile_text );
//		RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, publicExponent);
		
		//a KeyFactory... creates keys.
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		key = keyFactory.generatePublic(spec);
	}
}