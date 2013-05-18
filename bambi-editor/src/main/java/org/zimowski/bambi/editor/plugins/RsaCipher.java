package org.zimowski.bambi.editor.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.commons.RsaKeyGenerator;
import org.zimowski.bambi.editor.plugins.api.AbstractPlugin;
import org.zimowski.bambi.editor.plugins.api.TextEncrypter;

/**
 * Encrypter backed by RSA, making it possible to decrypt the result string. 
 * This implementation needs public key resource to encrypt, either the 
 * {@link #PUBLIC_KEY} or one that is custom defined looked for on the 
 * classpath. The public/private keypair can be easily generated with 
 * {@link RsaKeyGenerator#generateKeyPair(boolean, int, java.io.File, java.io.File)}. 
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class RsaCipher extends AbstractPlugin implements TextEncrypter {
	
	private static final Logger log = LoggerFactory.getLogger(RsaCipher.class);
	
	/**
	 * Public key resource name and path this cipher needs for encryption. This 
	 * file should be on root classpath of the packaged jar.
	 */
	public static final String PUBLIC_KEY = "/public.rsa";
	
	/**
	 * Public key resource (in the form of a file) used for encryption.
	 */
	private PublicKey publicKey;

	
	/**
	 * Constructs instance backed by {@link #PUBLIC_KEY}. Exception is thrown 
	 * if that file is not found.
	 * 
	 * @throws IOException if public key file is not on classpath
	 */
	public RsaCipher() throws IOException {
		this(PUBLIC_KEY);
	}
	
	/**
	 * Constructs an instance backed by user defined resource representing 
	 * a valid public key. Unlike the default constructor which looks for 
	 * specific resource at the specific location, this resource can be 
	 * anywhere in the classpath hierarchy.
	 * 
	 * @param publicKeyResourcePath
	 * @throws IOException
	 */
	public RsaCipher(String publicKeyResourcePath) throws IOException {
		InputStream publicKeyStream = RsaCipher.class.getResourceAsStream(publicKeyResourcePath);
		if(publicKeyStream == null) throw new IOException(publicKeyResourcePath + " not on classpath");
		publicKey = RsaKeyGenerator.readPublicKey(publicKeyStream);
	}
	
	@Override
	public String encrypt(String text) {
		String encryptedText = null;
		byte[] encryptedBytes = encryptRaw(text);
		encryptedText = DatatypeConverter.printHexBinary(encryptedBytes);
		return encryptedText;
	}
	
	public String decrypt(String cipherText, PrivateKey privateKey) {
		String clearText = null;
		byte[] encryptedBytes = DatatypeConverter.parseHexBinary(cipherText);
		clearText = decryptRaw(encryptedBytes, privateKey);
		return clearText;
	}
	
	public String decrypt(String cipherText, String privateKeyResourcePath) throws IOException {
		InputStream privateKeyStream = RsaCipher.class.getResourceAsStream(privateKeyResourcePath);
		if(privateKeyStream == null) throw new IOException(privateKeyStream + " not on classpath");
		PrivateKey privateKey = RsaKeyGenerator.readPrivateKey(privateKeyStream);
		return decrypt(cipherText, privateKey);
	}

	private byte[] encryptRaw(String plainText) {
		byte[] cipherData = null;
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			cipherData = cipher.doFinal(plainText.getBytes());
			
		}catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage());
		} catch (NoSuchPaddingException e) {
			log.error(e.getMessage());
		} catch (InvalidKeyException e) {
			log.error(e.getMessage());
		} catch (IllegalBlockSizeException e) {
			log.error(e.getMessage());
		} catch (BadPaddingException e) {
			log.error(e.getMessage());
		}
		
		return cipherData;
	}
	
	private String decryptRaw(byte[] encryptedBytes, PrivateKey privateKey) {
		String clearText = null;
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] decryptedData = cipher.doFinal(encryptedBytes);
			clearText = new String(decryptedData);
		}catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage());
		} catch (NoSuchPaddingException e) {
			log.error(e.getMessage());
		} catch (InvalidKeyException e) {
			log.error(e.getMessage());
		} catch (IllegalBlockSizeException e) {
			log.error(e.getMessage());
		} catch (BadPaddingException e) {
			log.error(e.getMessage());
		}

		return clearText;
	}
}