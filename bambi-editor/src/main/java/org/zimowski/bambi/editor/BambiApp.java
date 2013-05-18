package org.zimowski.bambi.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.config.ConfigLoader;
import org.zimowski.bambi.editor.config.ConfigManager;
import org.zimowski.bambi.editor.config.ConfigParameters;
import org.zimowski.bambi.editor.config.Configuration;
import org.zimowski.bambi.editor.studio.MainWindow;

/**
 * IPCAM: http://www.coderanch.com/t/569500/sockets/java/Ip-camera-view
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class BambiApp {

	private static final Logger log = LoggerFactory.getLogger(BambiApp.class);
	
	
	private BambiApp() {
	}
	
	/**
	 * Initializes internal GUI components and displays the main window.
	 */
	public void createAndShowGUI() {
		
		final Configuration config = ConfigManager.getInstance().getConfiguration();
		String lookAndFeel = config.getLookAndFeel();
		if(StringUtils.isEmpty(lookAndFeel)) {
			lookAndFeel = UIManager.getSystemLookAndFeelClassName();
		}
		try {
			UIManager.setLookAndFeel(lookAndFeel);
			log.debug("using {}", lookAndFeel);
		}
		catch(Exception e) {
			String sysLNF = UIManager.getSystemLookAndFeelClassName();
			log.warn("invalid " + ConfigParameters.LOOK_AND_FEEL + " [{}]; defaulting to {}", lookAndFeel, sysLNF);
			try { UIManager.setLookAndFeel(sysLNF); } catch(Exception e1) {}
		}

		File preloadedImage = config.getAutoloadImageFile();
		
        MainWindow win = new MainWindow();
        win.initialize();
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if(preloadedImage != null) win.preloadImage(preloadedImage);
        win.setVisible(true);
	}
	
	/**
	 * Application entry point. This is where it all begins ..
	 * 
	 * @param args custom property file name/path at index 0
	 */
	public static void main(String[] args) {

		log.info("greetings! initializing program ..");
		
		String propsPath = "/bambi.properties";
		if(args.length == 1) propsPath = args[0];
		
		log.info("loading settings from {} ...", propsPath);
		Properties props = new Properties();
		try { 
			InputStream stream = args.length == 1 ? 
					new FileInputStream(propsPath) : BambiApp.class.getResourceAsStream(propsPath);
			props.load(stream);
		} catch (Exception e) {
			log.error("could not read configuration file; aborting! [error: {}]", e.getMessage());
			return;
		}
		
		ConfigLoader configLoader = new ConfigLoader(props);
		if(!configLoader.initializeParameters()) {
			log.error("invalid configuration settings; aborting!");
			return;
		}
		configLoader.registerConfiguration();

		new BambiApp().createAndShowGUI();
	}
}