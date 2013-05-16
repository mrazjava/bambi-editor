package org.zimowski.bambi.editor.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.config.ImageOutputFormat;
import org.zimowski.bambi.editor.formpost.AbortException;
import org.zimowski.bambi.editor.formpost.MultipartFormPost;
import org.zimowski.bambi.editor.plugins.api.AbstractUploader;
import org.zimowski.bambi.editor.plugins.api.UploadAbortInformer;
import org.zimowski.bambi.editor.plugins.api.UploadProgressMonitor;
import org.zimowski.bambi.editor.plugins.api.UploadStateMonitor;

/**
 * Sends data using {@link MultipartFormPost} off an EDT thread keeping the
 * GUI responsive. Capable of processing server response according to a custom
 * protocol. Does not perform any gui drawing, but delegates these tasks via 
 * {@link UploadStateMonitor}.
 * <p>
 * The spec for this protocol server response is multi-line plain text in
 * KEY|VALUE format where KEY is the message identifier which Bambi knows about
 * and VALUE is the actual message.
 * </p>
 * The following KEYS are supported:
 * <ul>
 * <li>STATUS - one of the following: OK,ERROR - required</li>
 * <li>RECEIVED - bytes received (number) - required</li>
 * <li>PROCESSED - bytes processed by the server (number) - required</li>
 * <li>DATE - timestamp when submission was processed - optional</li>
 * <li>MSG - info message if STATUS=OK, error message if STATUS=ERROR - optional
 * </li>
 * </ul>
 * Example response 1 (success):
 * 
 * <pre>
 * STATUS|OK
 * RECEIVED|42933
 * PROCESSED|42933
 * DATE|2013-01-10 13:27:30
 * </pre>
 * 
 * Example response 2 (error):
 * 
 * <pre>
 * STATUS|ERROR
 * RECEIVED|2342
 * PROCESSED|0
 * DATE|2013-01-02 09:07:11
 * MSG|Incomplete Payload. Expected 292342 bytes.
 * </pre>
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class MultipartFormPostUploader extends AbstractUploader {

	public static final Logger log = LoggerFactory.getLogger(MultipartFormPostUploader.class);
	
	private MultipartFormPost post;

	private byte[] bytes;
	
	private String fileName;
	
	List<UploadStateMonitor> stateMonitors;

	
	public MultipartFormPostUploader() {
	}
	
	@Override
	public void upload(
			byte[] image, 
			ImageOutputFormat format, 
			String url,
			String loginId, 
			String password, 
			UploadAbortInformer abortInformer,
			UploadProgressMonitor progressMonitor,
			List<UploadStateMonitor> stateMonitors) {
		
		bytes = image;
		fileName = new Long(System.currentTimeMillis()).toString() + "." + format;
		this.stateMonitors = stateMonitors;
		
		try {
			post = new MultipartFormPost(url);
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
			Date now = new Date();
			for(UploadStateMonitor m : stateMonitors) {
				m.uploadError(e);
				m.uploadFinished(now);
			}
		}
		
		post.setUserId(loginId);
		post.setPassword(password);
		post.setProgressListener(progressMonitor);
		post.setKiller(abortInformer);
		
		execute();
	}

	@Override
	protected Void doInBackground() throws Exception {

		String serverResponseMessage = null;
		boolean xferError = false;

		try {
			post.setParameter("filename", fileName, bytes);
			final Date startTime = new Date();
			for(UploadStateMonitor l : stateMonitors) {
				l.uploadStarted(startTime);
			}
			InputStream is = post.post(); // THIS IS WHERE DATA IS SENT TO THE SERVER
			BufferedReader bin = new BufferedReader(new InputStreamReader(is));
			String line = null;
			log.info("waiting for server response...");
			Map<String, String> response = new HashMap<String, String>();
			while ((line = bin.readLine()) != null) {
				log.info("SERVER: {}", line);
				int split = line.indexOf('|');
				if (split >= 0) {
					try {
						String key = line.substring(0, split);
						String value = line.substring(split + 1);
						response.put(key, value);
					} catch (IndexOutOfBoundsException e) {
						log.warn(e.getMessage());
					}
				}
			}
			xferError = !"OK".equals(response.get("STATUS"));
			long bytesReceivedByServer = 0;
			if (!xferError) {
				try {
					String bytesStr = response.get("RECEIVED");
					bytesReceivedByServer = Long.parseLong(bytesStr);
					xferError = (bytesReceivedByServer != bytes.length);
				} catch (NumberFormatException nfe) {
					xferError = true;
				}
			}

			if (xferError) {
				// try to retrieve message from server, if any; server can
				// send error messages on line 2 of the response
				serverResponseMessage = response.get("MSG");
				if (StringUtils.isEmpty(serverResponseMessage)) {
					serverResponseMessage = "Server did not say what's wrong.";
				}
				throw new IOException(serverResponseMessage);
			} else {
				for(UploadStateMonitor l : stateMonitors)
					l.uploadSuccess(bytesReceivedByServer);
			}
		} catch (AbortException ae) {
			for(UploadStateMonitor l : stateMonitors)
				l.uploadAborted(ae.getBytesWritten());
		} catch (Exception e) {
			for(UploadStateMonitor l : stateMonitors)
				l.uploadError(e);
		}

		return null;
	}
	
	@Override
	protected void done() {
		Date doneTime = new Date();
		for(UploadStateMonitor l : stateMonitors)
			l.uploadFinished(doneTime);
	}
}