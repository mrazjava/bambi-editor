package org.zimowski.bambi.editor.formpost;

/**
 * An object implementing this interface will be notified of data transfer 
 * progress. To be informed when transfer completes implmeent 
 * {@link MultipartFormPostResultListener}.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public interface MultipartFormPostListener {
    
	/**
	 * Fired every time when buffer chunk was transferred.
	 * 
	 * @param bytes total bytes transferred so far.
	 */
	public void bytesTransferred(int bytes);
}