package org.zimowski.bambi.controls.dialog.image;

import java.io.File;

import javax.swing.Icon;
import javax.swing.filechooser.FileView;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.controls.resources.dialog.image.ImageDialogIcon;

/**
 * @author Oracle Corp (FileChooserDemo2)
 * @author Adam Zimowski (mrazjava)
 */
public class ImageFileView extends FileView {

	private static final Logger log = LoggerFactory.getLogger(ImageFileView.class);
	
	private Icon jpgIcon;
	private Icon pngIcon;
	private Icon dirIconGrey;
	private Icon dirIconPic;
	private Icon dirIconQues;
	
	public ImageFileView() {
		jpgIcon = ImageDialogIcon.Jpg.getIcon(); //ResourceLoader.getIcon("dialog/image/jpeg32x32.png");
		pngIcon = ImageDialogIcon.Png.getIcon(); //ResourceLoader.getIcon("dialog/image/png32x32.png");
		dirIconGrey = ImageDialogIcon.OpenGrey.getIcon(); // ResourceLoader.getIcon("dialog/image/opengrey24x24.png");
		dirIconPic = ImageDialogIcon.OpenPic.getIcon(); //ResourceLoader.getIcon("dialog/image/openpic24x24.png");
		dirIconQues = ImageDialogIcon.OpenQuestion.getIcon(); //ResourceLoader.getIcon("dialog/image/openquestion24x24.png");
	}
	
	@Override
	public String getName(File f) {
		return null; // let the L&F FileView figure this out
	}

	@Override
	public String getDescription(File f) {
		return null; // let the L&F FileView figure this out
	}

	@Override
	public Boolean isTraversable(File f) {
		return null; // let the L&F FileView figure this out
		//return f.isDirectory();
	}

	@Override
	public String getTypeDescription(File f) {
		String extension = FilenameUtils.getExtension(f.getName()); //ImageFilter.getExtension(f);
		String type = null;

		if (extension != null) {
			if (extension.equals(ImageChooser.EXT_JPEG)
					|| extension.equals(ImageChooser.EXT_JPG)) {
				type = "JPEG Image";
			} else if (extension.equals(ImageChooser.EXT_PNG)) {
				type = "PNG Image";
			}
		}
		return type;
	}

	@Override
	public Icon getIcon(File f) {
        
		String extension = FilenameUtils.getExtension(f.getName());
		if(extension == null) {
			// should not happen since incoming file should have already been 
			// filtered
			log.warn("null extension");
			return null;
		}
		extension = extension.toLowerCase();
        Icon icon = null;

       	if(ImageChooser.EXT_JPEG.equals(extension) || ImageChooser.EXT_JPG.equals(extension)) {
       		icon = jpgIcon;
       	}
       	else if(ImageChooser.EXT_PNG.equals(extension)) {
       		icon = pngIcon;
       	}
       	else if(f.isDirectory()) {
       		File[] files = f.listFiles();
       		boolean hasPics = false;
       		boolean hasDirs = false;
       		for(File file : files) {
       			if(!hasDirs && file.isDirectory()) {
       				hasDirs = true;
       			}
       			else if(!hasPics && isImage(file)) {
       				hasPics = true;
       			}
       			if(hasDirs && hasPics) break;
       		}
       		if(hasPics)
       			icon = dirIconPic;
       		else if(hasDirs)
       			icon = dirIconQues;
       		else
       			icon = dirIconGrey;
       	}

		return icon;
	}
	
	private boolean isImage(File file) {
		String extension = FilenameUtils.getExtension(file.getName());
		if(extension == null) return false;
		extension = extension.toLowerCase();
		return ImageChooser.EXT_JPEG.equals(extension) || 
				ImageChooser.EXT_JPG.equals(extension) || 
				ImageChooser.EXT_PNG.equals(extension);
	}
}