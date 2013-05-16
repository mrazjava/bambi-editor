package org.zimowski.bambi.editor.studio.eventbus.events;

import java.awt.Color;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class SelectorBackgroundEvent extends SelectorTypeEvent {

	private Color background;
	
	public SelectorBackgroundEvent(int type, Color backColor) {
		super(type);
		if(backColor == null) {
			throw new IllegalArgumentException("null background not allowed");
		}
		this.background = backColor;
	}
	
	public Color getBackgroundColor() {
		return background;
	}
}
