package org.zimowski.bambi.editor.plugins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.plugins.api.AbstractImageUploader;
import org.zimowski.bambi.editor.plugins.api.UploadAbortInformer;
import org.zimowski.bambi.editor.plugins.api.UploadProgressMonitor;
import org.zimowski.bambi.editor.plugins.api.UploadStateMonitor;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class FtpUploader extends AbstractImageUploader {
	
	private static final Logger log = LoggerFactory.getLogger(FtpUploader.class);
	
	public static final String CONFIG_WORKING_DIR = "workingDir";
	
	public static final String CONFIG_KEEP_ALIVE = "keepAlive";
	
	public static final String CONFIG_CONTROL_KEEP_ALIVE_TIMEOUT = "controlKeepAliveTimeout";
	
	public static final String CONFIG_DATA_TIMEOUT = "dataTimeout";
	
	public static final String CONFIG_CONNECT_TIMEOUT = "connectTimeout";
	
	public static final String CONFIG_LOCAL_PASSIVE_MODE = "localPassiveMode";
	
	public static final String CONFIG_BUFFER_SIZE = "bufferSize";
	
	private FTPClient ftp;

	
	public FtpUploader() {
		ftp = new FTPClient();
	}
	
	@Override
	protected Void doInBackground() throws Exception {

        String fileName = new Long(System.currentTimeMillis()).toString() + 
        		"." + uploadDef.getFormat();
        final List<UploadStateMonitor> stateMonitors = uploadDef.getStateMonitors();
        
		final Date startTime = new Date();
		for (UploadStateMonitor l : stateMonitors) {
			l.uploadStarted(startTime);
		}

		String host = new URL(uploadDef.getUrl()).getHost();
        ftp.connect(host);
        if(!ftp.login(uploadDef.getLoginId(), uploadDef.getPassword())) {
    		for (UploadStateMonitor l : stateMonitors) {
    			l.uploadError(new AuthenticationException("Invalid Login"));
    		}        	
        	return null;
        }

        ftp.setFileType(FTP.BINARY_FILE_TYPE);

        ftp.setKeepAlive(isKeepAlive());
        ftp.setControlKeepAliveTimeout(getControlKeepAliveTimeout());
        ftp.setDataTimeout(getDataTimeout());
        ftp.setConnectTimeout(getConnectTimeout());
        if(isLocalPassiveMode()) ftp.enterLocalPassiveMode();
        
        if(getWorkingDir() != null) ftp.changeWorkingDirectory(getWorkingDir());

        int reply = ftp.getReplyCode();
        log.info("FTP response: {}", reply);

        if(FTPReply.isPositiveCompletion(reply)) {
            log.info("FTP - connected");
        }
        else {
        	String error = "FTP - connection failed";
        	log.error(error);
			for(UploadStateMonitor m : stateMonitors) {
				m.uploadError(new IOException(error));
			}
			return null;
        }
        
        InputStream is = new ByteArrayInputStream(uploadDef.getImageBytes());
        log.debug("will send {} bytes", is.available());
        
    	CountingInputStream countingIs = new CountingInputStream(is) {

    		private UploadAbortInformer abortAgent;
    		private UploadProgressMonitor progressMonitor;
    		
    		{
    			abortAgent = uploadDef.getAbortAgent();
    			progressMonitor = uploadDef.getProgressMonitor();
    		}
    		
			@Override
			public int read(byte[] b) throws IOException {
				
				int bytesRead = super.read(b);
				
				if(abortAgent.isUploadAborted()) {
					ftp.abort();
					for (UploadStateMonitor l : stateMonitors) {
						l.uploadAborted(getByteCount());
					}
					// TODO: delete remote file
				}
				else {
					progressMonitor.bytesTransferred((int)getByteCount());
				}

				return bytesRead;
			}
    	};

    	ftp.setBufferSize(getBufferSize());
        boolean result = ftp.storeFile(fileName, countingIs);

        if(result) {
        	long bytesSent = countingIs.getByteCount();
        	log.info("stored remote file: {} ({} bytes)", fileName.toString(), bytesSent);
			for (UploadStateMonitor l : stateMonitors)
				l.uploadSuccess(bytesSent);
        }
        else {
			for (UploadStateMonitor l : stateMonitors)
				l.uploadError(new IOException("Upload failed due to unknown error"));        	
        }

        return null;
	}

	@Override
	protected void done() {
		super.done();
		try {
			ftp.logout();
		} 
		catch(IOException e) { log.error(e.getMessage()); }
		try { 
			ftp.disconnect();
		}
		catch(IOException e) { log.error(e.getMessage()); }
	}
	
	private String getWorkingDir() {
		String workingDir = configuration.getProperty(CONFIG_WORKING_DIR);
		log.debug("{}", workingDir);
		return workingDir;
	}
	
	private boolean isKeepAlive() {
		boolean keepAlive = true;
		try { keepAlive = Boolean.valueOf(configuration.getProperty(CONFIG_KEEP_ALIVE)); }
		catch(Exception e) {}
		log.debug("{}", keepAlive);
		return keepAlive;
	}
	
	private int getControlKeepAliveTimeout() {
		int controlKeepAliveTimeout = 3000; // 100 minutes
		try {
			controlKeepAliveTimeout = Integer.valueOf(CONFIG_CONTROL_KEEP_ALIVE_TIMEOUT);
			if(controlKeepAliveTimeout < 0) controlKeepAliveTimeout = 3000;
		}
		catch(Exception e) {}
		log.debug("{}", controlKeepAliveTimeout);
		return controlKeepAliveTimeout;
	}
	
	private int getDataTimeout() {
		int dataTimeout = 3000; // 100 minutes
		try {
			dataTimeout = Integer.valueOf(CONFIG_DATA_TIMEOUT);
			if(dataTimeout < 0) dataTimeout = 3000;
		}
		catch(Exception e) {}
		log.debug("{}", dataTimeout);
		return dataTimeout;
	}
	
	private int getConnectTimeout() {
		int connectTimeout = 3000; // 100 minutes
		try {
			connectTimeout = Integer.valueOf(CONFIG_CONNECT_TIMEOUT);
			if(connectTimeout < 0) connectTimeout = 3000;
		}
		catch(Exception e) {}
		log.debug("{}", connectTimeout);
		return connectTimeout;
	}
	
	private boolean isLocalPassiveMode() {
		boolean localPassiveMode = true;
		try { localPassiveMode = Boolean.valueOf(configuration.getProperty(CONFIG_LOCAL_PASSIVE_MODE)); }
		catch(Exception e) {}
		log.debug("{}", localPassiveMode);
		return localPassiveMode;
	}
	
	private int getBufferSize() {
		int bufferSize = 128;
		try { 
			bufferSize = Integer.valueOf(CONFIG_BUFFER_SIZE);
			if(bufferSize < 32) bufferSize = 128;
		}
		catch(Exception e) {}
		log.debug("{}", bufferSize);
		return bufferSize;
	}
}