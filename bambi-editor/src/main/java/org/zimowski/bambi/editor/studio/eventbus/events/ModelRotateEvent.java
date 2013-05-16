package org.zimowski.bambi.editor.studio.eventbus.events;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class ModelRotateEvent extends ImageEvent {

	private int angleOfRotation;
	
	private int cumulativeAngle;
	

	public ModelRotateEvent(int angleOfRotation, int cumulativeAngle) {
		this.angleOfRotation = angleOfRotation;
		this.cumulativeAngle = cumulativeAngle;
	}

	public int getAngleOfRotation() {
		return angleOfRotation;
	}

	public int getCumulativeAngle() {
		return cumulativeAngle;
	}
}