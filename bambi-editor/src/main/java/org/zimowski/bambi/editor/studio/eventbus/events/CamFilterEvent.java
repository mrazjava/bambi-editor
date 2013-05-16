package org.zimowski.bambi.editor.studio.eventbus.events;

import org.zimowski.bambi.editor.studio.cam.CamFilterOps;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class CamFilterEvent extends BambiEvent {

	public static final int BAND_RED = 0;
	
	public static final int BAND_GREEN = 1;
	
	public static final int BAND_BLUE = 2;
	
	private CamFilterOps operation;
	
	public CamFilterEvent(CamFilterOps operation) {
		super(EV_CAM);
		this.operation = operation;
	}
	
	public CamFilterOps getOperation() {
		return operation;
	}
}