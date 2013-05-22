package org.zimowski.bambi.editor.plugins.api;

import java.util.Date;

import org.zimowski.bambi.editor.formpost.AbortException;

/**
 * Allows to handle different stages of the export process. Objects to 
 * implement this are typically various GUI components that respond 
 * accordingly.
 * 
 * @author Adam Zimowski (mrazjava)
 * @see ExportProgressMonitor
 * @see ExportAbortInformer
 */
public interface ExportStateMonitor {

	/**
	 * Fired when export is finished. This is the last event in the lifecycle 
	 * of the export process, and is always fired regardless of outcome.
	 * 
	 * @param when time (local system clock) at which transfer had completed
	 */
	public void exportFinished(Date when);
	
	/**
	 * Fired immediately after export request has been issued.
	 * 
	 * @param when time at which export request was issued
	 */
	public void exportStarted(Date when);
	
	/**
	 * Fired at the end of the export if destination confirmed success. The  
	 * argument value is typically reported by destination endpoint (server 
	 * script, etc). This value should match exactly number of bytes that 
	 * were sent.
	 * 
	 * @param bytesReceived number of bytes received (by the server, etc.)
	 */
	public void exportSuccess(long bytesReceived);
	
	/**
	 * Fired when generic exception was caught.
	 * 
	 * @param e exception that was caught
	 */
	public void exportError(Exception e);
	
	/**
	 * Fired when {@link AbortException} was caught, which signals that user 
	 * aborted the export.
	 * 
	 * @param bytesWritten number of bytes written prior to abort
	 */
	public void exportAborted(long bytesWritten);
}
