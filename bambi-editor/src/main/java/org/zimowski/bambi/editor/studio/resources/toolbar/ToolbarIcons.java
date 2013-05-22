package org.zimowski.bambi.editor.studio.resources.toolbar;

import java.net.URL;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.config.Configuration;
import org.zimowski.bambi.editor.filters.ImageFilterOps;
import org.zimowski.bambi.editor.studio.cam.CamFilterOps;
import org.zimowski.bambi.editor.studio.image.EditorImageUtil;

/**
 * @author Adam Zimowski (mrazjava)
 */
public enum ToolbarIcons {
	
	Cartoonize("cartoonize24x24.png", ImageFilterOps.Cartoonize.toString()),
	Chessboard("checkboard24x24.png", ImageFilterOps.Chessboard.toString()),
	Deamonize("deamonize24x24.png", ImageFilterOps.Daemonize.toString()),
	Emboss("emboss24x24.png", ImageFilterOps.Emboss.toString()),
	Mirror("flip24x24.png", ImageFilterOps.Mirror.toString()),
	MirrorRed("mirror_red24x24.png", "Mirror Red Pixels"),
	MirrorGreen("mirror_green24x24.png", "Mirror Green Pixels"),
	MirrorBlue("mirror_blue24x24.png", "Mirror Blue Pixels"),
	Grayscale("gray24x24.png", ImageFilterOps.Grayscale.toString()),
	Help("help24x24.png", "Help"),
	Kaleidoscope("kaleidoscope24x24.png", ImageFilterOps.Kaleidoscope.toString()),
	Marble("marble-gold24x24.png", ImageFilterOps.Marble.toString()),
	Metrics("ruler24x24.png", "Ruler"),
	Posterize("monalisa24x24.png", ImageFilterOps.Posterize.toString()),
	Negative("negfilm24x24.png", ImageFilterOps.Negative.toString()),
	Refresh("refresh24x24.png", "Refresh"),
	RotateLeft("rotateleft24x24.png", ImageFilterOps.RotateLeft.toString()),
	RotateRight("rotateright24x24.png", ImageFilterOps.RotateRight.toString()),
	OldPhoto("sepia24x24.png", ImageFilterOps.OldPhoto.toString()),
	Stamp("stamp24x24.png", ImageFilterOps.Stamp.toString()),
	Solarize("sun24x24.png", ImageFilterOps.Solarize.toString()),
	Twirl("twirl24x24.png", ImageFilterOps.Twirl.toString()),
	Scale("scale24x24.png", "Change Size"),
	Red("red24x24.png", ImageFilterOps.Red.toString()),
	Green("green24x24.png", ImageFilterOps.Green.toString()),
	Blue("blue24x24.png", ImageFilterOps.Blue.toString()),
	Hue("hue24x24.png", ImageFilterOps.Hue.toString()),
	Saturation("saturation24x24.png", ImageFilterOps.Saturation.toString()),
	Brightness("brightness24x24.png", ImageFilterOps.Brightness.toString()),
	Contrast("contrast24x24.png", ImageFilterOps.Contrast.toString()),
	Rgb("rgb24x24.png", CamFilterOps.Rgb.toString()),
	Stream("stream24x24.png", CamFilterOps.None.toString()),
	Webcam("webcam24x24.png", "Webcam"),
	Camera("camera32x32.png", "Take Picture"),
	Export("upload24x24.png", "Export Image"),
	Cancel("cancel24x24.png", "Abort"),
	Open("open24x24.png", "Open Image"),
	Print("print24x24.png", "Print your creation!"),
	Separator("vseparator8x25.png", "Sepie");
	
	private static final Logger log = LoggerFactory.getLogger(ToolbarIcons.class);
	
	private String fileName = null;
	
	private String description = null;
	
	private String metaInfo;
	
	/**
	 * Load once and cache
	 */
	private ImageIcon icon = null;
	
	private ToolbarIcons(String fileName, String description) {
		this.fileName = fileName;
		this.description = description;
	}
	
	public ImageIcon getIcon() {
    	return getImageIcon();
	}
	
	private ImageIcon getImageIcon() {
		if(icon == null) {
			final String path = Configuration.RESOURCE_PATH + "toolbar/" + fileName;
	    	URL iconUrl = EditorImageUtil.class.getResource(path);
	    	icon = new ImageIcon(iconUrl);
		}
    	return icon;
	}
	
	public String getDescription() {
		return description;
	}
	
	/**
	 * Given filter operation look up associated toolbar icon.
	 * 
	 * @param op filter op for which toolbar icon should be returned
	 * @return toolbar icon if available, null otherwise
	 */
	public static ToolbarIcons fromFilter(ImageFilterOps op) {
		switch(op) {
		case Cartoonize:	return ToolbarIcons.Cartoonize;
		case Chessboard:	return ToolbarIcons.Chessboard;
		case Daemonize:		return ToolbarIcons.Deamonize;
		case Emboss:		return ToolbarIcons.Emboss;
		case Grayscale:		return ToolbarIcons.Grayscale;
		case Kaleidoscope:	return ToolbarIcons.Kaleidoscope;
		case Marble:		return ToolbarIcons.Marble;
		case Mirror:		return ToolbarIcons.Mirror;
		case Negative:		return ToolbarIcons.Negative;
		case OldPhoto:		return ToolbarIcons.OldPhoto;
		case Posterize:		return ToolbarIcons.Posterize;
		case RotateLeft:	return ToolbarIcons.RotateLeft;
		case RotateRight:	return ToolbarIcons.RotateRight;
		case Solarize:		return ToolbarIcons.Solarize;
		case Stamp:			return ToolbarIcons.Stamp;
		case Twirl:			return ToolbarIcons.Twirl;
		case Refresh:		return ToolbarIcons.Refresh;
		case Scale:			return ToolbarIcons.Scale;
		case Red:			return ToolbarIcons.Red;
		case Green:			return ToolbarIcons.Green;
		case Blue:			return ToolbarIcons.Blue;
		case Hue:			return ToolbarIcons.Hue;
		case Saturation:	return ToolbarIcons.Saturation;
		case Brightness:	return ToolbarIcons.Brightness;
		case Contrast:		return ToolbarIcons.Contrast;
			
		default:
			log.error("no icon conversion available for {}", op.toString());
			return null;
		}
	}

	public String getMetaInfo() {
		return metaInfo;
	}

	public void setMetaInfo(String metaInfo) {
		this.metaInfo = metaInfo;
	}
}