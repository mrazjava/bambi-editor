package org.zimowski.bambi.editor.plugins.api;


/**
 * Allows to inform interested parties performing export (plugins, etc) that 
 * abort has been requested or issued and the process should be halted.
 * 
 * @author Adam Zimowski (mrazjava)
 * @see ExportProgressMonitor
 * @see ExportStateMonitor
 */
public interface ExportAbortInformer {

	/**
	 * Allows to check during the export in progress if abort request has been 
	 * issued. This method is typically called from within the export loop on 
	 * every iteration and said loop should be broken if abort has been 
	 * detected.
	 * 
	 * @return true if export in progress has been aborted
	 */
	public boolean isExportAborted();
}