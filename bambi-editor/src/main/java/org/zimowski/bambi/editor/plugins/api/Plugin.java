package org.zimowski.bambi.editor.plugins.api;

import java.util.Properties;

import org.zimowski.bambi.editor.config.ConfigLoader;
import org.zimowski.bambi.editor.config.ConfigParameters;

/**
 * Common plugin operations.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public interface Plugin {

	/**
	 * Typically called immediately after instantiation to allow the plugin  
	 * proper initialization and certainly before its invocation. Since plugin 
	 * configuration is custom defined (cannot be known at compile time), it 
	 * must follow specific naming convention in order to be properly loaded.  
	 * Plugin setting must begin with the name of the plugin setting itself,  
	 * followed by a separator. For example, the {@link ConfigParameters#IMAGE_UPLOAD_PLUGIN} 
	 * is used to define the image upload strategy by specifying plugin's full 
	 * class path. Configuration settings for that plugin can be named freely 
	 * but must be prefixed with the plugin setting itself followed by 
	 * {@link ConfigLoader#PARAM_SEPARATOR}. Another example: say fooPlug is 
	 * the setting for com.foo.Plug. Configuration setting bar for fooPlug 
	 * would would be defined as fooPlug.bar if dot is {@link ConfigLoader#PARAM_SEPARATOR}.    
	 * 
	 * @param configuration plugin settings defined in the configuration file
	 */
	public void initialize(Properties configuration);
}
