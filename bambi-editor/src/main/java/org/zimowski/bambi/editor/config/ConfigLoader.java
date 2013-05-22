package org.zimowski.bambi.editor.config;

import java.applet.AppletContext;
import java.io.File;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.plugins.ClearTextProxy;
import org.zimowski.bambi.editor.plugins.MultipartFormPostImageUploader;

/**
 * Responsible for reading and parsing configuration file, then making these   
 * values accessible via {@link Configuration}.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class ConfigLoader implements ConfigParameters {
	
	private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);

	/**
	 * Separator character used to connect multiple words that make up a single 
	 * property key.
	 */
	public static final String PARAM_SEPARATOR = ".";

	/**
	 * Configuration file from which settings are read.
	 */
	private Properties props;
	
	/**
	 * Runtime configuration container used by the app.
	 */
	private ConfigurationImpl settings;
	
	
	public ConfigLoader(Properties config) {
		this.props = config;
		settings = new ConfigurationImpl();
	}
	
	/**
	 * @param context
	 * @deprecated with no replacement; applet deployment is no longer 
	 * 	recommended. Use Java Web Start instead
	 */
	public void setAppletContext(AppletContext context) {
		settings.appletContext = context;
	}

	/**
	 * Saves loaded configuration with {@link ConfigManager}. After 
	 * this call, {@link Configuration} can be retrieved from the manager 
	 * for the remainder of application's lifecycle from anywhere in the class 
	 * hierarchy.
	 */
	public void registerConfiguration() {
		ConfigManager.getInstance().setConfiguration(settings);
	}

	/**
	 * Initializes application parameters while performing full validation. If 
	 * false is returned, it is highly recommended the app aborts as it is 
	 * very likely it would be disfunctional.
	 * 
	 * @return true if initialization succeeded; false on failure
	 */
	public boolean initializeParameters() {
		
		settings.lookAndFeel = props.getProperty(LOOK_AND_FEEL);
		
		String numberOfPics = props.getProperty(NUMBER_OF_PIC_OUTPUTS);
		log.info("setting {} to {}", NUMBER_OF_PIC_OUTPUTS, numberOfPics);
		if(StringUtils.isEmpty(numberOfPics)) {
			log.error("{} is required; it must be 1,2,3 or 4.", NUMBER_OF_PIC_OUTPUTS);
			return false;
		}
		try {
			settings.numberOfPics = Integer.valueOf(numberOfPics);
			if(settings.numberOfPics < 1 || settings.numberOfPics > 4) {
				log.error("{} must be a number between 1 and 4 (inclusive)", NUMBER_OF_PIC_OUTPUTS);
				return false;
			}
		}
		catch(NumberFormatException nfe) {
			log.error("{} must be a number between 1 and 4 (inclusive)", NUMBER_OF_PIC_OUTPUTS);
			return false;
		}

		String isSelectorVisibleParam = props.getProperty(PIC_SELECTOR_VISIBLE);
		settings.selectorVisible = !"false".equalsIgnoreCase(isSelectorVisibleParam);
		log.info(
			String.format("%s defined as [%s]; setting to [%s]", 
					PIC_SELECTOR_VISIBLE, isSelectorVisibleParam, settings.selectorVisible)
		);	

		String isRulerVisibleParam = props.getProperty(RULER_VISIBLE);
		settings.rulerVisible = !"false".equalsIgnoreCase(isRulerVisibleParam);
		log.info(
			String.format("%s defined as [%s]; setting to [%s]", 
				RULER_VISIBLE, isRulerVisibleParam, settings.rulerVisible)
		);	

		String isRulerCheckboxVisibleParam = props.getProperty(RULER_TOGGLE_ONOFF_VISIBLE);
		settings.rulerToggleVisible = !"false".equalsIgnoreCase(isRulerCheckboxVisibleParam);
		log.info(
			String.format("%s defined as [%s]; setting to [%s]", 
				RULER_TOGGLE_ONOFF_VISIBLE, isRulerCheckboxVisibleParam, settings.rulerToggleVisible)
		);	

		
		// if width or height is forced, both are forced
		boolean widthForced = false;
		boolean heightForced = false;
		
		String windowWidthParam = props.getProperty(WINDOW_WIDTH);
		int windowWidth = ConfigurationImpl.DEFAULT_WINDOW_WIDTH;
		if(StringUtils.isEmpty(windowWidthParam)) {
			widthForced = true;
		}
		else {
			try {
				log.info("setting {} to {}", WINDOW_WIDTH, windowWidthParam);
				int width = Integer.valueOf(windowWidthParam);
				if(width < ConfigurationImpl.DEFAULT_WINDOW_WIDTH) {
					log.warn("{} too small!", WINDOW_WIDTH);
					widthForced = true;
				}
				else {
					windowWidth = width;
				}
			}
			catch(NumberFormatException nfe) {
				log.warn("invalid {}!", WINDOW_WIDTH);
				widthForced = true;
			}
		}
		settings.windowWidth = windowWidth;

		String windowHeightParam = props.getProperty(WINDOW_HEIGHT);
		int windowHeight = ConfigurationImpl.DEFAULT_WINDOW_HEIGHT;
		if(StringUtils.isEmpty(windowHeightParam)) {
			heightForced = true;
		}
		else {
			try {
				log.info("setting {} to {}", WINDOW_HEIGHT, windowHeightParam);
				int height = Integer.valueOf(windowHeightParam);
				if(height < ConfigurationImpl.DEFAULT_WINDOW_HEIGHT) {
					log.warn("{} too small!", WINDOW_HEIGHT);
					heightForced = true;
				}
				else {
					windowHeight = height;
				}
			}
			catch(NumberFormatException nfe) {
				log.warn("invalid {}!", WINDOW_HEIGHT);
				heightForced = true;
			}
		}
		settings.windowHeight = windowHeight;
		
		if(widthForced || heightForced) {
			log.info("forcing {} to {}", 
					WINDOW_WIDTH, ConfigurationImpl.DEFAULT_WINDOW_WIDTH);
			log.info("forcing {} to {}", 
					WINDOW_HEIGHT, ConfigurationImpl.DEFAULT_WINDOW_HEIGHT);
		}
		
		boolean picParamsOk = true;
		picParamsOk &= initPicParameters(1);
		picParamsOk &= initPicParameters(2);
		picParamsOk &= initPicParameters(3);
		picParamsOk &= initPicParameters(4);
		if(!picParamsOk) return false;

		String helpPageUrl = props.getProperty(HELP_PAGE_URL);
		log.info("setting {} to {}", HELP_PAGE_URL, helpPageUrl);
		settings.helpPageUrl = helpPageUrl;
		
		String host = props.getProperty(HOST);
		if(StringUtils.isEmpty(HOST)) {
			log.error("{} cannot be blank", HOST);
			return false;			
		}
		log.info("setting {} to {}", HOST, host);
		settings.host = host;

		String businessNameLong = props.getProperty(BUSINESS_NAME_LONG);
		log.info("setting {} to {}", BUSINESS_NAME_LONG, businessNameLong);
		settings.businessNameLong = businessNameLong;

		String businessNameShort = props.getProperty(BUSINESS_NAME_SHORT);
		log.info("setting {} to {}", BUSINESS_NAME_SHORT, businessNameShort);
		settings.businessNameShort = businessNameShort;
		
		boolean authenticationRequired = true;
		String authenticationRequiredStr = props.getProperty(AUTH_REQUIRED);
		if(StringUtils.isNotEmpty(authenticationRequiredStr)) {
			authenticationRequired = Boolean.valueOf(authenticationRequiredStr);
		}
		settings.authenticationRequired = authenticationRequired;
		log.info("setting {} to {}", AUTH_REQUIRED, authenticationRequired);
		
		String imageExporterClass = props.getProperty(IMAGE_EXPORT_PLUGIN);
		try { 
			Class<?> clazz = Class.forName(imageExporterClass);
			clazz.newInstance(); // test
		}
		catch(Exception e) {
			log.warn(String.format("invalid {} plugin {}; error: %s", e.getMessage()), 
					IMAGE_EXPORT_PLUGIN, imageExporterClass);
			imageExporterClass = MultipartFormPostImageUploader.class.getCanonicalName();
		}
		log.info("using {} for {}", imageExporterClass, IMAGE_EXPORT_PLUGIN);
		settings.imageUploaderClass = imageExporterClass;
				
		String loginIdEncryptClass = props.getProperty(AUTH_LOGINID_PLUGIN);
		try { 
			Class<?> clazz = Class.forName(loginIdEncryptClass);
			clazz.newInstance();
		}
		catch(Exception e) {
			log.warn("invalid {} plugin {}; using default", 
					AUTH_LOGINID_PLUGIN, loginIdEncryptClass);
			loginIdEncryptClass = ClearTextProxy.class.getCanonicalName();
		}
		settings.loginIdEncrypterClass = loginIdEncryptClass;

		String passwordEncryptClass = props.getProperty(AUTH_PASS_PLUGIN);
		try { 
			Class<?> clazz = Class.forName(passwordEncryptClass);
			clazz.newInstance();
		}
		catch(Exception e) {
			log.warn("invalid {} plugin {}; using default", 
					AUTH_PASS_PLUGIN, passwordEncryptClass);
			passwordEncryptClass = ClearTextProxy.class.getCanonicalName();
		}
		settings.passwordEncrypterClass = passwordEncryptClass;
		
		String authenticationPrompt = props.getProperty(AUTH_PROMPT);
		if(StringUtils.isNotEmpty(authenticationPrompt)) {
			log.info("setting {} to {}", AUTH_PROMPT, authenticationPrompt);
			settings.authenticationPrompt = authenticationPrompt;
		}
		
		String windowTitle = props.getProperty(WINDOW_TITLE);
		if(StringUtils.isNotEmpty(windowTitle)) {
			log.info("setting {} to {}", WINDOW_TITLE, windowTitle);
			settings.windowTitle = windowTitle;
		}
		
		String radioOutpuTypeLabel = props.getProperty(RADIOGROUP_OUTPUTTYPE_LABEL);
		if(StringUtils.isNotEmpty(radioOutpuTypeLabel)) {
			log.info("setting {} to {}", RADIOGROUP_OUTPUTTYPE_LABEL, radioOutpuTypeLabel);
			settings.radioOutputTypeLabel = radioOutpuTypeLabel;			
		}
		
		try {
			String autoloadFilePath = props.getProperty(AUTOLOAD_IMAGE_FILEPATH);
			File inputFile = new File(autoloadFilePath);
			if(!inputFile.canRead())
				log.warn("{} [{}] - can't read!", AUTOLOAD_IMAGE_FILEPATH, autoloadFilePath);
			else if(!inputFile.isFile())
				log.warn("{} [()] - not a file", AUTOLOAD_IMAGE_FILEPATH, autoloadFilePath);
			else {
				log.info("setting {} to {}", AUTOLOAD_IMAGE_FILEPATH, inputFile.getAbsolutePath());
				settings.autoloadImageFile = inputFile;
			}
		}
		catch(NullPointerException npe) {
			log.debug("{} disabled", AUTOLOAD_IMAGE_FILEPATH);
		}

		loadPluginConfig();
		
		return true;
	}
	
	private void loadPluginConfig() {
		
		// extract custom (unknown at compile time) configuration  
		final Set<String> keys = props.stringPropertyNames();
		
		for(String key : keys) {
			if(key.contains(PARAM_SEPARATOR)) {
				String adjustedKey = key.substring(key.indexOf(PARAM_SEPARATOR)+1);
				if(key.startsWith(IMAGE_EXPORT_PLUGIN)) {
					log.debug("plugin config :: {} -----> {}", key, adjustedKey);
					String val = props.getProperty(key);
					settings.imageUploaderConfig.put(adjustedKey, val);
				}
				if(key.startsWith(AUTH_LOGINID_PLUGIN)) {
					log.debug("plugin config :: {} -----> {}", key, adjustedKey);
					String val = props.getProperty(key);
					settings.loginIdEncrypterConfig.put(adjustedKey, val);
				}
				if(key.startsWith(AUTH_PASS_PLUGIN)) {
					log.debug("plugin config :: {} -----> {}", key, adjustedKey);
					String val = props.getProperty(key);
					settings.passwordEncrypterConfig.put(adjustedKey, val);
				}
			}
		}
	}

	/**
	 * Validates, then initializes all picture parameters.
	 * 
	 * @param picNo valid index (assumed) for picture settings array
	 * @return true if parameters were initialized successfully; false if 
	 * 	initialization failed due to validation problems
	 */
	private boolean initPicParameters(int picNo) {
		
		if(settings.numberOfPics < picNo) return true;
		
		boolean isValid = true;
		settings.picSettings[picNo - 1] = new ImageOutputSettings();
		
		isValid &= initPicTargetShapeParam(picNo);
		isValid &= initPicRatioPreservedParam(picNo);
		isValid &= initPicTargetWidthParam(picNo);
		isValid &= initPicTargetHeightParam(picNo);
		isValid &= initPicSelectorFactorParam(picNo);
		isValid &= initPicRadioLabelParam(picNo);
		isValid &= initPicOutputFormatParam(picNo);

		return isValid;
	}

	/**
	 * @param picNo picture number, starting with 1
	 * @return true if validation passed and successfully initialized; false on error
	 */
	private boolean initPicTargetShapeParam(int picNo) {
		
		String targetShapeParam = PIC_PREFIX + picNo + PIC_TARGET_SHAPE;
		try {
			ImageOutputSettings ps = settings.picSettings[picNo - 1];
			String targetShape = props.getProperty(targetShapeParam);
			log.info("setting {} to {}", targetShapeParam, targetShape);
			ps.targetShape = Integer.valueOf(targetShape);
			if(ps.targetShape != Configuration.TARGET_SHAPE_RECT && 
				ps.targetShape != Configuration.TARGET_SHAPE_ELIPSE && 
				ps.targetShape != Configuration.TARGET_SHAPE_FULL) {
				log.error(String.format("{} must be either %d (square/rectangle), %d (circle/elipse) or %d (full)", Configuration.TARGET_SHAPE_RECT, Configuration.TARGET_SHAPE_ELIPSE, Configuration.TARGET_SHAPE_FULL), targetShapeParam);
				return false;
			}
		}
		catch(NumberFormatException nfe) {
			log.error("expecting a positive number for {}", targetShapeParam);
			return false;
		}
		
		return true;
	}
	
	/**
	 * @param picNo picture number, starting with 1
	 * @return true if validation passed and successfully initialized; false on error
	 */
	private boolean initPicTargetWidthParam(int picNo) {

		String targetWidthParam = PIC_PREFIX + picNo + PIC_TARGET_WIDTH;
		ImageOutputSettings ps = settings.picSettings[picNo - 1];
		boolean ignore = ps.targetShape == Configuration.TARGET_SHAPE_FULL;
		if(ignore) {
			ps.targetWidth = 0;
			return true;
		}
		if(!ps.ratioPreserved) {
			return true;
		}
		try {
			String targetWidth = props.getProperty(targetWidthParam);
			log.info("setting {} to {}", targetWidthParam, targetWidth);
			if(ps.targetWidth < 0) {
				log.error("{} cannot be negative", targetWidthParam);
				return false;
			}
			else
				ps.targetWidth = Integer.valueOf(targetWidth);
		}
		catch(NumberFormatException nfe) {
			log.error("{} must be a non-negative number", targetWidthParam);
			return false;
		}

		return true;
	}
	
	/**
	 * @param picNo picture number, starting with 1
	 * @return true if validation passed and successfully initialized; false on error
	 */
	private boolean initPicTargetHeightParam(int picNo) {

		String targetHeightParam = PIC_PREFIX + picNo + PIC_TARGET_HEIGHT;
		ImageOutputSettings ps = settings.picSettings[picNo - 1];
		boolean ignore = ps.targetShape == Configuration.TARGET_SHAPE_FULL;
		if(ignore) {
			ps.targetHeight = 0;
			return true;
		}
		if(!ps.ratioPreserved) {
			return true;
		}
		try {
			String targetHeight = props.getProperty(targetHeightParam);
			log.info("setting {} to {}", PIC_TARGET_HEIGHT, targetHeight);
			if(ps.targetHeight < 0) {
				log.error("{} cannot be negative", targetHeightParam);
				return false;				
			}
			else
				ps.targetHeight = Integer.valueOf(targetHeight);
		}
		catch(NumberFormatException nfe) {
			log.error("{} must be a non-negative number", targetHeightParam);
			return false;
		}

		return true;
	}
	
	/**
	 * @param picNo picture number, starting with 1
	 * @return true if validation passed and successfully initialized; false on error
	 */
	private boolean initPicRatioPreservedParam(int picNo) {

		String preserveRatioParam = PIC_PREFIX + picNo + PIC_RATIO_PRESERVED;
		ImageOutputSettings ps = settings.picSettings[picNo - 1];
		boolean ignore = ps.targetShape == Configuration.TARGET_SHAPE_FULL;
		if(ignore) return true;

		String preserveRatio = props.getProperty(preserveRatioParam);
		if(StringUtils.isNotBlank(preserveRatio)) {
			if("false".equalsIgnoreCase(preserveRatio)) {
				log.info("setting {} to {}", PIC_RATIO_PRESERVED, false);
				ps.ratioPreserved = false;
			}
			else if("true".equalsIgnoreCase(preserveRatio)) {
				log.info("setting {} to {}", PIC_RATIO_PRESERVED, true);
				ps.ratioPreserved = true;
			}
			else {
				log.warn("{} is invalid value for {}; forcing default", preserveRatio, PIC_RATIO_PRESERVED);
			}		
		}

		return true;
	}
	
	/**
	 * @param picNo picture number, starting with 1
	 * @return true if validation passed and successfully initialized; false on error
	 */
	private boolean initPicSelectorFactorParam(int picNo) {
		
		String selectorFactorParam = PIC_PREFIX + picNo + PIC_SELECTOR_FACTOR;
		ImageOutputSettings ps = settings.picSettings[picNo - 1];
		try {
			String selectorFactor = props.getProperty(selectorFactorParam);
			if(StringUtils.isEmpty(selectorFactor)) {
				return true;
			}
			log.info("setting {} to {}", selectorFactorParam, selectorFactor);
			if(ps.selectorFactor <= 0F) {
				log.error("{} is an invalid selector factor; forcing default", selectorFactorParam);
			}
			else
				ps.selectorFactor = Float.valueOf(selectorFactor);
		}
		catch(NumberFormatException nfe) {
			log.error("{} must be a postivie floating point number; forcing default", selectorFactorParam);
		}

		return true;
	}
	
	/**
	 * @param picNo picture number, starting with 1
	 * @return true if validation passed and successfully initialized; false on error
	 */
	private boolean initPicRadioLabelParam(int picNo) {
		
		String radioLabelParam = PIC_PREFIX + picNo + PIC_RADIO_LABEL;
		String radioLabel = props.getProperty(radioLabelParam);
		if(StringUtils.isEmpty(radioLabel) && settings.getNumberOfPics() > 1) {
			log.error("{} cannot be blank", radioLabelParam);
			return false;						
		}
		log.info("setting {} to {}", radioLabelParam, radioLabel);
		ImageOutputSettings ps = settings.picSettings[picNo - 1];
		ps.radioLabel = radioLabel;
		
		return true;
	}
	
	/**
	 * @param picNo picture number, starting with 1
	 * @return true if validation passed and successfully initialized; false on error
	 */
	private boolean initPicOutputFormatParam(int picNo) {
		
		String outputFormatParam = PIC_PREFIX + picNo + PIC_OUTPUT_FORMAT;
		String outputFormat = props.getProperty(outputFormatParam);
		log.info("setting {} to {}", outputFormatParam, outputFormat);
		if(StringUtils.isBlank(outputFormat)) {
			log.error(String.format("[{%d}] {} is required; valid values are: {}", picNo), outputFormatParam, ImageOutputFormat.getValidValues());
			return false;
		}
		try {
			ImageOutputSettings ps = settings.picSettings[picNo - 1];
			ps.format = ImageOutputFormat.valueOf(outputFormat);
		}
		catch(IllegalArgumentException iae) {
			log.error("invalid {}! valid values are: {}", outputFormatParam, ImageOutputFormat.getValidValues());
			return false;
		}

		return true;
	}
}