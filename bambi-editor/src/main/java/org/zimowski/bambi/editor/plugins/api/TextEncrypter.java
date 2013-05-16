package org.zimowski.bambi.editor.plugins.api;

/**
 * Scrambling mechanism for securing text information such as user id and 
 * password. Depending on the implementation this could be one way hash,  
 * cipher or anything in between.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public interface TextEncrypter {

	/**
	 * Encrypts single string of text with the implementation specific 
	 * algorithm. This could be a non-decryptable digest, decryptable string  
	 * given a right cipher, or even unmodified string depending on the 
	 * algorithm used.
	 * 
	 * @param text value to be encrypted
	 * @return encrypted value
	 */
	public String encrypt(String text);
}
