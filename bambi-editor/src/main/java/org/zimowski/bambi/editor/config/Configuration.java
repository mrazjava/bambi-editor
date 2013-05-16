package org.zimowski.bambi.editor.config;

import java.applet.AppletContext;
import java.io.File;

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


	public int getNumberOfPics();
	
	public boolean isSelectorVisible();
	
	/**
	 * Determines if ruler is enabled for this instance. Method name may be 
	 * misleading indicating initial visibility, however, it set to false the 
	 * ruler will be hidden for the entier application's lifecycle.
	 * 
	 * @return true if ruler should be visible; false if hidden
	 */
	public boolean isRulerVisible();
	
	public boolean isRulerToggleVisible();
	
	public int getWindowWidth();
	
	public int getWindowHeight();
	
	public ImageOutputSettings getPicSettings(int picNo) throws ArrayIndexOutOfBoundsException;
	
	public String getHelpPageUrl();
	
	/**
	 * URL of a remote host where image should be uploaded to.
	 * 
	 * @return
	 */
	public String getRemoteHost();
	
	/**
	 * Name of the business running this app. Obviously optional, but if 
	 * provided it may appear on the GUI in places such as about box.
	 * 
	 * @return
	 */
	public String getBusinessNameLong();
	
	/**
	 * One word short name of the business. This could be short top level 
	 * domain as well. Used in certain screens such as login prompt to better 
	 * integrate end user experience with your organization.
	 * 
	 * @return
	 */
	public String getBusinessNameShort();
	
	/**
	 * Returns image file to be loaded on startup, if one is configured and 
	 * read successfully. Null at all other times. Optional configuration, 
	 * typically used for testing and debugging as user is expected to 
	 * manually select (open) their own file.
	 * 
	 * @return file to display in editor on startup; null if none
	 */
	public File getAutoloadImageFile();
	
	public String getWindowTitle();
	
	public String getLookAndFeel();
	
	/**
	 * Determines if the app is running as a result of browser deployment. In 
	 * the old days this would have been an applet, but since applet usage 
	 * for this app has been deprecated, it would be a Java Web Start.
	 * 
	 * @return true if app is web enabled (JNLP - java web start), false if 
	 * 	app is running in stand alone mode.
	 */
	public boolean isWebEnabled();
	
	/**
	 * Determines if user must authenticate to the server before uploading a 
	 * photo.
	 * 
	 * @return true if upload athentication is required; false otherwise
	 */
	public boolean isAuthenticationRequired();
	
	/**
	 * @return algorithm to use for securing login id before transmitting to 
	 * 	the server
	 */
	public TextEncrypter getLoginIdEncrypter();
	
	/**
	 * @return algorithm to use for securing the password before transmitting 
	 * 	to the server
	 */
	public TextEncrypter getPasswordEncrypter();
	
	/**
	 * @return custom prompt displayed when authenticating a user
	 */
	public String getAuthenticationPrompt();
	
	/**
	 * @return context of this applet; null if stand alone app
	 * @deprecated No direct replacement. This app has outgrown the applet 
	 * 	technology and should not be packaged as applet. Use Java Web Start 
	 * 	instead.
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
	 */
	public String getRadioOutputTypeLabel();
}