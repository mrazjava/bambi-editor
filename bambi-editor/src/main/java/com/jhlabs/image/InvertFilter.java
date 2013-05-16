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

import org.zimowski.bambi.editor.filters.ImageFilterOps;


/**
 * A filter which inverts the RGB channels of an image.
 * 
 * @author Jerry Huxtable (2006)
 * @author Adam Zimowski (mrazjava) (2013) - event handling and warning cleanup
 */
public class InvertFilter extends PointFilter {
	
	public InvertFilter() {
		canFilterIndexColorModel = true;
	}

	public int filterRGB(int x, int y, int rgb) {
		int a = rgb & 0xff000000;
		return a | (~rgb & 0x00ffffff);
	}

	@Override
	public String toString() {
		return getMetaData().toString();
	}

	@Override
	public ImageFilterOps getMetaData() {
		return ImageFilterOps.Negative;
	}
}