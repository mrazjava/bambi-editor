package org.zimowski.bambi.editor.filters;

import java.awt.image.BufferedImage;

import org.zimowski.bambi.editor.studio.image.EditorImageUtil;
import org.zimowski.bambi.jhlabs.image.NoiseFilter;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class SepiaFilter extends NoiseFilter {
	
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		filterInitialize();
		BufferedImage tmp = EditorImageUtil.applySepiaFilter(src);
		return super.filter(tmp, dst);
	}
	
	@Override
	public String toString() {
		return getMetaData().toString();
	}

	@Override
	public ImageFilterOps getMetaData() {
		return ImageFilterOps.OldPhoto;
	}
}