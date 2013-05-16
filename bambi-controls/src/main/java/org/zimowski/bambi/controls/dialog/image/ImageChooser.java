package org.zimowski.bambi.controls.dialog.image;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.commons.SwingUtil;

/**
 * Customized image file chooser capable of image and directory preview. 
 * Large images display loading in progress icon if generating thumb takes 
 * longer than EDT standard allows. Thumb generation is done off EDT ofcourse. 
 * The inner JList responds to key press events, enter in particular, 
 * allowing to navigate the dialog with a keyboard. The approve button is 
 * interactively disabled/enabled depending on contextual selection.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class ImageChooser extends JFileChooser implements KeyListener {

	private static final long serialVersionUID = -8164204758164576492L;
	
	private final static Logger log = LoggerFactory.getLogger(ImageChooser.class);
	
	/**
	 * Supported image type
	 */
	static final String EXT_JPG = "jpg";
	
	/**
	 * Supported image type
	 */
	static final String EXT_JPEG = "jpeg";
	
	/**
	 * Supported image type
	 */
	static final String EXT_PNG = "png";
	
	private JButton approveButton = null;
	
	public ImageChooser() {
		// get handle on approve button since we need to work with it
		List<JButton> buttons = SwingUtil.getDescendantsOfType(JButton.class, this);
		for (JButton button : buttons) {
			if ("OPEN".equalsIgnoreCase(button.getActionCommand())) {
				approveButton = button;
			}
		}
		setEnabledApproveButton(false);
		
		setPreferredSize(new Dimension(600, 400));
		// define supported extensions
		FileNameExtensionFilter allPicsFilter = new FileNameExtensionFilter(
				"All Supported Images", "jpg", "jpeg", "png");
		FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter(
				"JPEG Images", "jpg", "jpeg");
		FileNameExtensionFilter pngFilter = new FileNameExtensionFilter(
				"PNG Images", "png");
		
		// we allow files and directories, though directories are non
		// selectable; this is a result of a really dumb design as normally 
		// we would like FILES_ONLY but then PropertyChangeListener will not 
		// report SELECTED_FILE_CHANGED_PROPERTY for directories (craps null 
		// at us) - but we want to know about selected directory so we can 
		// display its content in a preview, ugh! Anyway, we do some clever 
		// hacking with approve button to work around this nasty design issue
		setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		setDialogTitle("Bambify your image!");
		addChoosableFileFilter(allPicsFilter);
		addChoosableFileFilter(jpgFilter);
		addChoosableFileFilter(pngFilter);
		setFileFilter(allPicsFilter);
		
		setAcceptAllFileFilterUsed(false);
		setFileView(new ImageFileView());
		//ImagePreview imagePreview = new ImagePreview();
		ImagePreview imagePreview = new ImagePreview();
		setAccessory(imagePreview);
		addPropertyChangeListener(imagePreview);
		
		Component jlist =  SwingUtil.getDescendantOfType(
				JList.class,
			    this, "Visible", true);
		jlist.addKeyListener(this);
	}
	
	public void setEnabledApproveButton(boolean enabled) {
		if(approveButton != null) {
			approveButton.setEnabled(enabled);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		File selectedFile = getSelectedFile();
		int code = e.getKeyCode();
		log.trace("key released: {}", code);
		if(code == 10 && selectedFile.isDirectory()) {
			setCurrentDirectory(getSelectedFile());
		}
	}
}