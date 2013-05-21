package org.zimowski.bambi.editor.studio.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.PixelGrabber;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.config.Configuration;

/**
 * Utility routines for handling image tasks.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public final class EditorImageUtil {

	private static final Logger log = LoggerFactory.getLogger(EditorImageUtil.class);
	
	private static final short[] INVERT_TABLE;
	
	static {
		INVERT_TABLE = new short[256];
		for (int i = 0; i < 256; i++) INVERT_TABLE[i] = (short) (255 - i);
	}
	
	/**
	 * Instantiating this utility class is pointless.
	 */
	private EditorImageUtil() {
	}
	
	public static BufferedImage toBufferedImage(Image image) {

		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels
		boolean hasAlpha = hasAlpha(image);
		// Create a buffered image with a format that's compatible with the
		// screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha == true) {
				transparency = Transparency.BITMASK;
			}
			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image
					.getHeight(null), transparency);
		} catch (HeadlessException e) {
		} // No screen
		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha == true) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null), image
					.getHeight(null), type);
		}
		// Copy image to buffered image
		Graphics g = bimage.createGraphics();
		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bimage;
	}

	public static boolean hasAlpha(Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			return ((BufferedImage) image).getColorModel().hasAlpha();
		}
		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}
		// Get the image's color model
		return pg.getColorModel().hasAlpha();
	}

    /**
     * Copies source image (regardless of format) to build output in JPEG 
     * format. The color argument is most relevant if image corners are to be 
     * clipped (to make round image), in which case the clipped areas would 
     * be filled with that color.
     * 
     * @param source - source image to copy
     * @param color - background color for clipped corner areas; may be null
     * @param clip - true if image to be round; false if rectangular
     * @return 
     */
	public static BufferedImage makeJpg(BufferedImage source, Paint color, boolean clip) {
    	
    	int width = source.getWidth();
    	int height = source.getHeight();
    	
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = result.createGraphics();
		
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        rh.put(RenderingHints.KEY_RENDERING,
               RenderingHints.VALUE_RENDER_QUALITY);
        
        g.setComposite(AlphaComposite.SrcOver);
        g.setPaint(color);
        g.fillRect(0, 0, width, height);

        if(clip) {
        	g.setClip(new Ellipse2D.Double(0, 0, width, height));
        }
        g.drawImage(source, 0, 0, null);

		return result;
    }
	
    /**
     * Copies source image (regardless of format) to build output in PNG 
     * format. If image corners are to be clipped, the will be transparent.
     * 
     * @param source
     * @param clip - true if image to be round; false if rectangular
     * @return
     */
    public static BufferedImage makePng(BufferedImage source, boolean clip) {
    	
    	int width = source.getWidth();
    	int height = source.getHeight();
    	
    	log.debug(String.format("w: {}, h: {}, c: %s", clip), width, height);
    	
		GraphicsConfiguration gc = source.createGraphics().getDeviceConfiguration();
		BufferedImage result = gc.createCompatibleImage(width, height, Transparency.BITMASK);
		Graphics2D g = result.createGraphics();

        g.setComposite(AlphaComposite.Src);

        if(clip) {
        	g.setClip(new Ellipse2D.Double(0, 0, width, height));
        }
        g.drawImage(source, 0, 0, null);

		return result;
    }

    public static AffineTransform findTranslation(
    		AffineTransform at, int width, int height, int angle) {
    	
    	Point2D p2din, p2dout;

	    p2din = new Point2D.Double(0.0, 0.0);
	    p2dout = at.transform(p2din, null);
	    double ytrans = p2dout.getY();

	    p2din = new Point2D.Double(0, height);
	    p2dout = at.transform(p2din, null);
	    double xtrans = p2dout.getX();

	    log.debug("BEFORE: xtrans [{}], ytrans [{}]", xtrans, ytrans);
	    
	    if(angle == 270) {
	    	
	    	log.trace("270 rotation");
	    	
	    	double[] points = findTranslationPointsFor270(width, height);
	    	xtrans = points[0];
	    	ytrans = points[1];
	    }
	    else if(angle == 180) {
	    	xtrans = 0.0;
	    	ytrans = 0.0;
	    }
	    
	    log.debug("AFTER: xtrans [{}], ytrans [{}]", xtrans, ytrans);

	    AffineTransform tat = new AffineTransform();
	    tat.translate(-xtrans, -ytrans);
	    return tat;
    }

    private static double[] findTranslationPointsFor270(int width, int height) {
    	
    	double[] points = new double[2];
    	AffineTransform at = new AffineTransform();
    	at.rotate(Math.toRadians(90), width/2.0, height/2.0);

    	Point2D p2din, p2dout;

	    p2din = new Point2D.Double(0.0, 0.0);
	    p2dout = at.transform(p2din, null);
	    points[1] = p2dout.getY();

	    p2din = new Point2D.Double(0, height);
	    p2dout = at.transform(p2din, null);
	    points[0] = p2dout.getX();

    	return points;
    }
    
    /**
     * Rotates and/or scales the source image. If angle is null rotation is not 
     * performed. If either scale argument is null scaling is not performed.  
     * Rotation is done clockwise according to the angle. Angle is in degrees 
     * and can be any integer, positive or negative as it will always be 
     * normalized to positive angle between 0 and 360. For example, a -90 angle 
     * results in a clockwise rotation by 270 degrees. Likewise a degree of 
     * -375 would rotate the image by 15 degrees clockwise. Background color 
     * argument can be passed if the image format does not carry transparency, 
     * in which case image graphics context will use that color to paint the 
     * background. This will be apparent if the rotation is outside of a 90 
     * degree increment. Scale is a value between 0.0 and 1.0 where 1.0 is a 
     * full scale (100%).
     * 
     * @param source - image to be rotated and/or scaled
     * @param angle - angle in degrees by how much the image should be rotated. 
     * 	Typically the angle is in increments of 90 degrees.
     * @param bgColor - color to fill the background with
     * @param scaleX - percent by which to scale x-axis
     * @param scaleY - percent by which to scale y-axis
     * @return rotated/scaled image
     */
    public static BufferedImage rotate(BufferedImage source, Integer angle, Color bgColor, Double scaleX, Double scaleY) {
    	
    	final boolean scale = (scaleX != null && scaleY != null);
    	
    	double sx = 1D;
    	double sy = 1D;

        if(scale) {
        	sx = scaleX;
        	sy = scaleY;
        }

        if(angle == null) {
        	angle = 0;
        }
        else if(angle < 0) {
    		int negAngle = angle;
    		angle = 360 + (negAngle % 360);
    		log.debug("negative angle {} converted to {}", negAngle, angle);
    	}
    	
        int imgType = source.getType();
        if(imgType == 0) imgType = BufferedImage.TYPE_INT_RGB;

    	log.debug("angle: {}, imgType: {}", angle, imgType);
    	
    	final Double width = new Double(source.getWidth());
    	final Double height = new Double(source.getHeight());
    	
    	log.debug("width: {}, height: {}", width, height);

    	double sin = Math.abs(Math.sin(Math.toRadians(angle)));
    	double cos = Math.abs(Math.cos(Math.toRadians(angle)));
    	
    	log.debug("sin: {}, cos: {}", sin, cos);
    	
    	int newWidth = (int)Math.round(((height*sy) * sin + (width*sx) * cos));
    	int newHeight = (int)Math.round(((height*sy) * cos + (width*sx) * sin));
    	
    	log.debug("newWidth: {}, newHeight: {}", newWidth, newHeight);

        final AffineTransform at = new AffineTransform();

        at.translate(newWidth/2D, newHeight/2D);
        if(scale) at.scale(sx, sy);
        at.rotate(Math.toRadians(angle));
        at.translate(-0.5*width, -0.5*height);

        BufferedImage dest = new BufferedImage(newWidth, newHeight, imgType);

        Graphics2D g2 = dest.createGraphics();
        if(bgColor != null) g2.setBackground(bgColor);
        g2.drawRenderedImage(source, at);

        g2.dispose();
    	
    	return dest;
    }
    
	/**
	 * 
	 * @param img - Image to modify
	 * @throws Exception
	 */
	public static BufferedImage applySepiaFilter(BufferedImage src) {

		final int width = src.getWidth();
		final int height = src.getHeight();
		int type = src.getType();
		
		if(type == 0) type = BufferedImage.TYPE_INT_RGB;
		
		// Create a copy of the original image in grayscale.
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		//ColorSpace cs = src.getColorModel().getColorSpace();
		BufferedImageOp biop = new ColorConvertOp(cs, null);
		BufferedImage bi1 = biop.filter(src, null);

		// Convert grayscaled image from TYPE_GRAY to TYPE_RGB.
		BufferedImage bi2 = new BufferedImage(width, height, type);
		Graphics g2 = bi2.createGraphics();
		g2.drawImage(bi1, 0, 0, null);
		g2.dispose();

		BufferedImage dest = new BufferedImage(width, height, type);

		Graphics2D g2d = dest.createGraphics();
		float[] scales = new float[] { 1.0f, 1.0f, 1.0f };
		// controls sepia levels
		float[] offsets = new float[] { 40.0f, 20.0f, -20.0f };
		g2d.drawImage(bi2, new RescaleOp(scales, offsets, null), 0, 0);

		g2d.dispose();

		return dest;
	}
	
	/**
	 * Loads icon from classpath as a resource.
	 * 
	 * @param resourcePath path to the icon relative to {@link Configuration#RESOURCE_PATH}
	 * @return requested icon
	 */
	public static BufferedImage getLocalIcon(String resourcePath) {
		BufferedImage icon = null;
		String path = Configuration.RESOURCE_PATH + resourcePath;
		URL url = EditorImageUtil.class.getResource(path);
		try {
			icon = ImageIO.read(url);
		} catch (IOException e) {
			log.error("problem loading a resource [{}]; {}", resourcePath, e.getMessage());
		}
		return icon;
	}
}