package org.zimowski.bambi.commons;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Often utilized converters and similar routines when working with
 * cryptography.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class CryptoUtil {
	
	private static final Logger log = LoggerFactory.getLogger(CryptoUtil.class);
	
	public static String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}
	
	/**
	 * Hashes text into chosen digest, then converts UTF-8 bytes to hex for the 
	 * final result. The digest must be compatible with 
	 * {@link MessageDigest#getInstance(String)}.
	 * 
	 * @param text value to be hashed
	 * @param digest hash algorithm to be used for hashing
	 * @return hashed value
	 */
	public static String hash(String text, String digest) {
		String hash = "";
		try {
			MessageDigest crypt = MessageDigest.getInstance(digest);
			crypt.reset();
			crypt.update(text.getBytes("UTF-8"));
			hash = CryptoUtil.byteToHex(crypt.digest());
		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage());
		}
		return hash;
	}
}