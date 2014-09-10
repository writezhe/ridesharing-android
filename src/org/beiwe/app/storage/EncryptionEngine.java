package org.beiwe.app.storage;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import org.beiwe.app.storage.TextFileManager;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.telephony.PhoneNumberUtils;
import android.util.Base64;
import android.util.Log;



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
			int value = byteArray[i] & 0xFF;
			hexCharArray[i * 2] = hexArray[value >>> 4];
			hexCharArray[i * 2 + 1] = hexArray[value & 0x0F];
		}
		return new String(hexCharArray);
	}
	
	
	/**Encrypts data using the RSA cipher and the public half of an RSA key pairing provided by the server. 
	 * @param text to be encrypted
	 * @return a hex string of the encrypted data. */
	public static String encrypt(String text) {
		if (key == null) { EncryptionEngine.readKey(); }
		
		byte[] encryptedText = null;
		Cipher rsaCipher = null;
		
		try { rsaCipher = Cipher.getInstance("RSA"); }
		catch (NoSuchAlgorithmException e) { Log.e("Encryption Engine", "THIS DEVICE DOES NOT SUPPORT RSA?"); }
		catch (NoSuchPaddingException e) { Log.e("Encryption Engine", "Something went wrong, go research Padding Exceptions. (NoSuchPaddingException) "); }
		finally { Log.e("Encryption Engine", "encryption is about to fail"); }
		
		try { rsaCipher.init(Cipher.ENCRYPT_MODE, key);	}
		catch (InvalidKeyException e) { Log.e("Encryption Engine", "The key is not a valid public RSA key."); }
		
		try {  encryptedText = rsaCipher.doFinal( text.getBytes() ); }
		catch (IllegalBlockSizeException e) { Log.e("Encryption Engine", "The key is malformed."); }
		catch (BadPaddingException e) { Log.e("Encryption Engine", "Something went wrong, go research Padding Exceptions. (BadPaddingException)"); }
		finally { Log.e("Encryption Engine", "Encryption has failed."); }

		return  bytesToHex( encryptedText );
	}
	
	/**Looks for the public key file and imports it.
	 * Spews out human readable errors to the Log if something seems wrong. */
	public static void readKey() {
		String key_content = TextFileManager.getKeyFile().read();
		byte[] key_bytes = Base64.decode(key_content, Base64.DEFAULT);
		
//		Log.i(" key ", key_content );
//		Log.i( "key length", "" + key_bytes.length );
		
		X509EncodedKeySpec x509EncodedKey = new X509EncodedKeySpec( key_bytes );
		
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			key = keyFactory.generatePublic( x509EncodedKey );
		} catch (NoSuchAlgorithmException e1) {
			Log.e("Encryption Engine", "ENCRYPTION HAS FAILED BECAUSE RSA IS NOT SUPPORTED?  AN ENCRYPT OPERATION IS ABOUT TO FAIL.");
			e1.printStackTrace();
		} catch (InvalidKeySpecException e2) {
			Log.e("Encryption Engine", "The provided RSA public key is NOT VALID.\nBEFORE doing anything else check that the key is the PUBLIC half of an RSA key pairing." );
		}
	}
}