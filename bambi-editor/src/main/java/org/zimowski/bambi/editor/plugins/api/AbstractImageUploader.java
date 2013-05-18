package org.zimowski.bambi.editor.plugins.api;

import java.util.Date;
import java.util.List;

/**
 * Base for image upload plugins. Since upload process could be time consuming, 
 * the work must be done off the EDT so that GUI remains responsive. Plugins 
 * deriving from this base must override {@link #doInBackground()} with the 
 * actual upload logic along with proper notifications of monitor members. The 
 * {@link #done()} is already implemented as it informs monitors that upload 
 * has finished, but deriving class might want to override it (ensuring to 
 * call base version first!), if cleanup or additional post-processing is 
 * desired. All parameters from {@link #upload(ImageUploadDef)}, which is 
 * fully implemented and need not be overriden, are exposed within the 
 * protected member with an identical name.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public abstract class AbstractImageUploader extends AbstractImagePlugin implements ImageUploader {
	
	/**
	 * Upload definition objects
	 */
	protected ImageUploadDef uploadDef;

	
	@Override
	public void upload(ImageUploadDef uploadDef) {
		
		this.uploadDef = uploadDef;
		execute();
	}

	@Override
	protected void done() {
		Date doneTime = new Date();
		List<UploadStateMonitor> monitors = uploadDef.getStateMonitors();
		for(UploadStateMonitor monitor : monitors) 
			monitor.uploadFinished(doneTime);
	}
}