package org.zimowski.bambi.editor.plugins.api;


/**
 * Allows to inform interested parties performing upload (plugins, etc) that 
 * abort has been requested or issued and transfer should be halted.
 * 
 * @author Adam Zimowski (mrazjava)
 * @see UploadProgressMonitor
 * @see UploadStateMonitor
 */
public interface UploadAbortInformer {

	/**
	 * Allows to check during upload in progress if abort request has been 
	 * issued. This method is typically called from within the upload loop on 
	 * every iteration and said loop should be broken if abort has been 
	 * detected.
	 * 
	 * @return true if transfer in progress has been aborted
	 */
	public boolean isUploadAborted();
}