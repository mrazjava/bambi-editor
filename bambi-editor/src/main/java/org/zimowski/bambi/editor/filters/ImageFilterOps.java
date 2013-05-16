package org.zimowski.bambi.editor.filters;

/**
 * Available image filter operations.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public enum ImageFilterOps {

	Cartoonize, 
	Daemonize, 
	Chessboard, 
	Emboss, 
	Mirror, 
	Grayscale, 
	Hue,
	Saturation,
	Contrast,
	Brightness,
	Negative, 
	Kaleidoscope, 
	Marble, 
	Red("Red Channel Adjust"),
	Green("Green Channel Adjust"),
	Blue("Blue Channel Adjust"),
	OldPhoto, 
	Posterize, 
	Solarize, 
	Stamp, 
	Twirl, 
	RotateLeft("Rotate left 90 degrees"), 
	RotateRight("Rotate right 90 degrees"),
	Refresh("Reload original image"),
	Scale;
	
	private String description = null;
	
	private ImageFilterOps() {
	}
	
	private ImageFilterOps(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		if(description == null)
			return super.toString();
		else
			return description;
	}
}