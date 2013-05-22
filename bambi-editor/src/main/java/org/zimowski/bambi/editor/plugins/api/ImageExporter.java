package org.zimowski.bambi.editor.plugins.api;

import java.awt.Component;
import java.util.Locale;

import javax.swing.SwingWorker;

/**
 * Image export plugin API. Technically any class implementing this interface 
 * could serve as the plugin, but because GUI must remain responsive, in 
 * practice such class should also extend {@link SwingWorker}. There is an 
 * abstract plugin base already defined exactly for that purpose, so rather 
 * than implementing this inteface directly, extend {@link AbstractImageExporter} 
 * instead.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public interface ImageExporter extends ImagePlugin {

	/**
	 * Initiates and executes the image export process. Implementing class is  
	 * responsible for notifying all monitor objects which in turn update 
	 * the UI. Without proper notifications the UI will not report export's  
	 * progress to end user. It is also up to the implementing class to listen 
	 * to abort agent for notification that export abort has been issued (not 
	 * required though highly recommended). 
	 * 
	 * @param exportDef export processing parameters
	 * @param component visual parent component invoking a plugin; handy if 
	 * 	plugin desires to display a dialog 
	 */
	public void export(ImageExportDef exportDef, Component parent);
	
	/**
	 * Retrieves tooltip from plugin's configuration.
	 * 
	 * @param locale language locale
	 * @return tooltip for this plugin's operation that could be attached to 
	 * 	a toolbar button
	 */
	public String getTooltip(Locale locale);
}