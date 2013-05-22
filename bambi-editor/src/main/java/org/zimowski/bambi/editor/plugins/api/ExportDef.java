package org.zimowski.bambi.editor.plugins.api;

import java.util.LinkedList;
import java.util.List;

/**
 * A simple struct which holds objects defining and handling generic export  
 * process.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class ExportDef {

	private String url;	
	private String loginId;	
	private String password;	
	private ExportAbortInformer abortAgent;	
	private ExportProgressMonitor progressMonitor;	
	private List<ExportStateMonitor> stateMonitors;

	/**
	 * @return destination endpoint receiving the exported data
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url destination endpoint receiving the exported data
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Login ID authentication token. Will be already encrypted if encryption 
	 * was requested.
	 * 
	 * @return authentication; may be null if not used
	 */
	public String getLoginId() {
		return loginId;
	}

	/** 
	 * @param loginId authentication; may be null if not used
	 */
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	/**
	 * Password authentication token. Will be already encrypted if encryption 
	 * was requested.
	 * 
	 * @return authentication; may be null if not used
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password authentication; may be null if not used
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return agent that can tell if export abort has been requested; null 
	 * 	safe
	 */
	public ExportAbortInformer getAbortAgent() {
		if(abortAgent == null) abortAgent = new ExportAbortInformer() {
			@Override
			public boolean isExportAborted() {
				return false;
			}
		};
		return abortAgent;
	}

	/**
	 * @param abortAgent agent that can tell if export abort has been requested
	 */
	public void setAbortAgent(ExportAbortInformer abortAgent) {
		this.abortAgent = abortAgent;
	}

	/**
	 * @return object to which export progress should be reported to; null 
	 * 	safe
	 */
	public ExportProgressMonitor getProgressMonitor() {
		if(progressMonitor == null) progressMonitor = new ExportProgressMonitor() {
			@Override
			public void bytesTransferred(int bytes) {
			}
		};
		return progressMonitor;
	}

	/**
	 * @param progressMonitor object to which export progress should be 
	 * 	reported to
	 */
	public void setProgressMonitor(ExportProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	/**
	 * @return objects to which state of the export should be reported to; 
	 * 	null safe
	 */
	public List<ExportStateMonitor> getStateMonitors() {
		if(stateMonitors == null) stateMonitors = new LinkedList<ExportStateMonitor>();
		return stateMonitors;
	}

	/**
	 * @param stateMonitors objects to which state of the export should be 
	 * 	reported to
	 */
	public void setStateMonitors(List<ExportStateMonitor> stateMonitors) {
		this.stateMonitors = stateMonitors;
	}
}