package org.zimowski.bambi.editor.plugins.api;

/**
 * A convenience base for fast plugins that want to run ON the EDT. This base 
 * should be used with CAUTION and ONLY for plugins which are certain to 
 * perform fast enough as to not block the user interface. 
 * 
 * @author Adam Zimowski (mrazjava)
 */
public abstract class AbstractPluginAdapter extends AbstractPlugin {

	/**
	 * Convenience stub so plugins that don't want to run off the EDT don't 
	 * have to be bothered with providing empty implementation. Does nothing.
	 * 
	 * @return null every time
	 */
	@Override
	protected Void doInBackground() throws Exception {
		return null;
	}
}