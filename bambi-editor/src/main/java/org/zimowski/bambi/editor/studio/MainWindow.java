package org.zimowski.bambi.editor.studio;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.config.ConfigManager;
import org.zimowski.bambi.editor.config.Configuration;
import org.zimowski.bambi.editor.studio.eventbus.EventBusManager;
import org.zimowski.bambi.editor.studio.eventbus.events.ImageLoadEvent;

/**
 * Image editing window. This window is considered application's core as it 
 * provides core functionality. This is where users edit their picture and 
 * upload it to the website.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class MainWindow extends JFrame {

	private static final long serialVersionUID = 7474236498527225026L;

	private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
	
	private Editor editPanel;
	
	private Configuration config;

	
	public MainWindow() {
		config = ConfigManager.getInstance().getConfiguration();
		String icon = Configuration.RESOURCE_PATH + "bambi20x20.png";
		try {
			setIconImage(ImageIO.read(getClass().getResource(icon)));
		}
		catch(IOException e) {
			log.warn("could not load main window icon: {}", icon);
		}
		final EventBusManager eventMgr = EventBusManager.getInstance();
		eventMgr.setWindowInstantiated(this);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				eventMgr.switchContext(MainWindow.this);
			}
		});
		setTitle(config.getWindowTitle());
	}
	
	/**
	 * Initializes window to get ready for display. Requires that configuration 
	 * is set and available. Will fail without configuration. Does not actually 
	 * display the window. Call {@link #setVisible(boolean)} to show the window.
	 * 
	 * @throws IllegalStateException if configuration is null
	 */
	public void initialize() {
		
		if(config == null)
			throw new IllegalStateException("configuration is not set!");
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				log.info("closing main window..");
				dispose();
			}
		});
		editPanel = new Editor();
		editPanel.initialize();
		addWindowListener(editPanel);
    	try { // we may get OutOfMemoryError if image file is too large
	        setContentPane(editPanel);
	        setPreferredSize(
	        		new Dimension(
	        				config.getWindowWidth(), 
	        				config.getWindowHeight()
	        		)
	        );
	        setMinimumSize(new Dimension(600, 450));
	        pack();
	        positionOnScreen();
    	}
    	catch(OutOfMemoryError e) {
    		String msg = "The image you're trying to load is too large, and " +
    				"there is not enough memory to handle it. You can fix " +
    				"this in two ways:\n" +
    				"\n1) Reduce size of the image using another program by " +
    				"resaving it with smaller file size or lower resolution" +
    				"\n2) Configure Java plugin to let it use more memory." +
    				"\n\nSee help (" + config.getHelpPageUrl() + ") " +
    				"on how to setup more memory (solution 2).";
			
    		JTextArea textArea = new JTextArea(6, 40);
    		textArea.setText(msg);
    		textArea.setEditable(false);
    		textArea.setLineWrap(true);
    		textArea.setWrapStyleWord(true);
    		textArea.setCaretPosition(0);
    	      
    	    // wrap a scrollpane around it
    		JScrollPane scrollPane = new JScrollPane(textArea);
    		scrollPane.setWheelScrollingEnabled(true);    		
    		
    		JOptionPane.showMessageDialog(null,
					scrollPane,
				    "Out of Memory Error",
				    JOptionPane.ERROR_MESSAGE);
    	}
	}
	
	/**
	 * Autoloads image into editor.
	 * 
	 * @param image file of the image that should be automatically loaded
	 */
	public void preloadImage(File image) {
		EventBusManager eventMgr = EventBusManager.getInstance();
		log.debug("initiating {} for {}", ImageLoadEvent.class.getSimpleName(), image.getAbsolutePath());
		// need to set context as this may be called on startup
		eventMgr.switchContext(this);
		eventMgr.getBus().post(new ImageLoadEvent(image, true));		
	}
	
	/**
	 * Centers window in the middle of a screen. If dual monitor setup detects 
	 * main (default) screen and centers on that screen.
	 */
	private void positionOnScreen() {
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		DisplayMode dm = gs.getDisplayMode();
		
		int screenWidth = dm.getWidth();
		int screenHeight = dm.getHeight();
		int windowWidth = getWidth();
		int windowHeight = getHeight();
		
		log.info(String.format("screen [w: %d, h: %d] window [w: %d, h: %d]", 
				screenWidth, screenHeight, windowWidth, windowHeight));
        
        setLocationRelativeTo(null);
	}
}