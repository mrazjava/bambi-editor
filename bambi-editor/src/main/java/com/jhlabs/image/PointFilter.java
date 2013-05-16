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

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * An abstract superclass for point filters. The interface is the same as the 
 * old RGBImageFilter.
 * 
 * @author Jerry Huxtable (2006)
 * @author Adam Zimowski (mrazjava) (2013) - event handling and warning cleanup
 */
public abstract class PointFilter extends AbstractBufferedImageOp {

	protected boolean canFilterIndexColorModel = false;

    public BufferedImage filter( BufferedImage src, BufferedImage dst ) {

        int width = src.getWidth();
        int height = src.getHeight();
		int type = src.getType();
		WritableRaster srcRaster = src.getRaster();

        if ( dst == null )
            dst = createCompatibleDestImage( src, null );
		WritableRaster dstRaster = dst.getRaster();

        setDimensions( width, height);

        filterStart(width*height);
		int[] inPixels = new int[width];
        for ( int y = 0; y < height; y++ ) {
			// We try to avoid calling getRGB on images as it causes them to become unmanaged, causing horrible performance problems.
			if ( type == BufferedImage.TYPE_INT_ARGB ) {
				srcRaster.getDataElements( 0, y, width, 1, inPixels );
				for ( int x = 0; x < width; x++ ) {
					inPixels[x] = filterRGB( x, y, inPixels[x] );
				}
				dstRaster.setDataElements( 0, y, width, 1, inPixels );
			} else {
				src.getRGB( 0, y, width, 1, inPixels, 0, width );
				for ( int x = 0; x < width; x++ ) {
					inPixels[x] = filterRGB( x, y, inPixels[x] );
				}
				dst.setRGB( 0, y, width, 1, inPixels, 0, width );
			}
			if(y%100==0) filterProgress(Math.round(((float)y/height)*100));
        }
        filterDone();

        return dst;
    }

/*
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
    	long start = System.currentTimeMillis();
    	int width = src.getWidth();
        int height = src.getHeight();
		int type = src.getType();

		dst = ImageUtil.deepCopy(src);
        setDimensions( width, height);
        int pxSize = width*height;
		int[] inPixels = new int[pxSize];

				src.getRGB( 0, 0, width, height, inPixels,0, width);
				for ( int x = 0; x < pxSize; x++ )
					inPixels[x] = filterRGB( 0, 0, inPixels[x] );
				dst.setRGB( 0, 0, width, height, inPixels, 0, width);

		long end = System.currentTimeMillis();
		log.debug("TIME [ms]: {}", (end-start));
        return dst;
    }
*/
	public void setDimensions(int width, int height) {
	}

	public abstract int filterRGB(int x, int y, int rgb);
}
