package org.zimowski.bambi.editor.studio.eventbus.events;

import java.awt.image.BufferedImage;
import java.util.Date;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

/**
 * Indicates that picture was taken from live video feed.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class CamPictureTakenEvent extends BambiEvent {

	private MBFImage picture;
	
	private Date timeTaken;
	
	public CamPictureTakenEvent(MBFImage picture) {
		super(BambiEvent.EV_CAM);
		this.picture = picture;
		timeTaken = new Date();
	}
	
	/**
	 * @return frame of the video taken as picture
	 */
	public BufferedImage getPicture() {
		return ImageUtilities.createBufferedImageForDisplay(picture);
	}
	
	public Date getTimeTaken() {
		return timeTaken;
	}
}