package org.zimowski.bambi.editor.plugins.api;

import javax.swing.SwingWorker;

/**
 * Image upload plugin API. Technically any class implementing this interface 
 * could serve as the plugin, but because GUI must remain responsive, in 
 * practice such class should also extend {@link SwingWorker}. There is an 
 * abstract plugin base already defined exactly for that purpose, so rather 
 * than implementing this inteface directly, extend {@link AbstractImageUploader} 
 * instead.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public interface ImageUploader extends ImagePlugin {

	/**
	 * Initiates and executes the image upload process. Implementing class is  
	 * responsible for notifying all monitor objects which in turn update 
	 * the UI. Without proper notifications the UI will not report upload 
	 * progress to end user. It is also up to the implementing class to listen 
	 * to abort agent for notification that upload abort has been issued (not 
	 * required though highly recommended). 
	 * 
	 * @param uploadDef upload parameters
	 */
	public void upload(ImageUploadDef uploadDef);
}