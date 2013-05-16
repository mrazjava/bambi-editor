package org.zimowski.bambi.editor.studio.eventbus.events;

import java.util.List;

import org.zimowski.bambi.editor.studio.eventbus.ImageFilterQueue;
import org.zimowski.bambi.editor.studio.resources.toolbar.ToolbarIcons;

/**
 * Fired whenever state of the {@link ImageFilterQueue} changes.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class ImageFilterQueueEvent extends ImageEvent {

	private List<ToolbarIcons> icons;
	
	private boolean aborted = false;
	
	public ImageFilterQueueEvent(List<ToolbarIcons> icons) {
		this.icons = icons;
	}
	
	public List<ToolbarIcons> getIcons() {
		return icons;
	}

	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}
}
