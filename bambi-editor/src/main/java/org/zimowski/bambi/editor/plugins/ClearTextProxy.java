package org.zimowski.bambi.editor.plugins;

import org.zimowski.bambi.editor.plugins.api.TextEncrypter;

/**
 * Dummy proxy encrypter which simply passes thru original text value. As a 
 * result, no encryption is performed.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class ClearTextProxy implements TextEncrypter {

	@Override
	public String encrypt(String text) {
		return text;
	}
}