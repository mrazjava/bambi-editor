package org.zimowski.bambi.editor.config;

import org.zimowski.bambi.editor.plugins.ClearTextProxy;
import org.zimowski.bambi.editor.plugins.FtpImageUploader;
import org.zimowski.bambi.editor.plugins.MD5Digest;
import org.zimowski.bambi.editor.plugins.MultipartFormPostImageUploader;
import org.zimowski.bambi.editor.plugins.RsaCipher;
import org.zimowski.bambi.editor.plugins.SHA1Digest;
import org.zimowski.bambi.editor.plugins.SHA256Digest;
import org.zimowski.bambi.editor.studio.WelcomePanel;

/**
 * List of valid parameters bambi can be configured with. Each parameter is 
 * a key in the property (configuration) file.
 * 
 * @author Adam Zimwoski
 */
public interface ConfigParameters {
	
	/**
	 * Fully qualified class name to the look and feel UI class 
	 * this applet should use. Optional. If not provided, default 
	 * Java look and feel is used.
	 */
	public final String LOOK_AND_FEEL = "lookAndFeel";
	
	/**
	 * URL to welcome file that will be displayed upon application startup 
	 * inside the {@link WelcomePanel}. If not provided or invalid, 
	 * application will attempt to display {@link #WELCOME_RESOURCE_PATH} 
	 * instead.
	 */
	public final String WELCOME_URL = "welcomeUrl";
	
	/**
	 * Local resource path to file application should display on startup. This 
	 * setting is ignored if {@link #WELCOME_URL} is provided and correct. If 
	 * this value is not provided or is incorrect, {@link WelcomePanel} is 
	 * displayed empty.
	 */
	public final String WELCOME_RESOURCE_PATH = "welcomeResourcePath";
	
	/**
	 * true if ruler should be shown; false if it should be hidden. Default 
	 * value is true.
	 */
	public final String RULER_VISIBLE = "isRulerVisible";
	
	/**
	 * controls if button which turns ruler visibility on/off is displayed or 
	 * not. this button allows the user to show/hide the ruler. true if button 
	 * should be shown; false if hidden. If shown, it uses {@link #SHOW_RULER} 
	 * as default value. If not defined, button is shown.
	 */
	public final String RULER_TOGGLE_ONOFF_VISIBLE = "isRulerToggleVisible";
	
	/**
	 * Preferred main widow width. Optional. Default width is 800. Minimum 
	 * value 640.
	 */
	public final String WINDOW_WIDTH = "windowWidth";
	
	/**
	 * Preferred main window height. Optional. Default height is 600. Minimum 
	 * value 480.
	 */
	public final String WINDOW_HEIGHT = "windowHeight";
	
	/**
	 * Number of picture output types radio buttons to display. Allowed values  
	 * are 1, 2, 3 or 4. If 1 is defined, a single selected radio button 
	 * shows up and it is disabled.
	 */
	public final String NUMBER_OF_PIC_OUTPUTS = "numberOfPicOutputOptions";
	
	/**
	 * Fully qualified class path to the plugin that will perform image 
	 * export. The default is {@link MultipartFormPostImageUploader}.
	 * 
	 * @see FtpImageUploader
	 * @see MultipartFormPostImageUploader
	 */
	public final String IMAGE_EXPORT_PLUGIN = "exporter";
	
	/**
	 * true if authentication is required before image upload; false if not 
	 * required. If authentication is required, user will be prompted for 
	 * login id and password. Default is true.
	 */
	public final String AUTH_REQUIRED = "isAuthenticationRequired";
	
	/**
	 * Short sentence that will be displayed in the dialog prompting user to 
	 * enter login credentials. The default is {@link #DEFAULT_AUTH_PROMPT}.
	 */
	public final String AUTH_PROMPT = "authenticationPrompt";
	
	public static final String DEFAULT_AUTH_PROMPT = 
			"Enter login credentials you normally use for this website";
	
	/**
	 * Determines how to secure user's login id before transmission. The value 
	 * is a fully qualified class name of the encryption plugin that 
	 * implements {@link org.zimowski.bambi.editor.plugins.api.TextEncrypter}. 
	 * The default value {@link ClearTextProxy}.
	 * 
	 * @see ClearTextProxy
	 * @see MD5Digest
	 * @see RsaCipher
	 * @see SHA1Digest
	 * @see SHA256Digest
	 * 
	 */
	public final String AUTH_LOGINID_PLUGIN = "authLoginIdSecurity";
	
	/**
	 * Determines how to secure user's password id before transmission. The value 
	 * is a fully qualified class name of the encryption plugin that 
	 * implements {@link org.zimowski.bambi.editor.plugins.api.TextEncrypter}. 
	 * The default value {@link ClearTextProxy}.
	 * 
	 * @see ClearTextProxy
	 * @see MD5Digest
	 * @see RsaCipher
	 * @see SHA1Digest
	 * @see SHA256Digest
	 * 
	 */
	public final String AUTH_PASS_PLUGIN = "authPasswordSecurity";
	
	/**
	 * Picture parameter prefix. To be followed by a number indicating 
	 * position within the array. Not zero based, first picture denoted 
	 * by 1. Not a valid parameter by itself, but to be used with all 
	 * PIC_* parameters.
	 */
	public final String PIC_PREFIX = "pic";

	/**
	 * Final shape. Valid values are:
	 * <ul>
	 * <li>{@link Configuration#TARGET_SHAPE_RECT} - SQUARE / RECTANGLE</li>
	 * <li>{@link Configuration#TARGET_SHAPE_ELIPSE} - ELIPSE / CIRCLE</li>
	 * <li>{@link Configuration#TARGET_SHAPE_FULL} - FULL PIC SIZE</li>
	 * </ul>
	 * Width and height parameters control squarness and eliptical factors. 
	 * If width and height are the same, we get either square of circle, 
	 * if they're different we get either rectangle of elipse.
	 * 
	 * Requires picture prefix identifier to assemble full parameter name.
	 */
	public final String PIC_TARGET_SHAPE = "Shape";
	
	/**
	 * Determines if resizing selector will preserve target dimension ratio. 
	 * By default (false) ratio is not preserved, but setting this to true will 
	 * ensure that {@link #PIC_TARGET_HEIGHT} and {@link #PIC_TARGET_WIDTH} are 
	 * resized relative one to another. This setting is ignored if 
	 * {@link #PIC_TARGET_SHAPE} is {@link Configuration#TARGET_SHAPE_FULL}.
	 */
	public final String PIC_RATIO_PRESERVED = "PreserveRatio";
	
	/**
	 * Width in pixes of the final generated size, after scaling.
	 * Requires picture prefix identifier to assemble full parameter name. 
	 * This setting is ignored if {@link #PIC_TARGET_SHAPE} is 
	 * {@link Configuration#TARGET_SHAPE_FULL} or if {@link #PIC_RATIO_PRESERVED} 
	 * is false.
	 */
	public final String PIC_TARGET_WIDTH = "TargetWidth";
	
	/**
	 * Height in pixes of the final generated size, after scaling. 
	 * Requires picture prefix identifier to assemble full parameter name. This 
	 * setting is ignored if {@link #PIC_TARGET_SHAPE} is 
	 * {@link Configuration#TARGET_SHAPE_FULL} or if {@link #PIC_RATIO_PRESERVED} 
	 * is false.
	 */
	public final String PIC_TARGET_HEIGHT = "TargetHeight";
	
	/**
	 * true if selector should be displayed on startup; false if it should be 
	 * hidden on startup. true if not defined
	 */
	public final String PIC_SELECTOR_VISIBLE = "isSelectorVisible";
	
	/**
	 * Controlling factor for the size of the selector used to chose sub image. 
	 * Recommended value is 1.0 which means that selector will be drawn in the 
	 * authentic size of the image's target dimensions 
	 * (@see {@link #PIC_TARGET_WIDTH}, {@link #PIC_TARGET_HEIGHT}).  
	 * This value can be reduced to draw a proportionally 
	 * smaller selector, or increased to draw a larger selector. For example, 
	 * a value of 0.5 would draw a selector half the original size. Selector 
	 * cannot be defined in pixel size, as it has to be proportionally 
	 * consistent with targetted output size. This parameter may be deprecated 
	 * in the future because drawing selector at reduced size caused clipped 
	 * image be upscaled to target dimensions. It is also a rather confusing 
	 * parameter without much added benefit from usage perspective. It is 
	 * highly recommended to not use this parameter and let the system draw it 
	 * at a default factor size of 1f.
	 * 
	 * Requires picture prefix identifier to assemble full parameter name.
	 */
	public final String PIC_SELECTOR_FACTOR = "SelectorFactor";
	
	/**
	 * Text to appear next to a radio button representing this picture. 
	 * Requires picture prefix identifier to assemble full parameter name.
	 */
	public final String PIC_RADIO_LABEL = "RadioLabel";
	
	/**
	 * Image format for the final output. Only valid formats are JPEG and PNG. 
	 * Requires picture prefix identifier to assemble full parameter name.
	 * 
	 * @see ImageOutputFormat
	 */
	public final String PIC_OUTPUT_FORMAT = "OutputFormat";
	
	/**
	 * URL that will perform image upload post processing. This often is a 
	 * javascript call to the browser to refresh uploaded image.
	 * 
	 * @deprecated used to be handy from applet context; though app has 
	 * 	outgrown the applet technology and is no longer targetted for applet 
	 * 	deploy. Instead, Java Web Start should be used, which renders 
	 * 	applet context related features obsolete. Instead of calling applet 
	 * 	host page to notify pic refresh is needed, server should handle this 
	 * 	task instead by employing javascript timeout (window.setInterval), or 
	 * 	other server push simulation effect. This parameter is deprecated with 
	 * 	no direct replacement. Setting it has no effect.
	 */
	public final String PIC_POST_PROCESS_URL = "PostProcessUrl";

	public final String HELP_PAGE_URL = "helpPageUrl";
	
	public final String HOST = "host";
	
	/**
	 * Full name of your business organization as it relates to the website  
	 * to which bambi is uploading. This value may appear on various screens  
	 * of the editor to better integrate your organization with bambi and end  
	 * user's experience.
	 */
	public final String BUSINESS_NAME_LONG = "businessNameLong";
	
	/**
	 * Short name of your business organization as it relates to the website 
	 * to which bambi is uploading. Ideally this should be one word or simply 
	 * top level domain of your website (Google or google.com, etc). This 
	 * value may appear on various screens of the editor to better integrate 
	 * your organization with bambi and end user's experience.
	 */
	public final String BUSINESS_NAME_SHORT = "businessNameShort";
	
	/**
	 * Alternative app name if in applet mode. This parameter is handy to 
	 * better integrate bambi as an applet into a hosting website. This 
	 * parameter is ignored in stand alone mode.
	 */
	public final String WINDOW_TITLE = "windowTitle";
	
	/**
	 * Label which describes radio buttons for selecing picture output type.
	 */
	public final String RADIOGROUP_OUTPUTTYPE_LABEL = "picOutputTypeLabel";
	
	/**
	 * Path to image file if autoload is desired. Also handy when developing 
	 * and/or testing. If set, init routine will immediately load the file into 
	 * editor for display.
	 */
	public final String AUTOLOAD_IMAGE_FILEPATH = "autoloadImageFilePath";
}