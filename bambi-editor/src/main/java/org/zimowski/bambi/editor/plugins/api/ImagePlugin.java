package org.zimowski.bambi.editor.plugins.api;

/**
 * Common operations for all image based plugins.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public interface ImagePlugin extends Plugin {

	/**
	 * Informs the plugin which selector was used during its invocation. This 
	 * setter should be called before invoking the plugin.
	 * 
	 * @param selectorId selector identifier related directly to selector IDs 
	 * 	defined in the configuration file
	 */
	public void setSelectorId(int selectorId);
}
