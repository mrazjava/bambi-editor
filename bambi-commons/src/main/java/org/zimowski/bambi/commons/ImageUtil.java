package org.zimowski.bambi.commons;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class ImageUtil {

	private static final Logger log = LoggerFactory.getLogger(ImageUtil.class);
	
	/**
	 * Given source image, makes and returns identical copy.
	 * 
	 * @param source
	 * @return
	 */
	public static BufferedImage deepCopy(BufferedImage source) {

		ColorModel cm = source.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = source.copyData(null);
		BufferedImage copy = new BufferedImage(cm, raster,
				isAlphaPremultiplied, null);

		return copy;
	}

	/**
	 * Computes transformation necessary to perform proper rotation given EXIF 
	 * orientation integer. The orientation value is standard EXIF tag.
	 *  
	 * @param orientation
	 * @param imageWidth image width before the desired rotation
	 * @param imageHeight image height before the desired rotation
	 * @return
	 */
	public static AffineTransform getExifTransformation(int orientation,
			int imageWidth, int imageHeight) {

		AffineTransform t = new AffineTransform();
		log.debug("orientation: {}", orientation);

		switch (orientation) {
		case 1:
			break;
		case 2: // Flip X
			t.scale(-1.0, 1.0);
			t.translate(-imageWidth, 0);
			break;
		case 3: // PI rotation
			t.translate(imageWidth, imageHeight);
			t.rotate(Math.PI);
			break;
		case 4: // Flip Y
			t.scale(1.0, -1.0);
			t.translate(0, -imageHeight);
			break;
		case 5: // - PI/2 and Flip X
			t.rotate(-Math.PI / 2);
			t.scale(-1.0, 1.0);
			break;
		case 6: // -PI/2 and -width
			t.translate(imageHeight, 0);
			t.rotate(Math.PI / 2);
			break;
		case 7: // PI/2 and Flip
			t.scale(-1.0, 1.0);
			t.translate(-imageHeight, 0);
			t.translate(0, imageWidth);
			t.rotate(3 * Math.PI / 2);
			break;
		case 8: // PI / 2
			t.translate(0, imageWidth);
			t.rotate(3 * Math.PI / 2);
			break;
		}

		return t;
	}
	
	/**
	 * Applies transformation to the image returning transformed image.
	 * 
	 * @param image image on which transform should be processed
	 * @param transform transform to process on the image
	 * @return new image with transform applied
	 * @throws Exception
	 */
	public static BufferedImage transformImage(BufferedImage image, AffineTransform transform) throws Exception {

	    AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);

	    BufferedImage destinationImage = op.createCompatibleDestImage(image,  (image.getType() == BufferedImage.TYPE_BYTE_GRAY)? image.getColorModel() : null );
	    Graphics2D g = destinationImage.createGraphics();
	    g.setBackground(Color.WHITE);
	    g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
	    destinationImage = op.filter(image, destinationImage);;
	    return destinationImage;
	}
}