package org.beiwe.app.storage;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import org.beiwe.app.storage.TextFileManager;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import android.telephony.PhoneNumberUtils;
import android.util.Base64;
import android.util.Log;


@SuppressLint("SecureRandom")
public class EncryptionEngine {
	
	private static PublicKey key = null;
	
	/*############################################################################
	 * ############################ Phone Numbers ################################
	 * #########################################################################*/
	
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
			return toBase64String( hasher.digest() );
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
		/* Note: this is only tested for US & Canadian phone numbers, and will probably break on
		 * international phone numbers.
		 * Improvement idea: make this work worldwide, perhaps using tools in Google's
		 * libphonenumber library: https://github.com/googlei18n/libphonenumber */
		String formattedNumber = PhoneNumberUtils.formatNumber(rawNumber);

		if (formattedNumber.startsWith("+1-"))     { return formattedNumber; }
		else if (formattedNumber.startsWith("1-")) { return "+1-" + formattedNumber.substring(2); }
		else { return "+1-" + formattedNumber; }
	}
	
	/*############################################################################
	 * ############################### Hashing ###################################
	 * #########################################################################*/
	
	/** Takes a string as input, handles the usual thrown exceptions, and return a hash string of that input.  
	 * @param input A String to hash
	 * @return a Base64 String of the hash result. */
	public static String safeHash (String input) {
		try {
			return unsafeHash( input ); }
		catch (NoSuchAlgorithmException e) {
			Log.e("Hashing function", "NoSuchAlgorithmException");
			e.printStackTrace(); }
		catch (UnsupportedEncodingException e) {
			Log.e("Hashing function", "UnsupportedEncodingException");
			e.printStackTrace(); }
		Log.e("hash", "this line of code should absolutely never run.");
		return null;
	}
	
	
	/** Takes a string as input, outputs a hash.
	 * @param input A String to hash.
	 * @return a Base64 String of the hash result. */
	public static String unsafeHash (String input) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		if (input == null ) { Log.e("Hashing", "The hash function received a null string, it should now crash...");}
		MessageDigest hash = null;

		hash = MessageDigest.getInstance("SHA-256");
		hash.update( input.getBytes("UTF-8") );
		return toBase64String( hash.digest() );		
	}
	
	
	/*############################################################################
	 * ############################ Encryption ###################################
	 * #########################################################################*/
	
	
	/**Encrypts data using the RSA cipher and the public half of an RSA key pairing provided by the server. 
	 * @param data to be encrypted
	 * @return a hex string of the encrypted data. */
	@SuppressLint("TrulyRandom")
	public static String encryptRSA(byte[] data) {
		if (key == null) { EncryptionEngine.readKey(); }
		
		byte[] encryptedText = null;
		Cipher rsaCipher = null;
		
		try { rsaCipher = Cipher.getInstance("RSA"); }
		catch (NoSuchAlgorithmException e) { Log.e("Encryption Engine", "THIS DEVICE DOES NOT SUPPORT RSA?"); }
		catch (NoSuchPaddingException e) { Log.e("Encryption Engine", "Something went wrong, go research Padding Exceptions. (NoSuchPaddingException) "); }
		
		try { rsaCipher.init(Cipher.ENCRYPT_MODE, key);	}
		catch (InvalidKeyException e) { Log.e("Encryption Engine", "The key is not a valid public RSA key."); }
		
		try {  encryptedText = rsaCipher.doFinal( data ); }
		catch (IllegalBlockSizeException e1) { Log.e("Encryption Engine", "The key is malformed."); } 
		catch (BadPaddingException e2) { Log.e("Encryption Engine", "Something went wrong, go research Padding Exceptions. (BadPaddingException)"); }
		
		return toBase64String(encryptedText);
	}

	
	/**Looks for the public key file and imports it.
	 * Spews out human readable errors to the Log if something seems wrong. */
	public static void readKey() {
		String key_content = TextFileManager.getKeyFile().read();
		byte[] key_bytes = Base64.decode(key_content, Base64.DEFAULT);
		X509EncodedKeySpec x509EncodedKey = new X509EncodedKeySpec( key_bytes );
		
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			key = keyFactory.generatePublic( x509EncodedKey ); }
		catch (NoSuchAlgorithmException e1) {
			Log.e("Encryption Engine", "ENCRYPTION HAS FAILED BECAUSE RSA IS NOT SUPPORTED?  AN ENCRYPT OPERATION IS ABOUT TO FAIL.");
			e1.printStackTrace(); }
		catch (InvalidKeySpecException e2) {
			Log.e("Encryption Engine", "The provided RSA public key is NOT VALID." );
			e2.printStackTrace(); }
	}
	
	
	public static String encryptAES(String someText) { return encryptAES( someText.getBytes() ); }
	
	public static String encryptAES(byte[] someText) {
		// setup seed		
		SecureRandom random = null;
		try { random = SecureRandom.getInstance("SHA1PRNG");}
		catch (NoSuchAlgorithmException e) { //seems unlikely
			Log.e("Encryption Engine", "device does not know what sha1 is..." );
			e.printStackTrace(); }
		
		random.setSeed( System.currentTimeMillis() ); //using a mildly insecure seed, do not care.

		//setup key generator object...
		KeyGenerator aesKeyGen = null;
		try { aesKeyGen = KeyGenerator.getInstance("AES"); }
		catch (NoSuchAlgorithmException e) { //seems unlikely
			Log.e("Encryption Engine", "device does not know what AES is... instance 1" );
			e.printStackTrace(); }
		
		aesKeyGen.init( 128, random );

		//from key generator, generate a key!
		SecretKey secretKey = aesKeyGen.generateKey();
		byte[] aesKey = secretKey.getEncoded();

		//iv
		byte[] iv = random.generateSeed(16); 
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		//we will use cfb mode so that we do not need to care about input length
		SecretKeySpec secretKeySpec = new SecretKeySpec( aesKey, "AES" );
		Cipher cipher = null;
		try { cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");}
		catch (NoSuchAlgorithmException e) { // seems unlikely and should fail at the previous AES
			Log.e("Encryption Engine", "device does not know what AES is, instance 2" );
			e.printStackTrace(); }
		catch (NoSuchPaddingException e) { //seems unlikely
			Log.e("Encryption Engine", "device does not know what PKCS5 padding is" );
			e.printStackTrace(); }
		
		try { cipher.init( Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec ); }
		catch (InvalidKeyException e) { // key is autogenerated and of a hardcoded length, should not happen.
			Log.e("Encryption Engine", "autogenerated AES key was invalid?" );
			e.printStackTrace(); }
		catch (InvalidAlgorithmParameterException e) { //seems unlikely, iv generation failed?
			Log.e("Encryption Engine", "an unknown error occured during AES encryption" );
			e.printStackTrace(); }

		try {
			return  encryptRSA( aesKey ) + ":" +
			toBase64String( ivSpec.getIV() ) + ":" +
			toBase64String( cipher.doFinal( someText ) ); }
		catch (IllegalBlockSizeException e) { //not possible, block size is hardcoded.
			Log.e("Encryption Engine", "an impossible error ocurred" );
			e.printStackTrace(); }
		catch (BadPaddingException e) {
			Log.e("Encryption Engine", "an unknown error occured in AES padding" );
			e.printStackTrace(); }
		//Should never run.
		Log.e("Encryption Engine", "AES encryption failed" );
		return null;
	}
	
	private static String toBase64String( byte[] data ) { return Base64.encodeToString(data, Base64.NO_WRAP | Base64.URL_SAFE ); }
//	private static String toBase64String( String data ) { return Base64.encodeToString(data.getBytes(), Base64.NO_WRAP | Base64.URL_SAFE ); }
//	private static byte[] toBase64Array( byte[] data ) { return Base64.encode(data, Base64.NO_WRAP | Base64.URL_SAFE ); }
//	private static byte[] toBase64Array( String data ) { return Base64.encode(data.getBytes(), Base64.NO_WRAP | Base64.URL_SAFE ); }
	
}