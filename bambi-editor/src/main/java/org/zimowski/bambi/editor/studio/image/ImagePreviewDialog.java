package org.zimowski.bambi.editor.studio.image;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.zimowski.bambi.editor.ExtensionFileFilter;
import org.zimowski.bambi.editor.config.Configuration;

/**
 * Very simple image preview dialog which paints exactly the image set via 
 * constructor to the window. To use this dialog, simply instantiate it and 
 * call showVisible(true).
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class ImagePreviewDialog extends JDialog {

	private static final long serialVersionUID = 8956391638585109377L;
	
	private BufferedImage image;
	
	public ImagePreviewDialog(Frame parent, String title, BufferedImage image) {

		super(parent, title);
	    this.image = image;

	    buildGui();
	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	    setPositionRelativeToParent(parent);
	    pack(); 
	}
	
	private void buildGui() {
	    
		JPanel messagePane = new JPanel();
	    messagePane.add(new JLabel(new ImageIcon(image)));
	    getContentPane().add(messagePane);
	    JPanel buttonPane = new JPanel();
	    JButton button = new JButton("Save To Computer", new ImageIcon(getClass().getResource(Configuration.RESOURCE_PATH + "filesave16x16.png"))); 
	    buttonPane.add(button); 
	    button.addActionListener(
	    	new ActionListener() {
	    		public void actionPerformed(ActionEvent aEvent) {
	    			saveToFile();
	    		}
	    	}
	    		);
	    getContentPane().add(buttonPane, BorderLayout.SOUTH);		
	}
	
	/**
	 * Centers this window within the boundaries of parent, if available. If 
	 * parent is null this operation does nothing.
	 * 
	 * @param parent
	 */
	public void setPositionRelativeToParent(Frame parent) {
		if (parent != null) {
		      Dimension parentSize = parent.getSize(); 
		      Point p = parent.getLocation();
		      Dimension mySize = getPreferredSize();
		      int x = (p.x + parentSize.width / 2) - mySize.width / 2;
		      int y = (p.y + parentSize.height / 4) - mySize.height / 4;
		      setLocation(x, y);
		}
	}
	  
	private void saveToFile() {
		try {
			JFileChooser fileDialog = new JFileChooser();
			
			int imgType = image.getType();
			String extLbl = imgType == 0 ? "png" : "jpg, jpeg";
			String[] extArray = imgType == 0 ? new String[]{"PNG"} : new String[]{"JPG", "JPEG"};
			String fmt = imgType == 0 ? "png" : "jpg";
			
			FileFilter imageFilter = new ExtensionFileFilter(
					"Picture Files (" + extLbl + ")", extArray);
			fileDialog.setFileFilter(imageFilter);

			int selection = fileDialog.showSaveDialog(this);
			if (selection == JFileChooser.APPROVE_OPTION) {
				File filePath = fileDialog.getSelectedFile();
				ImageIO.write(image, fmt, filePath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}