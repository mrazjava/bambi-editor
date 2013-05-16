package org.zimowski.bambi.test;

import java.io.IOException;

import org.zimowski.bambi.editor.plugins.ClearTextProxy;
import org.zimowski.bambi.editor.plugins.MD5Digest;
import org.zimowski.bambi.editor.plugins.RsaCipher;
import org.zimowski.bambi.editor.plugins.SHA1Digest;
import org.zimowski.bambi.editor.plugins.SHA256Digest;
import org.zimowski.bambi.editor.plugins.api.TextEncrypter;

import junit.framework.TestCase;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class CryptoTest extends TestCase {
	
	private static final String TEST_STRING = "hello world";

	public void testMd5() {
		MD5Digest md5 = new MD5Digest();
		String hash = md5.encrypt(TEST_STRING);
		assertEquals("5eb63bbbe01eeed093cb22bb8f5acdc3", hash);		
	}
	
	public void testSha1() {
		SHA1Digest sha = new SHA1Digest();
		String hash = sha.encrypt(TEST_STRING);
		assertEquals("2aae6c35c94fcfb415dbe95f408b9ce91ee846ed", hash);
	}
	
	public void testSha256() {
		SHA256Digest sha = new SHA256Digest();
		String hash = sha.encrypt(TEST_STRING);
		assertEquals("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9", hash);
	}
	
	public void testRsa() throws IOException {
		// use sample keypair in bambi-commons resources
		RsaCipher cipher = new RsaCipher("/public.key");
		String encrypted = cipher.encrypt(TEST_STRING);
		String clear = cipher.decrypt(encrypted, "/private.key");
		assertEquals(TEST_STRING, clear);
	}
	
	public void testClear() {
		TextEncrypter dummy = new ClearTextProxy();
		String result = dummy.encrypt(TEST_STRING);
		assertEquals(TEST_STRING, result);
	}
}