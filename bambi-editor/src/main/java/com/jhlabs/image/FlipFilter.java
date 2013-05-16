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
import java.awt.image.ColorModel;

import org.zimowski.bambi.editor.filters.ImageFilterOps;

/**
 * A filter which flips images or rotates by multiples of 90 degrees.
 * 
 * @author Jerry Huxtable (2006)
 * @author Adam Zimowski (mrazjava) (2013) - event handling and warning cleanup
 */
public class FlipFilter extends AbstractBufferedImageOp {
	
	/**
     * Flip the image horizontally.
     */
    public static final int FLIP_H = 1;

	/**
     * Flip the image vertically.
     */
	public static final int FLIP_V = 2;

	/**
     * Flip the image horizontally and vertically.
     */
	public static final int FLIP_HV = 3;

	/**
     * Rotate the image 90 degrees clockwise.
     */
	public static final int FLIP_90CW = 4;

	/**
     * Rotate the image 90 degrees counter-clockwise.
     */
	public static final int FLIP_90CCW = 5;

	/**
     * Rotate the image 180 degrees.
     */
	public static final int FLIP_180 = 6;

	private int operation;

    /**
     * Construct a FlipFilter which flips horizontally and vertically.
     */
	public FlipFilter() {
		this(FLIP_HV);
	}

    /**
     * Construct a FlipFilter.
     * @param operation the filter operation
     */
	public FlipFilter(int operation) {
		this.operation = operation;
	}

    /**
     * Set the filter operation.
     * @param operation the filter operation
     * @see #getOperation
     */
	public void setOperation(int operation) {
		this.operation = operation;
	}

    /**
     * Get the filter operation.
     * @return the filter operation
     * @see #setOperation
     */
	public int getOperation() {
		return operation;
	}

    public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        int width = src.getWidth();
        int height = src.getHeight();

		int[] inPixels = getRGB( src, 0, 0, width, height, null );

		int w = width;
		int h = height;

		int newW = w;
		int newH = h;
		switch (operation) {
		case FLIP_HV:
			newW = h;
			newH = w;
			break;
		case FLIP_90CW:
			newW = h;
			newH = w;
			break;
		case FLIP_90CCW:
			newW = h;
			newH = w;
			break;
		}

		final int totalPixels = newW * newH;
		int[] newPixels = new int[totalPixels];

		filterStart(totalPixels);
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				int index = row * width + col;
				int newRow = row;
				int newCol = col;
				switch (operation) {
				case FLIP_H:
					newCol = w - col - 1;
					break;
				case FLIP_V:
					newRow = h - row - 1;
					break;
				case FLIP_HV:
					newRow = col;
					newCol = row;
					break;
				case FLIP_90CW:
					newRow = col;
					newCol = h - row - 1;;
					break;
				case FLIP_90CCW:
					newRow = w - col - 1;
					newCol = row;
					break;
				case FLIP_180:
					newRow = h - row - 1;
					newCol = w - col - 1;
					break;
				}
				int newIndex = newRow * newW + newCol;
				newPixels[newIndex] = inPixels[index];
				if(newIndex%10000==0) filterProgress(Math.round(((float)newIndex/totalPixels)*100));
			}
		}

        if ( dst == null ) {
            ColorModel dstCM = src.getColorModel();
			dst = new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(newW, newH), dstCM.isAlphaPremultiplied(), null);
		}
        
		setRGB( dst, 0, 0, newW, newH, newPixels );
		filterDone();
        return dst;
    }

	@Override
	public String toString() {
		return getMetaData().toString();
	}

	@Override
	public ImageFilterOps getMetaData() {
		return ImageFilterOps.Mirror;
	}
}