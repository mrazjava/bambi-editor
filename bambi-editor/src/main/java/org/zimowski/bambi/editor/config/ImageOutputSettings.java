package org.zimowski.bambi.editor.config;

/**
 * Defines settings for individual image output. Members are package private as 
 * they can be set directly at the time of initialization. Values are read 
 * only afterwards.
 * 
 * @author Adam Zimowski
 */
public class ImageOutputSettings implements ImageOutputConfigFacade {

	int targetShape;
	
	int targetWidth;
	
	int targetHeight;
	
	float selectorFactor;

	String submitUrl;
	
	String radioLabel;

	ImageOutputFormat format;

	@Override
	public int getTargetShape() {
		return targetShape;
	}

	@Override
	public int getTargetWidth() {
		return targetWidth;
	}

	@Override
	public int getTargetHeight() {
		return targetHeight;
	}

	@Override
	public float getSelectorFactor() {
		return selectorFactor;
	}

	@Override
	public String getSubmitUrl() {
		return submitUrl;
	}

	public String getRadioLabel() {
		return radioLabel;
	}

	@Override
	public ImageOutputFormat getImageOutputFormat() {
		return format;
	}
}