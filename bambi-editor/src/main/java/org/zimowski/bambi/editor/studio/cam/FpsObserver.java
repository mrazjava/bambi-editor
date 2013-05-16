package org.zimowski.bambi.editor.studio.cam;

/**
 * Can be implemented by components interested in observing frame per second 
 * video statstics.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public interface FpsObserver {

	/**
	 * Fired when the most recent frame per second values were obtained.
	 * 
	 * @param fpsLive most recently calculated live value
	 * @param fpsAverage most recently calculated average fps over a 
	 * 	historical sample 
	 * 
	 */
	public void fpsComputed(double fpsLive, double fpsAverage);
}
