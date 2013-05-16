package org.zimowski.bambi.editor.config;

import org.apache.commons.lang3.StringUtils;

/**
 * Supported output image formats. Output format is used when user sends 
 * clipped image to the remote destination or saves a modified file to a 
 * computer.
 * 
 * @author Adam Zimowski
 */
public enum ImageOutputFormat {

	png, jpg;
	
	public static String getValidValues() {
		ImageOutputFormat[] vals = ImageOutputFormat.values();
		String validVals = StringUtils.join(vals, ",");
		return validVals;
	}
}
