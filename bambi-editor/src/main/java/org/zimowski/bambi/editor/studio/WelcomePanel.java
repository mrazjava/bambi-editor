package org.zimowski.bambi.editor.studio;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.config.ConfigManager;
import org.zimowski.bambi.editor.config.Configuration;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class WelcomePanel extends JPanel {

	private static final long serialVersionUID = -273279993467339007L;
	
	public static final Logger log = LoggerFactory.getLogger(WelcomePanel.class);
	
	
	public WelcomePanel() {
		super(new BorderLayout());
		
		ConfigManager mgr = ConfigManager.getInstance();
		Configuration config = mgr.getConfiguration();

		boolean welcomeOk = true;	// assume success
		JEditorPane editPane = new JEditorPane();
		
		try {
			String welcomeUrl = config.getWelcomeUrl();
			log.debug("welcome url: {}", welcomeUrl);
			editPane.setPage(welcomeUrl);
		} catch (Exception e) {
			try {
				String resource = config.getWelcomeResourcePath();
				URL url = WelcomePanel.class.getResource(resource);
				log.debug("welcome resource: {}", url.toExternalForm());
				editPane.setPage(url);
			}
			catch(Exception e1) {
				log.error(e1.getMessage());
				welcomeOk = false;
			}
		}
		
		if(welcomeOk) add(editPane, BorderLayout.CENTER);
	}
}