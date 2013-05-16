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
	
	public Configuration getConfiguration() {
		return configuration;
	}
}
