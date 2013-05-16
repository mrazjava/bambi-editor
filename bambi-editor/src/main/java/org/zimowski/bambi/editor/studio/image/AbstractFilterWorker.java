package org.zimowski.bambi.editor.studio.image;

import javax.swing.SwingWorker;

import org.zimowski.bambi.editor.filters.ImageFilterOpSupport;
import org.zimowski.bambi.editor.studio.resources.toolbar.ToolbarIcons;

/**
 * @author Adam Zimowski (mrazjava)
 */
public abstract class AbstractFilterWorker extends SwingWorker<Void, Integer> 
	implements ImageFilterOpSupport {

	/**
	 * Value to display when rendering queue. This is typically a number such 
	 * as int or float representing adjusted setting
	 */
	protected String displayValue = null;
	
	public ToolbarIcons getToolbarIcon() {
		return ToolbarIcons.fromFilter(getMetaData());
	}
	
	public String getDisplayValue() {
		return displayValue;
	}
}