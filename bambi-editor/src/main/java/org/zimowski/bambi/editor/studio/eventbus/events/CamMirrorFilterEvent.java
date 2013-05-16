package org.zimowski.bambi.editor.studio.eventbus.events;

import org.zimowski.bambi.editor.studio.cam.CamFilterOps;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class CamMirrorFilterEvent extends CamFilterEvent {
	
	private int band;
	
	private boolean on;
	
	/**
	 * @param band
	 * @param on true if band is turned on; false if off
	 */
	public CamMirrorFilterEvent(int band, boolean on) {
		super(CamFilterOps.Mirror);
		this.band = band;
		this.on = on;
	}
	
	public int getBand() {
		return band;
	}
	
	public boolean isOn() {
		return on;
	}
}