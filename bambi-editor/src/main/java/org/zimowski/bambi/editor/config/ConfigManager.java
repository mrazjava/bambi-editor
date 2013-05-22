package org.zimowski.bambi.editor.config;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class ConfigManager {

	private static ConfigManager manager = null;
	
	private Configuration configuration;
	
	
	private ConfigManager() {
	}
	
	public static ConfigManager getInstance() {
		if(manager == null) manager = new ConfigManager();
		return manager;
	}
	
	void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	/**
	 * Retrieves configuration for the proper instance of the app, weather 
	 * deployed stand-alone to desktop, or via Java Web Start. Retrieving 
	 * configuration via this getter always guarantees properly bound settings 
	 * to classloader instance (which is particularly important in browser 
	 * deployment).
	 * 
	 * @return configuration settings for this instance of the application
	 */
	public Configuration getConfiguration() {
		return configuration;
	}
}
