package org.zimowski.bambi.editor.studio.image;

import java.awt.Color;

/**
 * Defines commands that can be issued on image selector. Should be implemented 
 * by image container that drives selector operations if it wishes to be 
 * notified when user requested a change.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public interface SelectorCommandListener {

	/**
	 * @param show true to show; false to hide
	 */
	public void showSelector(boolean show);
	
	/**
	 * @param show true to show; false to hide
	 */
	public void showSelectorPosition(boolean show);
	
	/**
	 * @param show true to show; false to hide
	 */
	public void showSelectorSize(boolean show);

	/**
	 * @param color background color to use when constructing a jpg
	 */
	public void jpgSelectorBackgroundChanged(Color color);
	
	/**
	 * @param selector number following the defined order; first selector 
	 * 	starts with 1.
	 */
	public void selectorChanged(int index);
}
