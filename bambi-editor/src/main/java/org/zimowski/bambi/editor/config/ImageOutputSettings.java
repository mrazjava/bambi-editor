package org.zimowski.bambi.editor.config;

import org.zimowski.bambi.editor.studio.image.ImageContainer;

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
	
	boolean ratioPreserved;

	ImageOutputFormat format;

	
	public ImageOutputSettings() {
		// defaults
		ratioPreserved = false;
		targetShape = 1;
		targetWidth = ImageContainer.DEFAULT_SELECTOR_WIDTH;
		targetHeight = ImageContainer.DEFAULT_SELECTOR_HEIGHT;
		selectorFactor = 1f;
	}
	
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
	public boolean isRatioPreserved() {
		return ratioPreserved;
	}

	@Override
	public ImageOutputFormat getImageOutputFormat() {
		return format;
	}
}