package org.zimowski.bambi.editor.config;

import java.applet.AppletContext;
import java.io.File;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.plugins.ClearTextProxy;
import org.zimowski.bambi.editor.plugins.MultipartFormPostUploader;
import org.zimowski.bambi.editor.plugins.api.ImageUploader;
import org.zimowski.bambi.editor.plugins.api.TextEncrypter;

/**
 * Central data structure for all configuration needed to run this application.
 * This includes any applet parameters as well as dynamic settings such as 
 * run-time selected image file.
 * 
 * @author Adam Zimowski
 */
class ConfigurationImpl implements Configuration {

	private static final Logger log = LoggerFactory.getLogger(Configuration.class);
	
	/**
	 * Width, in pixels, applied to the window if not otherwise defined. 
	 */
	final static int DEFAULT_WINDOW_WIDTH = 800;
	
	/**
	 * Height, in pixels, applied to the window if not otherwise defined. 
	 */
	final static int DEFAULT_WINDOW_HEIGHT = 600;
	
	String lookAndFeel;
	
	/**
	 * we have outgrown applet technology and trying to deploy as applet would 
	 * be suicidal (resources, etc); use Java Web Start instead.
	 */
	@Deprecated
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
	
	/**
	 * Canonical name of the plugin
	 */
	String imageUploaderClass;
	
	Properties imageUploaderConfig;
	
	boolean authenticationRequired;
	
	/**
	 * Canonical name of the plugin
	 */
	String loginIdEncrypterClass;
	
	Properties loginIdEncrypterConfig;
	
	/**
	 * Canonical name of the plugin
	 */
	String passwordEncrypterClass;
	
	Properties passwordEncrypterConfig;
	
	String authenticationPrompt = null;
	
	
	/**
	 * Can only be instantiated at package level
	 */
	ConfigurationImpl() {
		imageUploaderConfig = new Properties();
		loginIdEncrypterConfig = new Properties();
		passwordEncrypterConfig = new Properties();
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
		return title;
	}
	
	public String getLookAndFeel() {
		return lookAndFeel;
	}

	/**
	 * {@inheritDoc} New instance is returned on every invocation.
	 */
	@Override
	public ImageUploader getImageUploader() {
		ImageUploader uploaderPlugin;
		try { 
			Class<?> clazz = Class.forName(imageUploaderClass);
			uploaderPlugin = (ImageUploader)clazz.newInstance();
		}
		catch(Exception e) {
			log.error(e.getMessage());
			uploaderPlugin = new MultipartFormPostUploader();
		}
		return uploaderPlugin;
	}

	@Override
	public Properties getImageUploaderConfig() {
		return imageUploaderConfig;
	}

	@Override
	public boolean isAuthenticationRequired() {
		return authenticationRequired;
	}

	@Override
	public TextEncrypter getLoginIdEncrypter() {
		return getEncrypter(loginIdEncrypterClass);
	}

	@Override
	public Properties getLoginIdEncrypterConfig() {
		return loginIdEncrypterConfig;
	}

	@Override
	public TextEncrypter getPasswordEncrypter() {
		return getEncrypter(passwordEncrypterClass);
	}

	@Override
	public Properties getPasswordEncrypterConfig() {
		return passwordEncrypterConfig;
	}
	
	private TextEncrypter getEncrypter(String encrypterClass) {
		TextEncrypter encrypterPlugin;
		try {
			Class<?> clazz = Class.forName(encrypterClass);
			encrypterPlugin = (TextEncrypter)clazz.newInstance();
		}
		catch(Exception e) {
			log.error("failed to load {}: {}", encrypterClass, e.getMessage());
			encrypterPlugin = new ClearTextProxy();
		}		
		return encrypterPlugin;
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