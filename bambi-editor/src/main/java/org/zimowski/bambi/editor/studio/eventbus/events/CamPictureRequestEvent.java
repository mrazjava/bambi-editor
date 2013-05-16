package org.zimowski.bambi.editor.studio.eventbus.events;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class CamPictureRequestEvent extends BambiEvent {

	public CamPictureRequestEvent() {
		super(BambiEvent.EV_CAM);
	}
}