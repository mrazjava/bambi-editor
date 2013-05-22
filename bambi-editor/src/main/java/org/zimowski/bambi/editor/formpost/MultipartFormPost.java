package org.zimowski.bambi.editor.formpost;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.plugins.api.ExportAbortInformer;
import org.zimowski.bambi.editor.plugins.api.ExportProgressMonitor;

/**
 * Helps to send POST HTTP requests with various form data and also allows 
 * for progress to be shown including files. Cookies can be added to be 
 * included in the request.
 * 
 * @author Tomer Petel
 * @author Adam Zimowski (mrazjava)
 * @version 0.01 This is an Enhancement of ClientHTTPRequest, written by Vlad
 *          Patryshev
 * @version 0.1 Integrated into Imager applet with minor modifications.
 * @version 1.0 Fixed raw type map warnings by parametarizing data types. 
 * 	Functional enhancements to improve two way communication. Defined return 
 *  protocol.
 */
public class MultipartFormPost {

	private static final Logger log = LoggerFactory.getLogger(MultipartFormPost.class);
	
	private URL url = null;
	
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	private Map<String, String> cookies = new HashMap<String, String>();
	
	private ExportProgressMonitor progressListener = null;
	
	private ExportAbortInformer killer = null;
	
	private StringBuffer cookieList = new StringBuffer();
	
	private int bufferSize = 1024;
	
	private static Random random = new Random();
	
	private String boundary = "---------------------------" + randomString()
			+ randomString() + randomString();
	
	/**
	 * In bytes
	 */
	private int fileSize = 0;
	
	private String userId = null;
	
	private String password = null;

	
	protected URLConnection getConnection(int contentLength) throws Exception {
		
		if(fileSize == 0)
			throw new IllegalStateException("fileSize must be set!");
		
		HttpURLConnection connection = 
				(HttpURLConnection)url.openConnection();
		
		connection.setFixedLengthStreamingMode(contentLength);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);
		if(userId != null) {
			connection.setRequestProperty("User-Id", userId);
		}
		if(password != null) {
			connection.setRequestProperty("User-Pass", password);
		}
		connection.setRequestProperty("Stream-Size", 
				Integer.toString(fileSize));
		if(cookieList.length() > 0) {
			connection.setRequestProperty("Cookie", cookieList.toString());
		}
		return connection;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	protected void write(char c) throws Exception {
		baos.write(c);
	}

	protected void write(String s) throws Exception {
		baos.write(s.getBytes());
	}

	protected void newline() throws Exception {
		write("\r\n");
	}

	protected void writeln(String s) throws Exception {
		write(s);
		newline();
	}

	protected static String randomString() {
		return Long.toString(random.nextLong(), 36);
	}

	private void boundary() throws Exception {
		write("--");
		write(boundary);
	}

	/**
	 * Creates a new multipart POST HTTP request for a specified URL
	 * 
	 * @param url
	 *            the URL to send request to
	 * @throws Exception
	 */
	public MultipartFormPost(URL url) {
		this.url = url;
	}

	/**
	 * Creates a new multipart POST HTTP request for a specified URL string
	 * 
	 * @param urlString
	 *            the string representation of the URL to send request to
	 * @throws Exception
	 */
	public MultipartFormPost(String urlString) throws MalformedURLException {
		this(new URL(urlString));
	}

	
	/**
	 * Set this if you want to be notified of transfer progress.
	 * 
	 * @param progressListener progress callback
	 */
	public void setProgressListener(ExportProgressMonitor progressListener) {
		this.progressListener = progressListener;
	}

	/**
	 * Set this if you want to be able to abort the transfer.
	 * 
	 * @param killer
	 */
	public void setKiller(ExportAbortInformer killer) {
		this.killer = killer;
	}

	/**
	 * adds a cookie to the requst
	 * 
	 * @param name
	 *            cookie name
	 * @param value
	 *            cookie value
	 * @throws Exception
	 */
	public void setCookie(String name, String value) throws Exception {

		cookies.put(name, value);
	}

	/**
	 * adds cookies to the request
	 * 
	 * @param cookies
	 *            the cookie "name-to-value" map
	 * @throws Exception
	 */
	public void setCookies(Map<String, String> cookies) throws Exception {

		if (cookies == null) return;
		this.cookies.putAll(cookies);
	}

	/**
	 * adds cookies to the request
	 * 
	 * @param cookies
	 *            array of cookie names and values (cookies[2*i] is a name,
	 *            cookies[2*i + 1] is a value)
	 * @throws Exception
	 */
	public void setCookies(String[] cookies) throws Exception {

		if (cookies == null)
			return;
		for (int i = 0; i < cookies.length - 1; i += 2) {
			setCookie(cookies[i], cookies[i + 1]);
		}
	}

	private void writeName(String name) throws Exception {

		newline();
		write("Content-Disposition: form-data; name=\"");
		write(name);
		write('"');
	}

	/**
	 * adds a string parameter to the request
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @throws Exception
	 */
	public void setParameter(String name, String value) throws Exception {
		boundary();
		writeName(name);
		newline();
		newline();
		writeln(value);
	}

	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * Defines size of the buffer chunk (in bytes) used to conduct 
	 * transfer with. Defaults to 1024.
	 * 
	 * @param bufferSize
	 */
	public void setBufferSize(int bufferSize) {
		if(bufferSize < 1) {
			throw new IllegalArgumentException("Invalid bufferSize!");
		}
		this.bufferSize = bufferSize;
	}

	/**
	 * Writes data in chunks of {@link #bufferSize} bits from source parameter 
	 * to destination parameter. The boolean argument indicates if pipe is 
	 * used for local processing or external processing.
	 * 
	 * @param in data to be written out
	 * @param out destination to receive the source data
	 * @param inner locality of the pipe; if true, pipe is used for inner write 
	 * 	(within the host system), if false the pipe is used to write out to the 
	 * 	destination (web, outer space, andromeda galaxy, etc).
	 * @throws Exception
	 */
	private void pipe(InputStream in, OutputStream out, boolean inner) throws Exception {

		byte[] buf = new byte[bufferSize];
		int nread;
		int total = 0;

		synchronized(in) {
			while((nread = in.read(buf, 0, buf.length)) > 0) {
				if(killer != null && killer.isExportAborted()) {
					if(!inner) {
						log.debug("aborting! processed {} bytes", total);
						throw new AbortException(total);
					}
				}
				out.write(buf, 0, nread);
				total += nread;
				out.flush();
				if(!inner) {
					log.trace("sent: {}, total: {} - bytes", nread, total);
					if(progressListener != null) {
						progressListener.bytesTransferred(total);
					}
					//Thread.sleep(500); // slow down xfer for testing
				}
			}
		}
		out.flush();
		if(!inner) log.debug("total bytes sent: {}", total);
		buf = null;
	}

	/**
	 * adds a file parameter to the request
	 * 
	 * @param name
	 *            parameter name
	 * @param filename
	 *            the name of the file
	 * @param is
	 *            input stream to read the contents of the file from
	 * @throws Exception
	 */
	public void setParameter(
			String name, 
			String filename, 
			InputStream is) throws Exception {

		boundary();
		writeName(name);
		write("; filename=\"");
		write(filename);
		write('"');
		newline();
		write("Content-Type: ");
		// String type = "application/octet-stream";
		String type = URLConnection.guessContentTypeFromName(filename);
		if (type == null)
			type = "application/octet-stream";
		writeln(type);
		newline();
		pipe(is, baos, true);
		newline();
		is.close();
	}
	
	/**
	 * @param name
	 * @param filename
	 * @param bytes content of the file
	 * @throws Exception
	 */
	public void setParameter(
			String name, 
			String filename, 
			byte[] bytes) throws Exception {
		
		if(bytes == null)
			throw new IllegalArgumentException("bytes can't be null");
		
		fileSize = bytes.length;
		setParameter(name, filename, new ByteArrayInputStream(bytes));
	}

	/**
	 * adds a file parameter to the request
	 * 
	 * @param name
	 *            parameter name
	 * @param file
	 *            the file to upload
	 * @throws Exception
	 */
	public void setParameter(String name, File file) throws Exception {

		setParameter(name, file.getPath(), new FileInputStream(file));
	}

	/**
	 * adds a parameter to the request; if the parameter is a File, the file is
	 * uploaded, otherwise the string value of the parameter is passed in the
	 * request
	 * 
	 * @param name
	 *            parameter name
	 * @param object
	 *            parameter value, a File or anything else that can be
	 *            stringified
	 * @throws Exception
	 */
	public void setParameter(String name, Object object) throws Exception {

		if (object instanceof File) {
			setParameter(name, (File) object);
		} else {
			setParameter(name, object.toString());
		}
	}

	/**
	 * adds parameters to the request
	 * 
	 * @param parameters
	 *            "name-to-value" map of parameters; if a value is a file, the
	 *            file is uploaded, otherwise it is stringified and sent in the
	 *            request
	 * @throws Exception
	 */
	public void setParameters(Map<String, Object> parameters) throws Exception {

		if (parameters == null) return;
		for (Iterator<Map.Entry<String,Object>> i = parameters.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String,Object> entry = i.next();
			setParameter(entry.getKey().toString(), entry.getValue());
		}
	}

	/**
	 * adds parameters to the request
	 * 
	 * @param parameters
	 *            array of parameter names and values (parameters[2*i] is a
	 *            name, parameters[2*i + 1] is a value); if a value is a file,
	 *            the file is uploaded, otherwise it is stringified and sent in
	 *            the request
	 * @throws Exception
	 */
	public void setParameters(Object[] parameters) throws Exception {

		if (parameters == null) return;
		for (int i = 0; i < parameters.length - 1; i += 2) {
			setParameter(parameters[i].toString(), parameters[i + 1]);
		}
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters
	 * that were added
	 * 
	 * @return input stream with the server response
	 * @throws Exception
	 */
	public InputStream post() throws Exception {

		boundary();
		writeln("--");
		int length = baos.size();
		URLConnection connection = getConnection(length);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		baos.close();
		OutputStream os = connection.getOutputStream();
		pipe(bais, os, false); // should go straight to the net, thanks to setFixedLengthStreamingMode()
		// additional help at:
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=50206745
		// if (_sp!=null) {
		// _sp.statusMessage("Audio Transferred. Awaiting server response...");
		// }
		
		os.close();
		InputStream is = null;
		try {
			is = connection.getInputStream();
		}
		catch(Exception e) {
			log.error("can't obtain InputStream! is your target URL correct?");
			throw e;
		}
		
		return is;
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters
	 * that were added before (if any), and with parameters that are passed in
	 * the argument
	 * 
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setParameters
	 */
	public InputStream post(Map<String, Object> parameters) throws Exception {

		setParameters(parameters);
		return post();
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters
	 * that were added before (if any), and with parameters that are passed in
	 * the argument
	 * 
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setParameters
	 */
	public InputStream post(Object[] parameters) throws Exception {

		setParameters(parameters);
		return post();
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters
	 * that were added before (if any), and with cookies and parameters that are
	 * passed in the arguments
	 * 
	 * @param cookies
	 *            request cookies
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setParameters
	 * @see setCookies
	 */
	public InputStream post(
			Map<String, String> cookies, 
			Map<String, Object> parameters) throws Exception {

		setCookies(cookies);
		setParameters(parameters);
		return post();
	}

	/**
	 * posts the requests to the server, with all the cookies and parameters
	 * that were added before (if any), and with cookies and parameters that are
	 * passed in the arguments
	 * 
	 * @param cookies
	 *            request cookies
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setParameters
	 * @see setCookies
	 */
	public InputStream post(
			String[] cookies, 
			Object[] parameters) throws Exception {

		setCookies(cookies);
		setParameters(parameters);
		return post();
	}

	/**
	 * post the POST request to the server, with the specified parameter
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setParameter
	 */
	public InputStream post(
			String name, 
			Object value) throws Exception {

		setParameter(name, value);
		return post();
	}

	/**
	 * post the POST request to the server, with the specified parameters
	 * 
	 * @param name1
	 *            first parameter name
	 * @param value1
	 *            first parameter value
	 * @param name2
	 *            second parameter name
	 * @param value2
	 *            second parameter value
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setParameter
	 */
	public InputStream post(
			String name1, 
			Object value1, 
			String name2,
			Object value2) throws Exception {

		setParameter(name1, value1);
		return post(name2, value2);
	}

	/**
	 * post the POST request to the server, with the specified parameters
	 * 
	 * @param name1
	 *            first parameter name
	 * @param value1
	 *            first parameter value
	 * @param name2
	 *            second parameter name
	 * @param value2
	 *            second parameter value
	 * @param name3
	 *            third parameter name
	 * @param value3
	 *            third parameter value
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setParameter
	 */
	public InputStream post(
			String name1, 
			Object value1, 
			String name2,
			Object value2, 
			String name3, 
			Object value3) throws Exception {

		setParameter(name1, value1);
		return post(name2, value2, name3, value3);
	}

	/**
	 * post the POST request to the server, with the specified parameters
	 * 
	 * @param name1
	 *            first parameter name
	 * @param value1
	 *            first parameter value
	 * @param name2
	 *            second parameter name
	 * @param value2
	 *            second parameter value
	 * @param name3
	 *            third parameter name
	 * @param value3
	 *            third parameter value
	 * @param name4
	 *            fourth parameter name
	 * @param value4
	 *            fourth parameter value
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setParameter
	 */
	public InputStream post(
			String name1, 
			Object value1, 
			String name2,
			Object value2, 
			String name3, 
			Object value3, 
			String name4,
			Object value4) throws Exception {

		setParameter(name1, value1);
		return post(name2, value2, name3, value3, name4, value4);
	}

	/**
	 * posts a new request to specified URL, with parameters that are passed in
	 * the argument
	 * 
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setParameters
	 */
	public static InputStream post(
			URL url, 
			Map<String, Object> parameters) throws Exception {

		return new MultipartFormPost(url).post(parameters);
	}

	/**
	 * posts a new request to specified URL, with parameters that are passed in
	 * the argument
	 * 
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setParameters
	 */
	public static InputStream post(
			URL url, 
			Object[] parameters) throws Exception {

		return new MultipartFormPost(url).post(parameters);
	}

	/**
	 * posts a new request to specified URL, with cookies and parameters that
	 * are passed in the argument
	 * 
	 * @param cookies
	 *            request cookies
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setCookies
	 * @see setParameters
	 */
	public static InputStream post(
			URL url, 
			Map<String, 
			String> cookies, 
			Map<String, Object> parameters) throws Exception {

		return new MultipartFormPost(url).post(cookies, parameters);
	}

	/**
	 * posts a new request to specified URL, with cookies and parameters that
	 * are passed in the argument
	 * 
	 * @param cookies
	 *            request cookies
	 * @param parameters
	 *            request parameters
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setCookies
	 * @see setParameters
	 */
	public static InputStream post(
			URL url, 
			String[] cookies,
			Object[] parameters) throws Exception {

		return new MultipartFormPost(url).post(cookies, parameters);
	}

	/**
	 * post the POST request specified URL, with the specified parameter
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setParameter
	 */
	public static InputStream post(
			URL url, 
			String name1, 
			Object value1) throws Exception {

		return new MultipartFormPost(url).post(name1, value1);
	}

	/**
	 * post the POST request to specified URL, with the specified parameters
	 * 
	 * @param name1
	 *            first parameter name
	 * @param value1
	 *            first parameter value
	 * @param name2
	 *            second parameter name
	 * @param value2
	 *            second parameter value
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setParameter
	 */
	public static InputStream post(
			URL url, 
			String name1, 
			Object value1,
			String name2, 
			Object value2) throws Exception {

		MultipartFormPost mpfp = new MultipartFormPost(url);
		InputStream result = mpfp.post(name1, value1, name2, value2);
		
		return result;
	}

	/**
	 * post the POST request to specified URL, with the specified parameters
	 * 
	 * @param name1
	 *            first parameter name
	 * @param value1
	 *            first parameter value
	 * @param name2
	 *            second parameter name
	 * @param value2
	 *            second parameter value
	 * @param name3
	 *            third parameter name
	 * @param value3
	 *            third parameter value
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setParameter
	 */
	public static InputStream post(
			URL url, 
			String name1, 
			Object value1,
			String name2, 
			Object value2, 
			String name3, 
			Object value3) throws Exception {

		MultipartFormPost mpfp = new MultipartFormPost(url);
		InputStream result = mpfp.post(
				name1, value1, name2, value2, name3, value3);
		
		return result;
	}

	/**
	 * post the POST request to specified URL, with the specified parameters
	 * 
	 * @param name1
	 *            first parameter name
	 * @param value1
	 *            first parameter value
	 * @param name2
	 *            second parameter name
	 * @param value2
	 *            second parameter value
	 * @param name3
	 *            third parameter name
	 * @param value3
	 *            third parameter value
	 * @param name4
	 *            fourth parameter name
	 * @param value4
	 *            fourth parameter value
	 * @return input stream with the server response
	 * @throws Exception
	 * @see setParameter
	 */
	public static InputStream post(
			URL url, 
			String name1, 
			Object value1,
			String name2, 
			Object value2, 
			String name3, 
			Object value3,
			String name4, 
			Object value4) throws Exception {

		MultipartFormPost mpfp = new MultipartFormPost(url);
		InputStream result = mpfp.post(
				name1, value1, name2, value2, name3, value3, name4, value4);
		
		return result;
	}
}
