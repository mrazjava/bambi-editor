package org.zimowski.bambi.controls.resources.dialog.image;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * @author Adam Zimowski (mrazjava)
 */
public enum ImageDialogIcon {

	Cam("cam21x16.png"),
	Gif("gif32x32.png"),
	Jpg("jpeg32x32.png"),
	Png("png32x32.png"),
	Loading("loading.png"),	// actually GIF but carrying PNG extension (bug)
	OpenGrey("opengrey24x24.png"),
	OpenPic("openpic24x24.png"),
	OpenQuestion("openquestion24x24.png");
	
	private static final String RESOURCE_PATH = "/" +  
			ImageDialogIcon.class.getPackage().getName().replace('.', '/') + "/";

	private String fileName;
	
	
	private ImageDialogIcon(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFullPath() {
		return (fileName.startsWith("/")) ? fileName : RESOURCE_PATH + fileName;
	}
	
	public URL getResource() {
		String path = getFullPath();
		return ImageDialogIcon.class.getResource(path);
	}
	
	public Icon getIcon() {
		return new ImageIcon(getResource());
	}
}