package org.zimowski.bambi.editor.studio.eventbus.events;

import java.awt.image.BufferedImage;

import org.zimowski.bambi.editor.studio.image.ImageConduit;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class ModelLifecycleEvent extends ImageEvent {
	
	public enum ModelPhase {
		Initialized, BeforeChange, AfterChange, Reset
	}
	
	private ModelPhase phase;
	
	private boolean rgbReset = false;
	
	private boolean hsReset = false;
	
	private boolean cbReset = false;
	
	private ImageConduit imageConduit;


	public ModelLifecycleEvent(ModelPhase phase) {
		this.phase = phase;
		if(ModelPhase.Reset.equals(phase)) {
			rgbReset = hsReset = cbReset = true;
		}
	}
	
	public ModelPhase getPhase() {
		return phase;
	}

	/**
	 * @return true if red, green and blue channels should be reset
	 */
	public boolean isRgbReset() {
		return rgbReset;
	}

	public void setRgbReset(boolean rgbReset) {
		this.rgbReset = rgbReset;
	}

	/**
	 * @return true if hue and saturation should be reset
	 */
	public boolean isHsReset() {
		return hsReset;
	}

	public void setHsReset(boolean hsReset) {
		this.hsReset = hsReset;
	}

	/**
	 * @return true if contrast and brightness should be reset
	 */
	public boolean isCbReset() {
		return cbReset;
	}

	public void setCbReset(boolean cbReset) {
		this.cbReset = cbReset;
	}

	public void setImageConduit(ImageConduit imageConduit) {
		this.imageConduit = imageConduit;
	}
	
	public BufferedImage getImage() {
		BufferedImage image = null;
		if(imageConduit != null) image = imageConduit.getModifiedImage();
		return image;
	}
}