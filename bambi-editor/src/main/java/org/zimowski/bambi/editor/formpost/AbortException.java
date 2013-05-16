package org.zimowski.bambi.editor.formpost;

/**
 * Thrown when {@link MultipartFormPost} transfer has been aborted by end 
 * user.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class AbortException extends Exception {

	private static final long serialVersionUID = 3877992259022794072L;
	
	private long bytesWritten;

	
	public AbortException(long bytesWritten) {
		super();
		this.bytesWritten = bytesWritten;
	}

	public long getBytesWritten() {
		return bytesWritten;
	}
}