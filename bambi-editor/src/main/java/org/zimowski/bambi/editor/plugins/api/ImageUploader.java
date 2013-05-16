package org.zimowski.bambi.editor.plugins.api;

import java.util.List;

import org.zimowski.bambi.editor.config.ImageOutputFormat;

/**
 * @author Adam Zimowski (mrazjava)
 */
public interface ImageUploader {

	/**
	 * Initiates and executes the image upload process. Implementing class is  
	 * responsible for notifying all monitor objects which in turn update 
	 * the UI. Without proper notifications the UI will not report upload 
	 * progress to end user. It is also up to the implementing class to listen 
	 * to abort agent for notification that upload abort has been issued (not 
	 * required though highly recommended). 
	 * 
	 * @param image the image chosen by end user
	 * @param format output format of the image (PNG, JPG, etc).
	 * @param url destination endpoint receiving the image
	 * @param loginId authentication; may be null if not configured
	 * @param password authentication; may be null if not configured
	 * @param abortAgent agent that will report if upload should be aborted 
	 * @param progressMonitor object to which progress should be reported to
	 * @param stateMonitors objects to which upload states should be reported 
	 * 	to; null safe
	 */
	public void upload(
			byte[] image, 
			ImageOutputFormat format, 
			String url, 
			String loginId, 
			String password, 
			UploadAbortInformer abortAgent, 
			UploadProgressMonitor progressMonitor, 
			List<UploadStateMonitor> stateMonitors);
}