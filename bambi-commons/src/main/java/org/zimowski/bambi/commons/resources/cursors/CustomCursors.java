package org.zimowski.bambi.commons.resources.cursors;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom cursors. Each enum is capable of fully initializing its own cursor.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public enum CustomCursors {
	Blank,
	Grab("grab16x16.png"), 
	Grabbed("grabbed16x16.png"),
	Drag("handdrag27x17.png", new Point(0, 10));
	
	private static final Logger log = 
			LoggerFactory.getLogger(CustomCursors.class);
	
	//private final static String RESOURCE_PATH = "/org/zimowski/bambi/commons/resources/cursors/";
	
	private static final String RESOURCE_PATH = "/" +  
			CustomCursors.class.getPackage().getName().replace('.', '/') + "/";
	
	private String fileName;
	
	private Point hotspot;
	
	private CustomCursors() {
		this(null, new Point(0, 0));
	}
	
	private CustomCursors(String fileName) {
		this(fileName, new Point(7, 7));
	}
	
	private CustomCursors(String fileName, Point hotspot) {
		this.fileName = fileName;
		this.hotspot = hotspot;		
	}
	
    /**
     * @return custom cursor ready for use
     */
    public Cursor getCursor() {
    	Cursor cursor;
    	Toolkit toolkit = Toolkit.getDefaultToolkit();
    	Image image = getImage();
        cursor = toolkit.createCustomCursor(image, hotspot, toString());
    	return cursor;
    }
    
    public Image getImage() {
    	
    	Image image = null;
    	
    	if(fileName != null) {
	    	URL handGrabUrl = getClass().getResource(RESOURCE_PATH + fileName);
	        try { image = ImageIO.read(handGrabUrl); }
	        catch(IOException e) { log.error(e.getMessage()); }
    	}
    	
    	if(image == null) {
    		// transparent 16 x 16 pixel cursor image
    		image = new BufferedImage(
    				16, 
    				16, 
    				BufferedImage.TYPE_INT_ARGB);
    	}
    	
    	return image;
    }

}