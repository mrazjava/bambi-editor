package org.zimowski.bambi.editor.config;

/**
 * Configuration values used for image ouput generation. Particular 
 * implementation provides a single call to retrieve image output configuration  
 * hiding the complexity of parsing out underlying selection context. 
 * 
 * @author Adam Zimowski
 */
public interface ImageOutputConfigFacade {

	public int getTargetShape();

	public int getTargetWidth();

	public int getTargetHeight();

	public float getSelectorFactor();

	public String getSubmitUrl();

	public ImageOutputFormat getImageOutputFormat();
}