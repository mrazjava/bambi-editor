package org.zimowski.bambi.controls.resources.dialog.print;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.zimowski.bambi.controls.resources.dialog.image.ImageDialogIcon;


public enum PrintDialogIcon {

	Crop("crop16x16.png"),
	Print("print16x16.png");
	
	private static final String RESOURCE_PATH = "/" +  
			PrintDialogIcon.class.getPackage().getName().replace('.', '/') + "/";

	private String fileName;

	private PrintDialogIcon(String fileName) {
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
