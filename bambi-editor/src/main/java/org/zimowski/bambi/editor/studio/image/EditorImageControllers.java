package org.zimowski.bambi.editor.studio.image;

import org.zimowski.bambi.editor.ViewportMouseListener;
import org.zimowski.bambi.editor.config.ImageOutputConfigFacade;
import org.zimowski.bambi.editor.studio.Editor;

/**
 * Defines components of {@link Editor} that {@link ImagePanel} must know 
 * about.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public interface EditorImageControllers {

	public SelectorObserver getSelectorObserver();
	
	public ViewportMouseListener getMouseInputListener();
	
	public ImageOutputConfigFacade getImageOutputConfigFacade();
}
