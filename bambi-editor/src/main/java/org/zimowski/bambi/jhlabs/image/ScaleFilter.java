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

package org.zimowski.bambi.jhlabs.image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jhlabs.image.AbstractBufferedImageOp;

/**
 * Scales an image using the area-averaging algorithm, which can't be done with AffineTransformOp.
 */
public class ScaleFilter extends AbstractBufferedImageOp {

	private static final Logger log = LoggerFactory.getLogger(ScaleFilter.class);
	
	private int width;
	private int height;
	
	/**
	 * Decimal percent representation; 0.1 (10%) - 5.0 (500%)
	 */
	private double zoom;

    /**
     * Construct a ScaleFilter.
     */
	public ScaleFilter() {
		this(32, 32);
	}
	
	public ScaleFilter(double zoom) {
		setZoom(zoom);
	}

    /**
     * Construct a ScaleFilter.
     * @param width the width to scale to
     * @param height the height to scale to
     */
	public ScaleFilter(int width, int height ) {
		this.width = width;
		this.height = height;
	}
	
	public void setZoom(double zoom) {
		if(zoom <= 0D || zoom > 500D) {
			log.error("zoom [{}] out of range; not set", zoom);
			return;
		}
		this.zoom = zoom;
	}
    public BufferedImage filter( BufferedImage src, BufferedImage dst ) {

    	if(zoom > 0D) { // use zoom if defined
    		width = (int)(src.getWidth() * zoom);
    		height = (int)(src.getHeight() * zoom);
    	}
    	
    	if ( dst == null ) {
			ColorModel dstCM = src.getColorModel();
			//dst = new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster( width, height ), dstCM.isAlphaPremultiplied(), null);
    		dst = createCompatibleDestImage(src, dstCM, width, height);
		}

		// SCALE_AREA_AVERAGING produces undesired effect of distorted circumference if image is oval
    	Image scaleImage = src.getScaledInstance( width, height, Image.SCALE_REPLICATE);
		Graphics2D g = dst.createGraphics();
		g.drawImage( scaleImage, 0, 0, width, height, null );
		g.dispose();

        return dst;
    }

	public String toString() {
		return "Distort/Scale";
	}

}
