package org.zimowski.bambi.editor.studio.eventbus.events;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class BambiEvent {
	
	public final static int EV_IMAGE = 0;
	
	public final static int EV_CAM = 1;
	
	private int eventType;
	
	public BambiEvent(int eventType) {
		this.eventType = eventType;
	}
	
	public int getEventType() {
		return eventType;
	}
}