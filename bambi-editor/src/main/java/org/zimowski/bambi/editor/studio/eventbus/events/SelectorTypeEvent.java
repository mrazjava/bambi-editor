package org.zimowski.bambi.editor.studio.eventbus.events;


/**
 * @author Adam Zimowski (mrazjava)
 */
public class SelectorTypeEvent extends ImageEvent {

	private int type;
	
	
	public SelectorTypeEvent(int selectorType) {
		type = selectorType;
	}
	
	public int getType() {
		return type;
	}
	
	/**
	 * @return color instance if color is defined; null if default color
	 *
	public Color getBackColor() {
		return backgroundColor;
	}*/
}