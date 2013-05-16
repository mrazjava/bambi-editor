package org.zimowski.bambi.controls.resources.dialog.login;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.zimowski.bambi.controls.resources.dialog.image.ImageDialogIcon;


/**
 * @author Adam Zimowski (mrazjava)
 */
public enum LoginDialogIcon {

	Password("password1.png");
	
	private static final String RESOURCE_PATH = "/" +  
			LoginDialogIcon.class.getPackage().getName().replace('.', '/') + "/";

	private String fileName;

	private LoginDialogIcon(String fileName) {
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