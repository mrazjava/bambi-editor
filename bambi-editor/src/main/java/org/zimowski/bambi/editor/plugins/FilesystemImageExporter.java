package org.zimowski.bambi.editor.plugins;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.ExtensionFileFilter;
import org.zimowski.bambi.editor.config.ImageOutputFormat;
import org.zimowski.bambi.editor.plugins.api.AbstractImageExporter;
import org.zimowski.bambi.editor.plugins.api.ExportStateMonitor;

/**
 * A plugin that will export current image to end user's local filesystem,
 * essentially providing for "image save" feature.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class FilesystemImageExporter extends AbstractImageExporter {

	private static final Logger log = LoggerFactory
			.getLogger(FilesystemImageExporter.class);

	@Override
	protected Void doInBackground() throws Exception {

		List<ExportStateMonitor> stateMonitors = exportDef.getStateMonitors();
		final Date startTime = new Date();
		for (ExportStateMonitor l : stateMonitors) {
			l.exportStarted(startTime);
		}

		JFileChooser fileDialog = new JFileChooser();
		ImageOutputFormat format = exportDef.getFormat();
		try {
			boolean png = ImageOutputFormat.png.equals(format);
			String extLbl = (png ? "png" : "jpg, jpeg");
			String[] extArray = png ? new String[] { "PNG" } : new String[] {
					"JPG", "JPEG" };

			FileFilter imageFilter = new ExtensionFileFilter("Picture Files ("
					+ extLbl + ")", extArray);
			fileDialog.setFileFilter(imageFilter);

			int selection = fileDialog.showSaveDialog(parent);
			if (selection == JFileChooser.APPROVE_OPTION) {
				File outputFile = fileDialog.getSelectedFile();
				InputStream in = new ByteArrayInputStream(
						exportDef.getImageBytes());
				BufferedImage image = ImageIO.read(in);
				ImageIO.write(image, format.toString(), outputFile);
				
				for (ExportStateMonitor l : stateMonitors) {
					l.exportSuccess(outputFile.length());
				}
				log.info("image saved to {} ({} bytes)", 
						outputFile.getAbsolutePath(), 
						outputFile.length());
			}
			
		} catch (IOException e) {
			log.error(e.getMessage());
			for (ExportStateMonitor m : stateMonitors) {
				m.exportError(e);
			}

		}
		return null;
	}
}