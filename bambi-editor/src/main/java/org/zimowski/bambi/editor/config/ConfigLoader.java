package org.zimowski.bambi.editor.config;

import java.applet.AppletContext;
import java.io.File;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.plugins.ClearTextProxy;
import org.zimowski.bambi.editor.plugins.api.TextEncrypter;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class ConfigLoader implements ConfigParameters {
	
	private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);

	private Properties props;
	
	private ConfigurationImpl settings;
	
	
	public ConfigLoader(Properties config) {
		this.props = config;
		settings = new ConfigurationImpl();
	}
	
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
		
		String loginIdEncryptClass = props.getProperty(AUTH_LOGINID_SECURITY);
		TextEncrypter loginIdEncrypter = null;
		try { 
			Class<?> clazz = Class.forName(loginIdEncryptClass);
			loginIdEncrypter = (TextEncrypter)clazz.newInstance();
		}
		catch(Exception e) {
			log.warn("invalid  {} plugin {}; using default", 
					AUTH_LOGINID_SECURITY, loginIdEncryptClass);
			loginIdEncrypter = new ClearTextProxy();
		}
		settings.loginIdEncrypter = loginIdEncrypter;

		String passwordEncryptClass = props.getProperty(AUTH_PASS_SECURITY);
		TextEncrypter passwordEncrypter = null;
		try { 
			Class<?> clazz = Class.forName(passwordEncryptClass);
			passwordEncrypter = (TextEncrypter)clazz.newInstance();
		}
		catch(Exception e) {
			log.warn("invalid {} plugin {}; using default", 
					AUTH_PASS_SECURITY, passwordEncryptClass);
			passwordEncrypter = new ClearTextProxy();
		}
		settings.passwordEncrypter = passwordEncrypter;
		
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
		
		return true;
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
		isValid &= initPicTargetWidthParam(picNo);
		isValid &= initPicTargetHeightParam(picNo);
		isValid &= initPicSelectorFactorParam(picNo);
		isValid &= initPicSubmitUrlParam(picNo);
		isValid &= initPicRadioLabelParam(picNo);
		isValid &= initPicOutputFormatParam(picNo);
		isValid &= initPicPostProcessUrl(picNo);

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
			if(ps.targetShape < 1 || ps.targetShape > 2) {
				log.error("{} must be either 1 (square/rectangle) or 2 (circle/elipse)", targetShapeParam);
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
		try {
			ImageOutputSettings ps = settings.picSettings[picNo - 1];
			String targetWidth = props.getProperty(targetWidthParam);
			log.info("setting {} to {}", targetWidthParam, targetWidth);
			ps.targetWidth = Integer.valueOf(targetWidth);
			if(ps.targetWidth <= 0) {
				log.error("{} must be a positive number", targetWidthParam);
				return false;
			}
		}
		catch(NumberFormatException nfe) {
			log.error("{} must be a positive number", targetWidthParam);
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
		try {
			ImageOutputSettings ps = settings.picSettings[picNo - 1];
			String targetHeight = props.getProperty(targetHeightParam);
			log.info("setting {} to {}", PIC_TARGET_HEIGHT, targetHeight);
			ps.targetHeight = Integer.valueOf(targetHeight);
			if(ps.targetHeight <= 0) {
				log.error("{} must be a positive number", targetHeightParam);
				return false;				
			}
		}
		catch(NumberFormatException nfe) {
			log.error("{} must be a positive number", targetHeightParam);
			return false;
		}

		return true;
	}
	
	/**
	 * @param picNo picture number, starting with 1
	 * @return true if validation passed and successfully initialized; false on error
	 */
	private boolean initPicSelectorFactorParam(int picNo) {
		
		String selectorFactorParam = PIC_PREFIX + picNo + PIC_SELECTOR_FACTOR;
		try {
			ImageOutputSettings ps = settings.picSettings[picNo - 1];
			String selectorFactor = props.getProperty(selectorFactorParam);
			log.info("setting {} to {}", selectorFactorParam, selectorFactor);
			if(StringUtils.isEmpty(selectorFactor)) {
				log.error("{} is required", selectorFactorParam);
				return false;
			}
			ps.selectorFactor = Float.valueOf(selectorFactor);
			if(ps.selectorFactor <= 0F) {
				log.error("{} must be a postivie floating point number", selectorFactorParam);
				return false;				
			}
		}
		catch(NumberFormatException nfe) {
			log.error("{} must be a postivie floating point number", selectorFactorParam);
			return false;
		}

		return true;
	}
	
	/**
	 * @param picNo picture number, starting with 1
	 * @return true if validation passed and successfully initialized; false on error
	 */
	private boolean initPicSubmitUrlParam(int picNo) {
		
		final String submitUrlParam = PIC_PREFIX + picNo + PIC_SUBMIT_URL;
		String submitUrl = props.getProperty(submitUrlParam);
		if(StringUtils.isEmpty(submitUrl)) {
			log.error("{} cannot be blank", submitUrlParam);
			return false;						
		}
		log.info("setting {} to {}", submitUrlParam, submitUrl);
		ImageOutputSettings ps = settings.picSettings[picNo - 1];
		ps.submitUrl = submitUrl;

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
	
	private boolean initPicPostProcessUrl(int picNo) {
		
		@SuppressWarnings("deprecation")
		String postProcessUrlParam = PIC_PREFIX + picNo + PIC_POST_PROCESS_URL;
		String postProcessUrl = props.getProperty(postProcessUrlParam);
		if(StringUtils.isNotEmpty(postProcessUrl)) {
			log.info("seting {} to {}", postProcessUrlParam, postProcessUrl);
			log.warn("usage of {} is deprecated and will be ignored", postProcessUrlParam);
		}
		
		return true;
	}
}