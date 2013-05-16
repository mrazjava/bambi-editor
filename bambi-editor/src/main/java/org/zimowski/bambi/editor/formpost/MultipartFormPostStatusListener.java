package org.zimowski.bambi.editor.formpost;

import java.util.Date;

/**
 * @author Adam Zimowski (mrazjava)
 */
public interface MultipartFormPostStatusListener {

	/**
	 * Fired when post is finished. This is the last event in the lifecycle of  
	 * a post, and is always fired regardless of outcome.
	 * 
	 * @param when time (local system clock) at which transfer had completed
	 */
	public void uploadFinished(Date when);
	
	/**
	 * Fired immediately after transfer request has been issued.
	 * 
	 * @param when time (local system clock) at which transfer request was 
	 * 	issued
	 */
	public void uploadStarted(Date when);
	
	/**
	 * Fired at the end of the post if server confirmed success. The argument 
	 * value is reported by the server. This value should match exactly number 
	 * of bytes that were sent.
	 * 
	 * @param bytesReceived number of bytes received by the server
	 */
	public void uploadSuccess(long bytesReceived);
	
	/**
	 * Fired when generic exception was caught.
	 * 
	 * @param e exception that was caught
	 */
	public void uploadError(Exception e);
	
	/**
	 * Fired when {@link AbortException} was caught, which signals that user 
	 * aborted the transfer.
	 * 
	 * @param bytesWritten number of bytes written prior to abort
	 */
	public void uploadAborted(long bytesWritten);
}
