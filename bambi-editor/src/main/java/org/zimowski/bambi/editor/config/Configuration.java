package org.zimowski.bambi.editor.config;

import java.applet.AppletContext;
import java.io.File;
import java.util.Properties;

import org.zimowski.bambi.editor.plugins.api.ImageUploader;
import org.zimowski.bambi.editor.plugins.api.TextEncrypter;

/**
 * Application global settings configurable externally. These are typically 
 * stored in the configuration (properties) file.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public interface Configuration {

	public final static String RESOURCE_PATH = "/org/zimowski/bambi/editor/studio/resources/";
	
	public final static int TARGET_SHAPE_RECT = 1;
	
	public final static int TARGET_SHAPE_ELIPSE = 2;
	
	/**
	 * application name
	 */
	public static final String APP_NAME = "BambiEditor";


	/**
	 * @return
	 * @see ConfigParameters#NUMBER_OF_PIC_OUTPUTS
	 */
	public int getNumberOfPics();
	
	/**
	 * @return
	 * @see ConfigParameters#PIC_SELECTOR_VISIBLE
	 */
	public boolean isSelectorVisible();
	
	/**
	 * Determines if ruler is enabled for this instance. Method name may be 
	 * misleading indicating initial visibility, however, it set to false the 
	 * ruler will be hidden for the entier application's lifecycle.
	 * 
	 * @return true if ruler should be visible; false if hidden
	 * @see ConfigParameters#RULER_VISIBLE
	 */
	public boolean isRulerVisible();
	
	/**
	 * @return
	 * @see ConfigParameters#RULER_TOGGLE_ONOFF_VISIBLE
	 */
	public boolean isRulerToggleVisible();
	
	/**
	 * @return
	 * @see ConfigParameters#WINDOW_WIDTH
	 */
	public int getWindowWidth();
	
	/**
	 * @return
	 * @see ConfigParameters#WINDOW_HEIGHT
	 */
	public int getWindowHeight();
	
	public ImageOutputSettings getPicSettings(int picNo) throws ArrayIndexOutOfBoundsException;
	
	/**
	 * Full URL to the home page of the help system.
	 * 
	 * @return
	 * @see ConfigParameters#HELP_PAGE_URL
	 */
	public String getHelpPageUrl();
	
	/**
	 * URL of a remote host where image should be uploaded to.
	 * 
	 * @return
	 * @see ConfigParameters#HOST
	 */
	public String getRemoteHost();
	
	/**
	 * Name of the business running this app. Obviously optional, but if 
	 * provided it may appear on the GUI in places such as about box.
	 * 
	 * @return
	 * @see ConfigParameters#BUSINESS_NAME_LONG
	 */
	public String getBusinessNameLong();
	
	/**
	 * One word short name of the business. This could be short top level 
	 * domain as well. Used in certain screens such as login prompt to better 
	 * integrate end user experience with your organization.
	 * 
	 * @return
	 * @see ConfigParameters#BUSINESS_NAME_SHORT
	 */
	public String getBusinessNameShort();
	
	/**
	 * Returns image file to be loaded on startup, if one is configured and 
	 * read successfully. Null at all other times. Optional configuration, 
	 * typically used for testing and debugging as user is expected to 
	 * manually select (open) their own file.
	 * 
	 * @return file to display in editor on startup; null if none
	 * @see ConfigParameters#AUTOLOAD_IMAGE_FILEPATH
	 */
	public File getAutoloadImageFile();
	
	/**
	 * @return
	 * @see ConfigParameters#WINDOW_TITLE
	 */
	public String getWindowTitle();
	
	/**
	 * @return
	 * @see ConfigParameters#LOOK_AND_FEEL
	 */
	public String getLookAndFeel();
	
	/**
	 * Returns configured image upload plugin. If not configured, default one 
	 * is used. 
	 * 
	 * @return the plugin to handle image upload process
	 * @see ConfigParameters#IMAGE_UPLOAD_PLUGIN
	 */
	public ImageUploader getImageUploader();
	
	/**
	 * @return plugin specific configuration
	 */
	public Properties getImageUploaderConfig();
	
	/**
	 * Determines if user must authenticate to the server before uploading a 
	 * photo.
	 * 
	 * @return true if upload athentication is required; false otherwise
	 * @see ConfigParameters#AUTH_REQUIRED
	 */
	public boolean isAuthenticationRequired();
	
	/**
	 * @return algorithm to use for securing login id before transmitting to 
	 * 	the server
	 * @see ConfigParameters#AUTH_LOGINID_PLUGIN
	 */
	public TextEncrypter getLoginIdEncrypter();
	
	/**
	 * @return optional configuration for encrypter plugin
	 */
	public Properties getLoginIdEncrypterConfig();
	
	/**
	 * @return algorithm to use for securing the password before transmitting 
	 * 	to the server
	 * @see ConfigParameters#AUTH_PASS_PLUGIN
	 */
	public TextEncrypter getPasswordEncrypter();
	
	/**
	 * @return optional configuration for encrypter plugin
	 */
	public Properties getPasswordEncrypterConfig();
	
	/**
	 * @return custom prompt displayed when authenticating a user
	 * @see ConfigParameters#AUTH_PROMPT
	 */
	public String getAuthenticationPrompt();
	
	/**
	 * @return context of this applet; null if stand alone app
	 * @deprecated No direct replacement. This app has outgrown the applet 
	 * 	technology. Use Java Web Start instead.
	 */
	public AppletContext getAppletContext();
	
	/**
	 * Desired label for selector radio button group. This label describes 
	 * the nature of these buttons. For example, if the radio buttons action 
	 * changes selector to a different shape, this label may be called 
	 * "Selector", however it is up to the implementation to provide the 
	 * default in absence of a setting for this configuration.
	 * 
	 * @return
	 * @see ConfigParameters#RADIOGROUP_OUTPUTTYPE_LABEL
	 */
	public String getRadioOutputTypeLabel();
}