package org.zimowski.bambi.editor.studio.eventbus.events;

import java.io.File;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class ImageLoadEvent extends ImageEvent {
	
	private File imageFile;
	
	private boolean autoLoaded = false;
	
	private boolean loadThumb = true;
	
	public ImageLoadEvent(File imageFile) {
		super();
		this.imageFile = imageFile;
	}
	
	public ImageLoadEvent(File imageFile, boolean autoLoad) {
		this(imageFile);
		this.autoLoaded = autoLoad;
	}
	
	public File getImageFile() {
		return imageFile;
	}

	public boolean isAutoLoaded() {
		return autoLoaded;
	}

	public boolean isLoadThumb() {
		return loadThumb;
	}

	public void setLoadThumb(boolean loadThumb) {
		this.loadThumb = loadThumb;
	}
}