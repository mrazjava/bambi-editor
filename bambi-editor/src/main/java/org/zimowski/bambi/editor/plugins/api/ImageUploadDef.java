package org.zimowski.bambi.editor.plugins.api;

import org.zimowski.bambi.editor.config.ImageOutputFormat;

/**
 * A simple struct which holds objects defining and handling the image upload 
 * process.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class ImageUploadDef extends UploadDef {

	private byte[] imageBytes;
	private ImageOutputFormat format;
	
	/**
	 * @return the image to be uploaded
	 */
	public byte[] getImageBytes() {
		return imageBytes;
	}
	
	/**
	 * @param imageBytes the image to be uploaded
	 */
	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}
	
	/**
	 * @return output format of the image (PNG, JPG, etc)
	 */
	public ImageOutputFormat getFormat() {
		return format;
	}
	
	/**
	 * @param format output format of the image (PNG, JPG, etc).
	 */
	public void setFormat(ImageOutputFormat format) {
		this.format = format;
	}
}