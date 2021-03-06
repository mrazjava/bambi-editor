package org.zimowski.bambi.editor.plugins.api;

/**
 * An object implementing this interface will be notified of data export  
 * progress. Typically this is a GUI component such as a progress bar.
 * 
 * @author Adam Zimowski (mrazjava)
 * @see ExportStateMonitor
 * @see ExportAbortInformer
 */
public interface ExportProgressMonitor {
    
	/**
	 * Fired every time when data buffer was transferred.
	 * 
	 * @param bytes total bytes transferred so far.
	 */
	public void bytesTransferred(int bytes);
}