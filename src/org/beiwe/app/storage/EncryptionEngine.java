package org.beiwe.app.storage;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.telephony.PhoneNumberUtils;
import android.util.Log;

public class EncryptionEngine {
	
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
	
	
	/**
	 * Converts a phone number into a 64-character hexadecimal string
	 * @param phoneNumber
	 * @return a hexadecimal string, or an error message string
	 */
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
	
	
	/**
	 * Put the phone number in a standardized format, so that, for example,
	 * 2345678901 and +1-234-567-8901 have the same hash
	 * @param rawNumber the not-yet-standardized number
	 * @return the hopefully standardized number
	 */
	private static String standardizePhoneNumber(String rawNumber) {
		// TODO: check many cases, and see if this works for non-US phone numbers.
		// TODO: If it doesn't, make declaration about false negative phone number matches.
		String formattedNumber = PhoneNumberUtils.formatNumber(rawNumber);

		if (formattedNumber.startsWith("+1-")) {
			return formattedNumber;
		}
		else if (formattedNumber.startsWith("1-")) {
			return "+1-" + formattedNumber.substring(2);			
		}
		else {
			return "+1-" + formattedNumber;
		}
	}
	
	
	/**
	 * Converts a byteArray into a hexadecimal string.
	 * Based heavily on: http://stackoverflow.com/a/9855338
	 * @param byteArray
	 * @return a String composed only of characters 0-9 and A-F 
	 */
	private static String bytesToHex(byte[] byteArray) {
		char[] hexCharArray = new char[byteArray.length * 2];
		for (int i = 0; i < byteArray.length; i++) {
			int v = byteArray[i] & 0xFF;
			hexCharArray[i * 2] = hexArray[v >>> 4];
			hexCharArray[i * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexCharArray);
	}
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
}