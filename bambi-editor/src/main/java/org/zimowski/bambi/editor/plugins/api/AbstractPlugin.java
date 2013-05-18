package org.zimowski.bambi.editor.plugins.api;

import java.util.Properties;

import javax.swing.SwingWorker;

/**
 * Raw plugin base with configuration support. All plugins must be instances 
 * of {@link SwingWorker} due to their unpredictable nature, so the support for 
 * executing off the UI thread is built-in. That does not mean that plugin 
 * is forced to utilize worker thread if it's operation is fast enough as to 
 * not block the UI, though in general it is recommended a plugin does utilize  
 * worker facilities. To run off the EDT a plugin must define its logic inside 
 * {@link #doInBackground()} (by overriding empty implementation), plus 
 * optionally {@link #done()}, and call {@link #execute()} from main plugin 
 * interface entry point. If a plugin is not utilizing non-blocking UI thread, 
 * then it can simply implement its logic directly in its interface entry point.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public abstract class AbstractPlugin 
	extends SwingWorker<Void, Void> implements Plugin {

	protected Properties configuration;
	
	@Override
	public void initialize(Properties configuration) {
		
		this.configuration = configuration;
	}

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