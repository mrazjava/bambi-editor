package org.zimowski.bambi.editor.config;

/**
 * Defines settings for individual image output. Members are package private as 
 * they can be set directly at the time of applet initialization. Values are 
 * read only afterwards.
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

	/* (non-Javadoc)
	 * @see org.zimowski.bambi.ImageOutput#getTargetShape()
	 */
	@Override
	public int getTargetShape() {
		return targetShape;
	}

	/* (non-Javadoc)
	 * @see org.zimowski.bambi.ImageOutput#getTargetWidth()
	 */
	@Override
	public int getTargetWidth() {
		return targetWidth;
	}

	/* (non-Javadoc)
	 * @see org.zimowski.bambi.ImageOutput#getTargetHeight()
	 */
	@Override
	public int getTargetHeight() {
		return targetHeight;
	}

	/* (non-Javadoc)
	 * @see org.zimowski.bambi.ImageOutput#getSelectorFactor()
	 */
	@Override
	public float getSelectorFactor() {
		return selectorFactor;
	}

	/* (non-Javadoc)
	 * @see org.zimowski.bambi.ImageOutput#getSubmitUrl()
	 */
	@Override
	public String getSubmitUrl() {
		return submitUrl;
	}

	public String getRadioLabel() {
		return radioLabel;
	}

	/* (non-Javadoc)
	 * @see org.zimowski.bambi.ImageOutput#getFormat()
	 */
	@Override
	public ImageOutputFormat getImageOutputFormat() {
		return format;
	}
}