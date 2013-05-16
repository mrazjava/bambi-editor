package org.zimowski.bambi.editor;

import java.awt.event.MouseEvent;

/**
 * @author Adam Zimowski (mrazjava)
 */
public interface ViewportMouseListener {

	/**
	 * @param e mouse event
	 * @param pixelColor color under mouse at the time when event was fired
	 */
	public void mouseMoved(MouseEvent e, int pixelColor);
	
	public void mouseDragged(MouseEvent e, int pixelColor);
	
	public void mouseExited(MouseEvent e);
}
