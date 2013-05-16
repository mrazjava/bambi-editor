package org.zimowski.bambi.editor.studio.eventbus.events;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class ThumbAddEvent extends ImageEvent {

	private BufferedImage thumbSource;
	
	private File thumbFile;
	
	private boolean selected = true;
	
	public ThumbAddEvent(BufferedImage thumbSource, File thumbFile) {
		this.thumbSource = thumbSource;
		this.thumbFile = thumbFile;
	}

	public BufferedImage getThumbSource() {
		return thumbSource;
	}

	public File getThumbFile() {
		return thumbFile;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}