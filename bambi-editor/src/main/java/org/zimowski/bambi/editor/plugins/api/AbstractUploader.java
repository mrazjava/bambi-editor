package org.zimowski.bambi.editor.plugins.api;

import javax.swing.SwingWorker;

/**
 * Base for image upload plugins. Since upload process could be time consuming, 
 * the work must be done off the EDT so that GUI remains responsive. The 
 * interface upload method is what is ultimately called by the application, 
 * therefore {@link SwingWorker#execute()} must be called from inside the 
 * interface upload method.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public abstract class AbstractUploader 
	extends SwingWorker<Void, Void> implements ImageUploader {

}