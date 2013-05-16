package org.zimowski.bambi.editor.config;

import java.applet.AppletContext;
import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.zimowski.bambi.editor.plugins.api.TextEncrypter;

/**
 * Central data structure for all configuration needed to run this application.
 * This includes any applet parameters as well as dynamic settings such as 
 * run-time selected image file.
 * 
 * @author Adam Zimowski
 */
class ConfigurationImpl implements Configuration {

	//private static final Logger log = LoggerFactory.getLogger(Configuration.class);
	
	final static int DEFAULT_WINDOW_WIDTH = 800; // in pixels
	
	final static int DEFAULT_WINDOW_HEIGHT = 600; // in pixels
	
	String lookAndFeel;
	
	AppletContext appletContext = null;
	
	boolean selectorVisible = true; // default
	
	boolean rulerVisible = true; // default
	
	boolean rulerToggleVisible = true; // default
	
	int windowWidth;
	
	int windowHeight;
	
	int numberOfPics;

	
	/**
	 * Always supportings settings for maximum available pictures, used or not. 
	 * However, instances only constructed for number of pics used, remaining 
	 * elements are null.
	 */
	ImageOutputSettings[] picSettings = new ImageOutputSettings[4];

	String helpPageUrl;
	
	String host;
	
	String businessNameLong;
	
	String businessNameShort;
	
	boolean selectorOn;
	
	File autoloadImageFile;
	
	String windowTitle;
	
	String radioOutputTypeLabel;
	
	boolean authenticationRequired;
	
	TextEncrypter loginIdEncrypter;
	
	TextEncrypter passwordEncrypter;
	
	String authenticationPrompt = null;
	
	
	/**
	 * Can only be instantiated at package level
	 */
	ConfigurationImpl() {
	}

	public int getNumberOfPics() {
		return numberOfPics;
	}

	public boolean isSelectorVisible() {
		return selectorVisible;
	}

	public boolean isRulerVisible() {
		return rulerVisible;
	}

	public boolean isRulerToggleVisible() {
		return rulerToggleVisible;
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public ImageOutputSettings getPicSettings(int picNo) {
		return picSettings[picNo - 1];
	}

	public String getHelpPageUrl() {
		return helpPageUrl;
	}

	public String getRemoteHost() {
		return host;
	}

	public String getBusinessNameLong() {
		return businessNameLong;
	}

	@Override
	public String getBusinessNameShort() {
		return businessNameShort;
	}

	public File getAutoloadImageFile() {
		return autoloadImageFile;
	}
	
	public String getWindowTitle() {
		String title = StringUtils.isNotBlank(windowTitle) ? windowTitle : Configuration.APP_NAME;
		return isWebEnabled() ? title : Configuration.APP_NAME;
	}
	
	public String getLookAndFeel() {
		return lookAndFeel;
	}

	public boolean isWebEnabled() {
		return appletContext != null;
	}

	@Override
	public boolean isAuthenticationRequired() {
		return authenticationRequired;
	}

	@Override
	public TextEncrypter getLoginIdEncrypter() {
		return loginIdEncrypter;
	}

	@Override
	public TextEncrypter getPasswordEncrypter() {
		return passwordEncrypter;
	}

	@Override
	public String getAuthenticationPrompt() {
		return authenticationPrompt;
	}

	@Deprecated
	public AppletContext getAppletContext() {
		return appletContext;
	}
	
	public String getRadioOutputTypeLabel() {
		return StringUtils.isEmpty(radioOutputTypeLabel) ?
				"Selector:" : radioOutputTypeLabel;
	}
}