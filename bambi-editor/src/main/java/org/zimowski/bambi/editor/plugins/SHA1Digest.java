package org.zimowski.bambi.editor.plugins;

import org.zimowski.bambi.commons.CryptoUtil;
import org.zimowski.bambi.editor.plugins.api.AbstractPluginAdapter;
import org.zimowski.bambi.editor.plugins.api.TextEncrypter;

/**
 * One way hash encrypter utilizing SHA-1 digest. After hashing, bytes are 
 * hex encoded. Returned value is lower case.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class SHA1Digest extends AbstractPluginAdapter implements TextEncrypter {

	@Override
	public String encrypt(String text) {
		return CryptoUtil.hash(text, "SHA-1");
	}
}
