/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.jhlabs.image;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

import org.zimowski.bambi.editor.filters.FilterListener;
import org.zimowski.bambi.editor.filters.ImageFilterOpSupport;
import org.zimowski.bambi.editor.filters.ImageFilterOps;
import org.zimowski.bambi.jhlabs.image.ImageUtils;

/**
 * A convenience class which implements those methods of BufferedImageOp which are rarely changed.
 * 
 * @author Jerry Huxtable (2006)
 * @author Adam Zimowski (mrazjava) (2013) - event handling and warning cleanup
 */
public abstract class AbstractBufferedImageOp 
	implements BufferedImageOp, Cloneable, FilterListener, ImageFilterOpSupport {

	protected FilterListener filterListener;
	
	public void setFilterListener(FilterListener listener) {
		this.filterListener = listener;
	}
	
	public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
		return createCompatibleDestImage(src, dstCM, null, null);
	}
	
	public BufferedImage createCompatibleDestImage(BufferedImage src,
			ColorModel dstCM, Integer width, Integer height) {

		return ImageUtils.createCompatibleImage(src, dstCM, width, height);
	}

    public Rectangle2D getBounds2D( BufferedImage src ) {
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }
    
    public Point2D getPoint2D( Point2D srcPt, Point2D dstPt ) {
        if ( dstPt == null )
            dstPt = new Point2D.Double();
        dstPt.setLocation( srcPt.getX(), srcPt.getY() );
        return dstPt;
    }

    public RenderingHints getRenderingHints() {
        return null;
    }

	/**
	 * A convenience method for getting ARGB pixels from an image. This tries to avoid the performance
	 * penalty of BufferedImage.getRGB unmanaging the image.
     * @param image   a BufferedImage object
     * @param x       the left edge of the pixel block
     * @param y       the right edge of the pixel block
     * @param width   the width of the pixel arry
     * @param height  the height of the pixel arry
     * @param pixels  the array to hold the returned pixels. May be null.
     * @return the pixels
     * @see #setRGB
     */
	public int[] getRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
		int type = image.getType();
		if ( type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
			return (int [])image.getRaster().getDataElements( x, y, width, height, pixels );
		return image.getRGB( x, y, width, height, pixels, 0, width );
    }

	/**
	 * A convenience method for setting ARGB pixels in an image. This tries to avoid the performance
	 * penalty of BufferedImage.setRGB unmanaging the image.
     * @param image   a BufferedImage object
     * @param x       the left edge of the pixel block
     * @param y       the right edge of the pixel block
     * @param width   the width of the pixel arry
     * @param height  the height of the pixel arry
     * @param pixels  the array of pixels to set
     * @see #getRGB
	 */
	public void setRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
		int type = image.getType();
		if ( type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
			image.getRaster().setDataElements( x, y, width, height, pixels );
		else
			image.setRGB( x, y, width, height, pixels, 0, width );
    }

	public Object clone() {
		try {
			return super.clone();
		}
		catch ( CloneNotSupportedException e ) {
			return null;
		}
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void filterInitialize() {
		if(filterListener != null) filterListener.filterInitialize();
	}

	@Override
	public void filterStart(int totalPixels) {
		if(filterListener != null) filterListener.filterStart(totalPixels);
	}

	@Override
	public void filterProgress(int percentComplete) {
		if(filterListener != null) filterListener.filterProgress(percentComplete);
	}

	@Override
	public void filterDone() {
		if(filterListener != null) filterListener.filterDone();
	}

	/**
	 * Bambi filter metadata. This implementation always returns throws 
	 * exception because this method can only be called if defined. This method 
	 * should be overwritten if child filter is used within a Bambi app. The 
	 * reason exception is throw rather than returning null is to help identify 
	 * offending class which is much easier than tracing NPE.
	 * 
	 * @throws IllegalStateException on every invocation
	 */
	@Override
	public ImageFilterOps getMetaData() {
		throw new IllegalStateException();
	}
}