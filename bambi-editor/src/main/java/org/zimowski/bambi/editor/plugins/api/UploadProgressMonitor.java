package org.zimowski.bambi.editor.plugins.api;

/**
 * An object implementing this interface will be notified of data transfer 
 * progress. To be informed when transfer completes implmeent 
 * {@link MultipartFormPostResultListener}.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public interface UploadProgressMonitor {
    
	/**
	 * Fired every time when data buffer was transferred.
	 * 
	 * @param bytes total bytes transferred so far.
	 */
	public void bytesTransferred(int bytes);
}