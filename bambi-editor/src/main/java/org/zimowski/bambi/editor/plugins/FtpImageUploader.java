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
import org.zimowski.bambi.editor.plugins.api.AbstractImageExporter;
import org.zimowski.bambi.editor.plugins.api.ExportAbortInformer;
import org.zimowski.bambi.editor.plugins.api.ExportProgressMonitor;
import org.zimowski.bambi.editor.plugins.api.ExportStateMonitor;

/**
 * Image upload plugin utilizing FTP protocol. Supports basic customization 
 * via CONFIG_* parameters.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class FtpImageUploader extends AbstractImageExporter {
	
	private static final Logger log = LoggerFactory.getLogger(FtpImageUploader.class);
	
	public static final String CONFIG_WORKING_DIR = "workingDir";
	
	public static final String CONFIG_KEEP_ALIVE = "keepAlive";
	
	public static final String CONFIG_CONTROL_KEEP_ALIVE_TIMEOUT = "controlKeepAliveTimeout";
	
	public static final String CONFIG_DATA_TIMEOUT = "dataTimeout";
	
	public static final String CONFIG_CONNECT_TIMEOUT = "connectTimeout";
	
	public static final String CONFIG_LOCAL_PASSIVE_MODE = "localPassiveMode";
	
	public static final String CONFIG_BUFFER_SIZE = "bufferSize";
	
	private FTPClient ftp;

	
	public FtpImageUploader() {
		ftp = new FTPClient();
	}
	
	@Override
	protected Void doInBackground() throws Exception {

        String fileName = new Long(System.currentTimeMillis()).toString() + 
        		"." + exportDef.getFormat();
        final List<ExportStateMonitor> stateMonitors = exportDef.getStateMonitors();
        
		final Date startTime = new Date();
		for (ExportStateMonitor l : stateMonitors) {
			l.exportStarted(startTime);
		}

		String host = new URL(exportDef.getUrl()).getHost();
        ftp.connect(host);
        if(!ftp.login(exportDef.getLoginId(), exportDef.getPassword())) {
    		for (ExportStateMonitor l : stateMonitors) {
    			l.exportError(new AuthenticationException("Invalid Login"));
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
			for(ExportStateMonitor m : stateMonitors) {
				m.exportError(new IOException(error));
			}
			return null;
        }
        
        InputStream is = new ByteArrayInputStream(exportDef.getImageBytes());
        log.debug("will send {} bytes", is.available());
        
    	CountingInputStream countingIs = new CountingInputStream(is) {

    		private ExportAbortInformer abortAgent;
    		private ExportProgressMonitor progressMonitor;
    		
    		{
    			abortAgent = exportDef.getAbortAgent();
    			progressMonitor = exportDef.getProgressMonitor();
    		}
    		
			@Override
			public int read(byte[] b) throws IOException {
				
				int bytesRead = super.read(b);
				
				if(abortAgent.isExportAborted()) {
					ftp.abort();
					for (ExportStateMonitor l : stateMonitors) {
						l.exportAborted(getByteCount());
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
			for (ExportStateMonitor l : stateMonitors)
				l.exportSuccess(bytesSent);
        }
        else {
			for (ExportStateMonitor l : stateMonitors)
				l.exportError(new IOException("Upload failed due to unknown error"));        	
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