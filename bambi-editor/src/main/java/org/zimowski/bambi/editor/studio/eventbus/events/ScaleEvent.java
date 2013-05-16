package org.zimowski.bambi.editor.studio.eventbus.events;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class ScaleEvent extends ImageEvent {

	private int value;
	
	public ScaleEvent(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
}