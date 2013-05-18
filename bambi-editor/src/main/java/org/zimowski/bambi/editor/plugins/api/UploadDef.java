package org.zimowski.bambi.editor.plugins.api;

import java.util.LinkedList;
import java.util.List;

/**
 * A simple struct which holds objects defining and handling generic upload 
 * process.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class UploadDef {

	private String url;	
	private String loginId;	
	private String password;	
	private UploadAbortInformer abortAgent;	
	private UploadProgressMonitor progressMonitor;	
	private List<UploadStateMonitor> stateMonitors;

	/**
	 * @return destination endpoint receiving the uploaded data
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url destination endpoint receiving the uploaded data
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
	 * @return agent that can tell if upload abort has been requested; null 
	 * 	safe
	 */
	public UploadAbortInformer getAbortAgent() {
		if(abortAgent == null) abortAgent = new UploadAbortInformer() {
			@Override
			public boolean isUploadAborted() {
				return false;
			}
		};
		return abortAgent;
	}

	/**
	 * @param abortAgent agent that can tell if upload abort has been requested
	 */
	public void setAbortAgent(UploadAbortInformer abortAgent) {
		this.abortAgent = abortAgent;
	}

	/**
	 * @return object to which upload progress should be reported to; null 
	 * 	safe
	 */
	public UploadProgressMonitor getProgressMonitor() {
		if(progressMonitor == null) progressMonitor = new UploadProgressMonitor() {
			@Override
			public void bytesTransferred(int bytes) {
			}
		};
		return progressMonitor;
	}

	/**
	 * @param progressMonitor object to which upload progress should be reported to
	 */
	public void setProgressMonitor(UploadProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	/**
	 * @return objects to which state of the upload should be reported to; 
	 * 	null safe
	 */
	public List<UploadStateMonitor> getStateMonitors() {
		if(stateMonitors == null) stateMonitors = new LinkedList<UploadStateMonitor>();
		return stateMonitors;
	}

	/**
	 * @param stateMonitors objects to which state of the upload should be reported to
	 */
	public void setStateMonitors(List<UploadStateMonitor> stateMonitors) {
		this.stateMonitors = stateMonitors;
	}
}