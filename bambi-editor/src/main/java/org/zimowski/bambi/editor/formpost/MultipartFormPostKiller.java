package org.zimowski.bambi.editor.formpost;


/**
 * Allows to abort transfer in progress. Implement this interface and set it 
 * on {@link MultipartFormPost}.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public interface MultipartFormPostKiller {

	/**
	 * @return true if transfer in progress has been aborted
	 */
	public boolean isMultipartFormPostAborted();
}
