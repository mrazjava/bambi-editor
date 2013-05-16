package org.zimowski.bambi.editor.studio.eventbus.events;

import org.zimowski.bambi.editor.studio.cam.CamFilterOps;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class CamRgbFilterEvent extends CamFilterEvent {
	
	/**
	 * @see {@link CamFilterEvent#BAND_RED}, {@link CamFilterEvent#BAND_GREEN}, 
	 * {@link CamFilterEvent#BAND_BLUE}
	 */
	private int band;
	
	private int value;
	
	public CamRgbFilterEvent(int band, int value) {
		super(CamFilterOps.Rgb);
		this.band = band;
		this.value = value;
	}
	
	public int getBand() {
		return band;
	}
	
	public int getValue() {
		return value;
	}
}