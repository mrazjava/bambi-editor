package org.zimowski.bambi.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility capable of creating new public/private key pair, reading from 
 * exisiting file and saving new ones to a file.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class RsaKeyGenerator {

	private static final Logger log = LoggerFactory.getLogger(RsaKeyGenerator.class);
	
	private RsaKeyGenerator() {
	}

	public static void main(String[] args) {
//		RsaKeyGenerator.generateKeyPair(true, 1024, new File("private.key"), new File("public.key"));
		InputStream privateIs = RsaKeyGenerator.class.getResourceAsStream("/private.key");
		PrivateKey privateKey = RsaKeyGenerator.readPrivateKey(privateIs);
		log.info(privateKey.toString());
		InputStream publicIs = RsaKeyGenerator.class.getResourceAsStream("/public.key");
		PublicKey publicKey = RsaKeyGenerator.readPublicKey(publicIs);
		log.info(publicKey.toString());
	}
	
	public static KeyPair generateKeyPair(
			boolean createFiles, int size, 
			File privateKeyFile, File publicKeyFile) {

		KeyPair keypair = null;
		
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(size);
			keypair = keyGen.genKeyPair();
			PrivateKey privateKey = keypair.getPrivate();
			PublicKey publicKey = keypair.getPublic();
			
			if(createFiles) {
				RsaKeyGenerator.saveKeyToFile(privateKey, privateKeyFile);
				RsaKeyGenerator.saveKeyToFile(publicKey, publicKeyFile);
			}
			
			log.info("{}\n{}", privateKey, publicKey);
		}
		catch(NoSuchAlgorithmException e) {
			log.error(e.getMessage());
		}
		
		return keypair;
	}
	
	public static void saveKeyToFile(PublicKey key, File file) {
		try {
			X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(key.getEncoded());
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(x509EncodedKeySpec.getEncoded());
			fos.close();
		}
		catch(IOException e) {
			log.error("file error", e);
		}
	}
	
	public static void saveKeyToFile(PrivateKey key, File file) {
		try {
			PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(key.getEncoded());
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(pkcs8EncodedKeySpec.getEncoded());
			fos.close();
		}
		catch(IOException e) {
			log.error("file error", e);
		}
	}
	
	public static PublicKey readPublicKey(InputStream is) {
		
		KeyFactory keyFactory = null;
		
		try { keyFactory = KeyFactory.getInstance("RSA"); }
		catch(NoSuchAlgorithmException e) { 
			log.error(e.getMessage());
			return null;
		}		

		byte[] encodedKey = null;
		try {
			encodedKey = new byte[is.available()];
			is.read(encodedKey);
			is.close();
		}
		catch(IOException e) {
			log.error(e.getMessage());
		}
		X509EncodedKeySpec keySpec = null;			
		keySpec = new X509EncodedKeySpec(encodedKey);
		PublicKey key = null;
		try {
			key = keyFactory.generatePublic(keySpec);
		} catch (InvalidKeySpecException e) {
			log.error(e.getMessage());
		}
		return key;

	}
	
	public static PublicKey readPublicKeyFromFile(File file) throws FileNotFoundException {
		log.debug("reading: {}", file.getAbsolutePath());
		return RsaKeyGenerator.readPublicKey(new FileInputStream(file));
	}
	
	public static PrivateKey readPrivateKey(InputStream is) {
		KeyFactory keyFactory = null;
		
		try { keyFactory = KeyFactory.getInstance("RSA"); }
		catch(NoSuchAlgorithmException e) { 
			log.error(e.getMessage());
			return null;
		}		

		byte[] encodedKey = null;
		try {
			encodedKey = new byte[is.available()];
			is.read(encodedKey);
			is.close();
		}
		catch(IOException e) {
			log.error(e.getMessage());
		}
		PKCS8EncodedKeySpec keySpec = null;			
		keySpec = new PKCS8EncodedKeySpec(encodedKey);
		PrivateKey key = null;
		try {
			key = keyFactory.generatePrivate(keySpec);
		} catch (InvalidKeySpecException e) {
			log.error(e.getMessage());
		}
		return key;
		
	}
	
	public static PrivateKey readPrivateKeyFromFile(File file) throws FileNotFoundException {
		log.debug("reading: {}", file.getAbsolutePath());
		return RsaKeyGenerator.readPrivateKey(new FileInputStream(file));
	}
}
