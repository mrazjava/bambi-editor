package org.zimowski.bambi.editor.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.jhlabs.image.ContrastFilter;
import org.zimowski.bambi.jhlabs.image.HSBAdjustFilter;

import com.jhlabs.image.RGBAdjustFilter;

/**
 * A composite filter that simultaneously adjusts RGB, Hue, Saturation, 
 * Brightness and Contrast. The core underlying filters from jhlabs: 
 * {@link RGBAdjustFilter}, {@link HSBAdjustFilter} and {@link ContrastFilter} 
 * are encapsulated so that pixel adjustment for all operations takes place 
 * in a single loop, resulting in huge performance increase as opposed to 
 * running each filter separately.
 * 
 * @author Adam Zimowski
 */
public class ColorAdjustFilter extends ContrastFilter {
	
	private static final Logger log = LoggerFactory.getLogger(ColorAdjustFilter.class);

	private RGBAdjustFilter rgbFilter;
	
	private HSBAdjustFilter hsbFilter;
	
	private ImageFilterOps currentOp;
	
	public enum AdjustType {
		Rgb, Hs;
	}
	
	private ImageFilterOps lastOp;
	
	
	public ColorAdjustFilter() {
		rgbFilter = new RGBAdjustFilter();
		hsbFilter = new HSBAdjustFilter();
	}
	
	/**
	 * Changes image natural hue upwards or downwards. Zero value defines 
	 * image's natural hue, therefore positive value intensifies the hue while 
	 * negative value decreases the hue.
	 * 
	 * @param hue value between -1 to +1
	 */
	public void setHue(float hue) {
		hsbFilter.setHFactor(hue);
	}
	
	/**
	 * @param saturation -1 to +1
	 */
	public void setSaturation(float saturation) {
		hsbFilter.setSFactor(saturation);
	}

	/**
	 * {@inheritDoc}
	 * @param brightness -1 to +1
	 */
	@Override
	public void setBrightness(float brightness) {
		super.setBrightness(brightness);
	}

	/**
	 * {@inheritDoc}
	 * @param contrast -1 to +1
	 */
	@Override
	public void setContrast(float contrast) {
		super.setContrast(contrast);
	}

	/**
	 * @param red -1 to +1
	 */
	public void setRed(float red) {
		rgbFilter.setRFactor(red);
	}
	
	/**
	 * @param green -1 to +1
	 */
	public void setGreen(float green) {
		rgbFilter.setGFactor(green);
	}

	/**
	 * @param blue -1 to +1
	 */
	public void setBlue(float blue) {
		rgbFilter.setBFactor(blue);
	}

	private boolean getRunRgbFilter() {
		return rgbFilter.getRFactor() != 0f || 
				rgbFilter.getGFactor() != 0f || 
				rgbFilter.getBFactor() != 0;
	}
	
	private boolean getRunHsbFilter() {
		return hsbFilter.getHFactor() != 0 || hsbFilter.getSFactor() != 0;
	}

	private boolean getRunContrastFilter() {
		return getBrightness() != 1f || getContrast() != 1f;
	}

	@Override
	public int filterRGB(int x, int y, int rgb) {
		int result = rgb;
		if(getRunRgbFilter()) result = rgbFilter.filterRGB(x, y, result);
		if(getRunHsbFilter()) result = hsbFilter.filterRGB(x, y, result);
		if(getRunContrastFilter()) result = super.filterRGB(x, y, result);
		return result;
	}
	
	/**
	 * @param resetHS true to reset hue and saturation
	 * @param resetCB true to reset contrast and brightness
	 */
	public void resetHscb(boolean resetHS, boolean resetCB) {
		if(resetHS) {
			setHue(0);
			setSaturation(0);
		}
		if(resetCB) {
			setContrast(1);
			setBrightness(1);
		}
		if(AdjustType.Hs.equals(getAdjustType(lastOp))) lastOp = null;
		if(AdjustType.Hs.equals(getAdjustType(currentOp))) currentOp = null;
	}
	
	public void resetRgb() {
		setRed(0);
		setGreen(0);
		setBlue(0);		
		if(AdjustType.Rgb.equals(getAdjustType(lastOp))) lastOp = null;
		if(AdjustType.Rgb.equals(getAdjustType(currentOp))) currentOp = null;
	}

	/**
	 * Sets current filter operation since this is a composite filter. By 
	 * default, this filter performs contrast operation since that's the base 
	 * it extends. Consequently, setting operation to null indicates that 
	 * current op is contrast. This setting is used mostly for informational 
	 * purposes, since filter always executes all composite operations. This 
	 * setting simply indicates parameter that was most recently set, which in 
	 * a way could be thought as "current operation".
	 * 
	 * @param currentOp
	 */
	public void setCurrentOp(ImageFilterOps currentOp) {
		lastOp = this.currentOp;
		this.currentOp = currentOp;
	}

	@Override
	public ImageFilterOps getMetaData() {
		if(currentOp == null)
			return ImageFilterOps.Contrast;
		else
			return currentOp;
	}
	
	@Override
	public String toString() {
		return getMetaData().toString();
	}

	public AdjustType getLastAdjust() {
		return ColorAdjustFilter.getAdjustType(lastOp);
	}
	
	/**
	 * @param op
	 * @return adjust type given operation; null if operation does not match
	 */
	public static AdjustType getAdjustType(ImageFilterOps op) {
		if(op == null) return null;
		
		switch(op) {
		case Hue:
		case Saturation:
			return AdjustType.Hs;
		case Red:
		case Green:
		case Blue:
			return AdjustType.Rgb;
		default:
			return null;
		}
	}
	
	/**
	 * Debug purpose method to log current parameters. Needed since 
	 * {@link #toString()} is already purposed.
	 */
	public void printValues() {
		if(log.isDebugEnabled()) {
			log.debug(String.format(
					"r: %.1f, g: %.1f, b: %.1f, h: %.1f, s: %.1f, c: %.1f, b: %.1f", 
					rgbFilter.getRFactor(), 
					rgbFilter.getGFactor(), 
					rgbFilter.getBFactor(), 
					hsbFilter.getHFactor(),
					hsbFilter.getSFactor(),
					getContrast(),
					getBrightness()
				)
			);
		}
	}
}