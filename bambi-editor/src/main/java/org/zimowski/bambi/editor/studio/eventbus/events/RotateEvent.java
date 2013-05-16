package org.zimowski.bambi.editor.studio.eventbus.events;

import org.zimowski.bambi.editor.studio.image.ImageTransformListener.RotateDirection;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class RotateEvent extends ImageEvent {

	private RotateDirection direction;
	
	public RotateEvent(RotateDirection direction) {
		this.direction = direction;
	}

	public RotateDirection getDirection() {
		return direction;
	}
}