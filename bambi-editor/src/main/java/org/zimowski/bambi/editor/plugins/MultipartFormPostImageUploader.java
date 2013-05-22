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
import org.zimowski.bambi.editor.config.ConfigLoader;
import org.zimowski.bambi.editor.formpost.AbortException;
import org.zimowski.bambi.editor.formpost.MultipartFormPost;
import org.zimowski.bambi.editor.plugins.api.AbstractImageExporter;
import org.zimowski.bambi.editor.plugins.api.ExportProgressMonitor;
import org.zimowski.bambi.editor.plugins.api.ExportStateMonitor;

/**
 * Sends data using {@link MultipartFormPost} off an EDT thread keeping the GUI
 * responsive. Capable of processing server response according to a custom
 * protocol. Does not perform any GUI drawing, but delegates these tasks via
 * {@link ExportStateMonitor} and {@link ExportProgressMonitor}.
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
public class MultipartFormPostImageUploader extends AbstractImageExporter {

	public static final Logger log = LoggerFactory.getLogger(MultipartFormPostImageUploader.class);
	
	/**
	 * Name of the server side script that will receive and process the 
	 * uploaded image. For example, if using PHP this could be something like 
	 * processUpload.php which could perform authentication and load the 
	 * image to a database. Requires picture prefix identifier to assemble 
	 * full parameter name.
	 */
	public static final String CONFIG_SCRIPT = "processingScript";


	public MultipartFormPostImageUploader() {
	}

	@Override
	protected Void doInBackground() throws Exception {

		String fileName = new Long(System.currentTimeMillis()).toString() + 
				"." + exportDef.getFormat();
		String serverResponseMessage = null;
		boolean xferError = false;
		List<ExportStateMonitor> stateMonitors = exportDef.getStateMonitors();

		MultipartFormPost formPostWorker = null;
		try {
			String url = exportDef.getUrl() + getRemoteScript();
			formPostWorker = new MultipartFormPost(url);
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
			for (ExportStateMonitor m : stateMonitors) {
				m.exportError(e);
			}
			return null;
		}

		formPostWorker.setUserId(exportDef.getLoginId());
		formPostWorker.setPassword(exportDef.getPassword());
		formPostWorker.setProgressListener(exportDef.getProgressMonitor());
		formPostWorker.setKiller(exportDef.getAbortAgent());

		try {
			final byte[] imageBytes = exportDef.getImageBytes();
			formPostWorker.setParameter("filename", fileName, imageBytes);
			final Date startTime = new Date();
			for (ExportStateMonitor l : stateMonitors) {
				l.exportStarted(startTime);
			}
			InputStream is = formPostWorker.post(); // THIS IS WHERE DATA IS
													// SENT TO THE SERVER
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
					xferError = (bytesReceivedByServer != imageBytes.length);
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
				for (ExportStateMonitor l : stateMonitors)
					l.exportSuccess(bytesReceivedByServer);
			}
		} catch (AbortException ae) {
			for (ExportStateMonitor l : stateMonitors)
				l.exportAborted(ae.getBytesWritten());
		} catch (Exception e) {
			for (ExportStateMonitor l : stateMonitors)
				l.exportError(e);
		}

		return null;
	}
	
	private String getRemoteScript() {
		String key = selectorId + ConfigLoader.PARAM_SEPARATOR + CONFIG_SCRIPT;
		return configuration.getProperty(key);
	}
}