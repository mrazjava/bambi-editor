package org.zimowski.bambi.editor.studio.eventbus.events;

import org.zimowski.bambi.editor.filters.ImageFilterOps;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class ImageFilterEvent extends ImageEvent {
		
	private ImageFilterOps filter;
	
	private int value;

	public ImageFilterEvent(ImageFilterOps filter) {
		this.filter = filter;
	}
	
	public ImageFilterEvent(ImageFilterOps filter, int value) {
		this(filter);
		this.value = value;
	}
	
	public ImageFilterOps getFilter() {
		return filter;
	}

	public int getValue() {
		return value;
	}
}