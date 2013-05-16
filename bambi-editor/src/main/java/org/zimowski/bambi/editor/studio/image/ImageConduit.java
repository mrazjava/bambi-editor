package org.zimowski.bambi.editor.studio.image;

import java.awt.image.BufferedImage;

/**
 * Provider of source image with all transformations applied.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public interface ImageConduit {

	/**
	 * @return image on display, unscaled but with all filters applied
	 */
	public BufferedImage getModifiedImage();
}
