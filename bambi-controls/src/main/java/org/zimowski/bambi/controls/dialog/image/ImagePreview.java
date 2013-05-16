package org.zimowski.bambi.controls.dialog.image;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.commons.ImageUtil;
import org.zimowski.bambi.controls.resources.dialog.image.ImageDialogIcon;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class ImagePreview extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(ImagePreview.class);
	
	private static final String CARD_EMPTY = "E";
	
	private static final String CARD_DIR = "D";
	
	private static final String CARD_LOADING = "L";
	
	private static final String CARD_PREVIEW = "P";

	private CardLayout cardLayout;
	
	private JPanel previewPanel;
	
	private LoadingPreview loadingPreview;
	
	private DirectoryPreview directoryPreview;
	
	private ThumbPreview thumbPreview;
	
	public ImagePreview() {
		cardLayout = new CardLayout();

		loadingPreview = new LoadingPreview();
		directoryPreview = new DirectoryPreview();
		thumbPreview = new ThumbPreview();
		thumbPreview.setPreferredSize(new Dimension(160, 0));
		previewPanel = new JPanel(cardLayout);
		previewPanel.add(new EmptyPreview(), CARD_EMPTY);
		previewPanel.add(directoryPreview, CARD_DIR);
		previewPanel.add(loadingPreview, CARD_LOADING);
		previewPanel.add(thumbPreview, CARD_PREVIEW);

		setLayout(new BorderLayout());
		JLabel previewHeader = new PreviewHeader("Preview");
		previewHeader.setVerticalAlignment(JLabel.TOP);
		previewHeader.setHorizontalAlignment(JLabel.CENTER);
		previewHeader.setPreferredSize(new Dimension(0, 20));
		add(previewHeader, BorderLayout.NORTH);
		add(previewPanel, BorderLayout.CENTER);
		cardLayout.show(previewPanel, CARD_EMPTY);
		setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 10, 0, 0), new EtchedBorder()));
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {

		String prop = e.getPropertyName();

		if(JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
			// user entered directory
			cardLayout.show(previewPanel, CARD_EMPTY);
		}
		else if(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
			// user selected either file or directory
			ImageChooser chooser = (ImageChooser)e.getSource();
			File file = (File)e.getNewValue();
			if(file == null) {
				// should not be the case though in some tests on underpowered 
				// winXP machine it crapped out right here; TODO: investigate
				return;
			}
			String name;
			pngCount = new int[2];
			jpgCount = new int[2];
			othCount = new int[2];
			dirCount = new int[2];
			if(file.isDirectory()) {
				File[] files = file.listFiles();
				for(File f : files) {
					if(f.isDirectory())
						analyzeDirectory(f, -1);
					else
						analyzeFile(f, 0);
				}
				name = CARD_DIR;
				directoryPreview.pngCount = pngCount[0];
				directoryPreview.jpgCount = jpgCount[0];
				directoryPreview.otherCount = othCount[0];
				directoryPreview.subDirectoryCount = dirCount[0];
				directoryPreview.repaint();
			}
			else {
				// user selected an image!
				name = CARD_LOADING;
				loadingPreview.fileSizeLabel.setText(getFileSize(file));
				loadThumb(file);
			}
			cardLayout.show(previewPanel, name);
			chooser.setEnabledApproveButton(!file.isDirectory());
		}
	}
	
	/**
	 * Computes and formats for display size of the file.
	 * 
	 * @param file file for which size should be computed
	 * @return formatted string denoting the size of the file
	 */
	private String getFileSize(File file) {
		int bytes = 0;
		try {
			URL url = file.toURI().toURL();
			bytes = url.openStream().available();
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return FileUtils.byteCountToDisplaySize(new Long(bytes));
	}
	
	/**
	 * Generates a thumb preview from reading source off a filesystem. Work 
	 * is done off the EDT.
	 * 
	 * @param file
	 */
	private void loadThumb(final File file) {
		thumbPreview.setThumb(null);
		new SwingWorker<Void, Integer>() {
			final int THUMB_SIZE = 120; // pixels
			BufferedImage thumb = null;
			@Override
			protected Void doInBackground() throws Exception {
				BufferedImage tmpImg = null;
				try {
					tmpImg = ImageIO.read(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (tmpImg != null) {
					if (tmpImg.getWidth() > THUMB_SIZE
							|| tmpImg.getHeight() > THUMB_SIZE) {
						thumb = Scalr.resize(tmpImg, THUMB_SIZE);
					} else { // no need to miniaturize
						thumb = tmpImg;
					}
					thumbPreview.source = tmpImg;
					thumbPreview.sourceFile = file;
				}
				return null;
			}
			@Override
			protected void done() {
				cardLayout.show(previewPanel, CARD_PREVIEW);
				thumbPreview.setThumb(this.thumb);
			}
		}.execute();
	}
	
	private int[] dirCount;
	private int[] pngCount;
	private int[] jpgCount;
	private int[] othCount;

	
	/**
	 * Recursively analyzes directory structure counting number of directories 
	 * and image files inside the top level directory. The second level 
	 * argument is to track total counts when traversing the directory 
	 * structure. Level can be negative to indicate not to traverse 
	 * recursively.
	 * 
	 * @param dir directory to be analyzed; when level = 0 this is root
	 * @param level recursion depth level; 0 is top level
	 * @see ImagePreview#analyzeFile(File, int)
	 */
	private void analyzeDirectory(File dir, int level) {
		if(level <= 0) dirCount[0]++;
		if(level < 0) return;
		dirCount[1]++;
		File[] files = dir.listFiles();
		for(File file : files) {
			if(file.isDirectory())
				analyzeDirectory(file, ++level);
			else
				analyzeFile(file, ++level);
		}
	}
	
	/**
	 * Determines image type given file parameter. The second level argument 
	 * is to track total file count when recursively traversing directory 
	 * structure.
	 * 
	 * @param file current file to be analyzed
	 * @param level recursion depth level; 0 is top level
	 * @see ImagePreview#analyzeDirectory(File, int)
	 */
	private void analyzeFile(File file, int level) {
		String ext = FilenameUtils.getExtension(file.getName());
		if(ImageChooser.EXT_PNG.equals(ext)) {
			if(level == 0) pngCount[0]++;
			pngCount[1]++;
		}
		else if(ImageChooser.EXT_JPG.equals(ext) || 
				ImageChooser.EXT_JPEG.equals(ext)) {
			if(level == 0) jpgCount[0]++;
			jpgCount[1]++;
		}
		else {
			if(level == 0) othCount[0]++;
			othCount[1]++;
		}
	}
	
	/**
	 * Header for the preview panel which simply displays the label and a 
	 * separator.
	 * 
	 * @author Adam Zimowski (mrazjava)
	 */
	class PreviewHeader extends JLabel {

		private static final long serialVersionUID = -6114470400100543163L;

		public PreviewHeader() {
			super();
		}

		public PreviewHeader(String text) {
			super(text);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.GRAY);
			g.drawLine(15, getHeight()-3, getWidth()-15, getHeight()-3);
		}
		
	}
	
	/**
	 * Preview responsible for rendering animated loading gif while large 
	 * image is prepared for thumb display. This preview can also show some 
	 * quick to obtain file info such as its size.
	 * 
	 * @author Adam Zimowski (mrazjava)
	 */
	class LoadingPreview extends JPanel {

		private static final long serialVersionUID = -777996446876196460L;
		
		private JLabel loadingLabel;
		
		private JLabel fileSizeLabel;

		public LoadingPreview() {
			setLayout(new BorderLayout());
			loadingLabel = new JLabel(ImageDialogIcon.Loading.getIcon(), JLabel.CENTER);
			fileSizeLabel = new JLabel();
			add(loadingLabel, BorderLayout.CENTER);
			add(fileSizeLabel, BorderLayout.SOUTH);
		}
	}
	
	/**
	 * Preview associated with selected directory. It displays some basic 
	 * content info, such as number of images and subdirectories. It does not 
	 * traverse subdirectories.
	 * 
	 * @author Adam Zimowski (mrazjava)
	 */
	class DirectoryPreview extends JComponent {

		private static final long serialVersionUID = 7650421462608420907L;
		

		private int pngCount;
		
		private int jpgCount;
		
		private int subDirectoryCount;
		
		private int otherCount;
		
		@Override
		protected void paintComponent(Graphics g) {
			String pngStat = "PNG: " + pngCount;
			int y = 20;
			int x = 5;
			g.drawString(pngStat, x, y);
			String jpgStat = "JPG: " + jpgCount;
			FontMetrics fm = getFontMetrics(getFont());
			Rectangle2D fontRect = fm.getStringBounds(pngStat, g);
			y += fontRect.getHeight() + 5;
			g.drawString(jpgStat, x, y);
			String otherStat = "Other Files: " + otherCount;
			fontRect = fm.getStringBounds(jpgStat, g);
			y += fontRect.getHeight() + 5;
			g.drawString(otherStat, x, y);
			fontRect = fm.getStringBounds(otherStat, g);
			y += fontRect.getHeight() + 5;
			String dirStat = "Dir: " + subDirectoryCount;
			g.drawString(dirStat, x, y);
			g.dispose();
		}
	}
	
	private static BufferedImage camIcon = null;
			
	static {
		try {
			camIcon = ImageIO.read(ImageDialogIcon.Cam.getResource());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	/**
	 * Displays thumbnail associated with selected image.
	 * 
	 * @author Adam Zimowski (mrazjava)
	 */
	class ThumbPreview extends JComponent {

		private static final long serialVersionUID = 4179328578179193059L;
		
		/**
		 * already scaled thumb of the source
		 */
		private BufferedImage thumb;
		
		/**
		 * original image source
		 */
		private BufferedImage source;
		
		/**
		 * file denoting the source
		 */
		private File sourceFile;
		
		private boolean rotated = false;
		
		private final DateFormat df = DateFormat.getDateInstance();

		@Override
		protected void paintComponent(Graphics g) {
			if(thumb == null) return;
			String dateTaken = null;
			int orientation = 0;
			String model = null;
			Font font = new Font("SansSerif", Font.PLAIN, 11);
			g.setFont(font);
			try {
				Metadata meta = ImageMetadataReader.readMetadata(sourceFile);
				Directory directory = meta
						.getDirectory(ExifIFD0Directory.class);
				if (directory != null) {
					if(directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
						orientation = directory
								.getInt(ExifIFD0Directory.TAG_ORIENTATION);
					}
					if(directory.containsTag(ExifIFD0Directory.TAG_MODEL)) {
						model = directory.getString(ExifIFD0Directory.TAG_MODEL);
					}
					if(directory.containsTag(ExifIFD0Directory.TAG_DATETIME)) {
						Date date = directory
								.getDate(ExifIFD0Directory.TAG_DATETIME);
						dateTaken = df.format(date);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			int y = 10;
			if(!rotated && orientation > 0) {
				// we have exif info on orientation
				try {
					AffineTransform tfm = ImageUtil.getExifTransformation(orientation, thumb.getWidth(), thumb.getHeight());
					thumb = ImageUtil.transformImage(thumb, tfm);
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
			g.drawImage(thumb, getWidth() / 2 - thumb.getWidth() / 2, y, null);

			String res = source.getWidth() + " x " + source.getHeight() + 
					", " + getFileSize(sourceFile);
			int x = 5;
			y += thumb.getHeight() + 20;
			g.drawString(res, x, y);
			FontMetrics fm = getFontMetrics(getFont());
			Rectangle2D fontRect = fm.getStringBounds(res, g);
			if (dateTaken != null || model != null) {
				y += fontRect.getHeight();
				g.setColor(Color.GRAY);
				int x2 = getWidth()-x-(camIcon.getWidth()*2);
				g.drawLine(x, y+(camIcon.getHeight()/2), x2, y+(camIcon.getHeight()/2));
				g.setColor(Color.BLACK);
				g.drawImage(camIcon, x2 + ((getWidth()-x2)/2 - camIcon.getWidth()/2), y, null);
				y += camIcon.getHeight() + 10;
				if (dateTaken != null) {
					g.drawString(dateTaken, x, y);
					fontRect = fm.getStringBounds(dateTaken, g);
				}
				if (model != null) {
					y += fontRect.getHeight() + fontRect.getHeight()*.25;
					g.drawString(model, x, y);
					fontRect = fm.getStringBounds(model, g);
				}
			}
			g.dispose();
		}

		public void setThumb(BufferedImage thumb) {
			this.thumb = thumb;
			rotated = false;
		}

	}
	
	/**
	 * Dummy preview which never displays anything.
	 * 
	 * @author Adam Zimowski (mrazjava)
	 */
	class EmptyPreview extends JComponent {

		private static final long serialVersionUID = 2456306731712688092L;		
	}
}