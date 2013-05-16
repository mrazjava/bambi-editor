package org.zimowski.bambi.editor.customui.slider;

import java.awt.Color;
import java.awt.Image;

import javax.imageio.ImageIO;

/**
 * @author Adam Zimowski (mrazjava)
 */
public enum SliderDecor {

	Red(Color.RED, "red8x8.png", "red12x12.png"), 
	Green(Color.decode("0x228b22"), "green8x8.png", "green12x12.png"), 
	Blue(Color.BLUE, "blue8x8.png", "blue12x12.png"), 
	Hue("hue8x8.png", "hue12x12.png"), 
	Saturation("saturation8x8.png", "saturation12x12.png"), 
	Brigtness("brightness12x12.png", "brightness15x15.png"),
	Contrast("contrast8x8.png", "contrast12x12.png"),
	Thumb(null, "bambiface16x16.png");
	
	private Color color;
	
	private Image minIcon;
	
	private Image maxIcon;


	private SliderDecor() {
		this(Color.DARK_GRAY);
	}
	
	private SliderDecor(Color color) {
		this.color = color;
	}
	
	private SliderDecor(String minIcon, String maxIcon) {
		this(Color.DARK_GRAY, minIcon, maxIcon);
	}
	
	private SliderDecor(Color color, String minIcon, String maxIcon) {
		this(color);
		try { this.minIcon = ImageIO.read(getClass().getResource(minIcon)); }
		catch(Exception e) {};
		try { this.maxIcon = ImageIO.read(getClass().getResource(maxIcon)); }
		catch(Exception e) {};
	}
	
	public Image getMaxIcon() {
		return maxIcon;
	}
	
	public Image getMinIcon() {
		return minIcon;
	}
	
	public Color getColor() {
		return color;
	}
}
