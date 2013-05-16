package org.zimowski.bambi.editor.studio.image;

/**
 * @author Adam Zimowski (mrazjava)
 */
public interface SelectorObserver {

	/**
	 * Fired when selector was moved
	 * 
	 * @param x - new x-coordinate of selector's right upper corner
	 * @param y - new y-coordinate of selector's right upper corner
	 */
	public void selectorMoved(int x, int y);
	
	/**
	 * Fired when selector size was modified
	 * 
	 * @param width - new width
	 * @param height - new height
	 */
	public void selectorResized(int width, int height);
	
	/**
	 * Fired when selector becomes hidden.
	 */
	public void selectorClosed();
}
