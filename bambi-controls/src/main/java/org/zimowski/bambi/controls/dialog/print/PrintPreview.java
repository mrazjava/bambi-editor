package org.zimowski.bambi.controls.dialog.print;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.swing.JComponent;

import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.commons.ImageUtil;
import org.zimowski.bambi.commons.resources.cursors.CustomCursors;
import org.zimowski.bambi.controls.dialog.print.PrintDialog.Orientation;

/**
 * A print preview computing engine. This component is capable of a WYSIWYG 
 * print preview graphical presentation with underlying philosophy of being 
 * simple to use, yet powerful. For example, rather than asking users for 
 * cumbersome margin parameters it intuitively allows the user to re-scale and 
 * re-position the image which ultimately accomplishes the desired margin 
 * effect. It tries to faithfully and accurately reproduce printer hardware 
 * margin limitations as well as ultimately print output as closely to the 
 * desired preview as possible. 
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class PrintPreview extends JComponent implements MouseMotionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(PrintPreview.class);

	private final int JAVA_DPI_BASE = 72;
	
	private BufferedImage previewImage;
	
	private BufferedImage scaledPreview;
	
	/**
	 * virtual paper preview width, in pixels, but scaled to letter 
	 * ratio
	 */
	private int previewPaperWidth;
	
	/**
	 * virtual paper preview height, in pixels, but scaled to letter 
	 * ratio
	 */
	private int previewPaperHeight;
	
	/**
	 * X co-ordinate of the left upper corner of scaled image as shown on 
	 * preview paper. This value is computed based on displacement.
	 */
	private int imgX;
	
	/**
	 * Y co-ordinate of the left upper corner of scaled image as shown on 
	 * preview paper. This value is computed based on displacement.
	 */
	private int imgY;
	
	/**
	 * X co-ordinate of a mouse click which began the drag process. This value
	 * is reset to zero at the end of the drag.
	 */
	private int dragStartX;
	
	/**
	 * Y co-ordinate of a mouse click which began the drag process. This value 
	 * is reset to zero at the end of the drag.
	 */
	private int dragStartY;
	
	/**
	 * Used for shading effects during dragging; caching member for performance 
	 * rather than obtaining each time on paint.
	 */
	private AlphaComposite composite;

	/**
	 * displacement, in pixels, in the x direction as a result of moving the 
	 * image
	 */
	private int xDisplacement;
	
	/**
	 * displacement, in pixels, in the y direction as a result of moving the 
	 * image
	 */
	private int yDisplacement;
	
	private PreviewInfo previewInfo;
	
    int canvasWidth, canvasHeight;
    
	/**
	 * X co-ordinate of paper's left upper corner relative to canvas. 
	 */
	private int paperX;
	
	/**
	 * Y co-ordinate of paper's left upper corner relative to canvas. 
	 */
	private int paperY;

	private Orientation orientation = Orientation.Portrait;
	
	private MediaSizeName mediaFormat = MediaSizeName.NA_LETTER;
	
	/**
	 * Printer to use for this preview. Used for hardware margins and other 
	 * printer specific artifacts.
	 */
	private PrintService selectedPrinter;
	
	/**
	 * Simple struct to hold translated printer co-ordinates of available 
	 * (imageable) print area, fit for currently displayed preview paper. 
	 * These specs already account for the fact that printers always return 
	 * theirs specs in portrait format, but this preview component always 
	 * displays preview paper in landscape format. 
	 * 
	 * @see PrintPreview#getImageableSpecs()
	 * @author Adam Zimowski (mrazjava)
	 */
	static class ImageableSpecs {
		int x;
		int y;
		int width;
		int height;
	}
	
	/**
	 * Final print configuration end user chose to use for printing the image.
	 * 
	 * @author Adam Zimowski (mrazjava)
	 */
	static class PrintOutputConfig {
		double xDisplacementPct;
		double yDisplacementPct;
		double renderScalePct;
		BufferedImage image;
		PrintableSpecs printerSpecs;
		MediaSizeName mediaFormat;
	}
	
    
	PrintPreview(PreviewInfo previewInfo) {
		this.previewInfo = previewInfo;
		int type = AlphaComposite.SRC_OVER; 
		composite = AlphaComposite.getInstance(type, 0.3f);
		addMouseMotionListener(this);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				dragStartX = e.getX();
				dragStartY = e.getY();
				if(isOverImage(dragStartX, dragStartY)) {
					paintDrag = true;
					setCursor(CustomCursors.Grabbed.getCursor());
				}
				else if(isOverCropTool(dragStartX, dragStartY)) {
					cropDrag = true;
					cropXDragDelta = cropYDragDelta = 0;
					setCursor(CustomCursors.Drag.getCursor());
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				Cursor cursor = null;
				if(paintDrag) {
					//log.debug("imgX: {}, imgY: {}", imgX, imgY);
					//log.debug("xDisplacement: {}, yDisplacement: {}", xDisplacement, yDisplacement);
					imgX += xDisplacement;
					imgY += yDisplacement;
					xDisplacement = yDisplacement = 0;
					if(isOverImage(e.getX(), e.getY())) {
						cursor = CustomCursors.Grab.getCursor();
					}
					paintDrag = false;
				}
				else if(cropDrag) {
					if(isOverCropTool(e.getX(), e.getY())) {
						cursor = CustomCursors.Grab.getCursor();
					}
					cropX += cropXDragDelta;
					cropY += cropYDragDelta;
					cropDrag = false;
				}
				if(cursor == null) {
					cursor = Cursor.getDefaultCursor();
				}
				setCursor(cursor);
				repaint();
			}
		});
	}
	
	public void setPreviewImage(BufferedImage previewImage) {
		this.previewImage = previewImage;
	}

	private boolean isPortrait() {
		boolean orient = Orientation.Portrait.equals(orientation);
		return orient;
	}
	
	private void positionImage() {
		if(scaledPreview == null) return;
		imgX = leftMarginX+1;
		imgY = isPortrait() ? bottomMarginY - scaledPreview.getHeight() : topMarginY+1;
	}
	
	/**
	 * if set to true, preview media will be rendered with approximate size 
	 * differences relative one to another. This results in small media 
	 * being rendered very small, therefore it is recommended to keep this 
	 * setting turned off.
	 */
	boolean useSizeDiffRation = false;
	
	private void configurePreviewPaper() {
		canvasWidth = getWidth();
		canvasHeight = getHeight();
		log.trace("canvasHeight: {}", canvasHeight);
		log.trace("canvasHeight: {}", canvasHeight);

		double canvasRatio = new Double(canvasHeight) / new Double(canvasWidth);
		log.trace("CANVAS RATIO: {}, FORMAT RATIO: {}", canvasRatio, getFormatRatio());

		double multi = 0.85d;
		if(canvasRatio < getFormatRatio()) {
			multi = multi - (getFormatRatio() - canvasRatio);
		}
		double base = Math.max(14d, getMediaSizeWidth());
		if(base > 14d) base += 2d;
		// ratio between different media size relative to media width; 
		// we can use it if canvas is large enough to better relate the size 
		// between different media as selected
		double sizeDiffRatio = useSizeDiffRation ? getMediaSizeWidth() / base : 1d; //getMediaSizeHeight() / 8.5d;
		
		previewPaperWidth = (int)Math.round(canvasWidth * multi * sizeDiffRatio);
		previewPaperHeight = (int)Math.round(previewPaperWidth * getFormatRatio());
		
		paperX = (canvasWidth/2) - (previewPaperWidth/2);
		paperY = (canvasHeight/2) - (previewPaperHeight/2);
		//log.debug("previewPaperWidth: {}, previewPaperHeight: {}", previewPaperWidth, previewPaperHeight);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if(scaledPreview == null) return;

		normalizeCoordinates();
		
		g.setColor(Color.WHITE);
		g.fillRect(paperX, paperY, previewPaperWidth, previewPaperHeight);
		g.setColor(Color.DARK_GRAY);
		g.drawRect(paperX, paperY, previewPaperWidth, previewPaperHeight);
		g.setColor(Color.GRAY);
		g.fillRect(paperX+5, paperY+previewPaperHeight+1, previewPaperWidth, 5);
		g.fillRect(paperX+previewPaperWidth+1, paperY+5, 5, previewPaperHeight);

		drawHorizontalPaperDimension(paperX, paperY-10, g);
		drawVerticalPaperDimension(paperX + previewPaperWidth + 15, paperY, g);
		if(drawMargins) drawHardwareMargins(g.create(), false);

		Graphics2D g2 = (Graphics2D)g.create();

		if(paintDrag) g2.setComposite(composite);
		g2.drawImage(scaledPreview, imgX, imgY, null);
		if(paintDrag) {
			g.drawImage(scaledPreview, imgX + xDisplacement, imgY + yDisplacement, null);
		}
		
		if(showCroppingTool && beforeCropPreview == null) {
			g2.setColor(Color.BLUE);
			Rectangle crop = getCropRectangle();
			int dragXOffset = cropDrag ? cropXDragDelta : 0;
			int dragYOffset = cropDrag ? cropYDragDelta : 0;
			g2.drawRect(crop.x+dragXOffset, crop.y+dragYOffset, crop.width, crop.height);
		}

		g2.dispose();
		updateInfo();
		previewInfo.repaint();
	}
	
	private Rectangle getCropRectangle() {
		Rectangle r = new Rectangle();
		r.x = imgX + cropX;
		r.y = imgY + cropY;
		PrintableSpecs pSpecs = getPrintableSpecs();
		do {
			if(scaledPreview.getWidth() > scaledPreview.getHeight() && r.width < scaledPreview.getWidth()) {
				r.height = scaledPreview.getHeight();
				r.width = (int)Math.floor(r.height * ((pSpecs.height-pSpecs.y*2)/(pSpecs.width-pSpecs.x)));
			}
			else {
				r.width = scaledPreview.getWidth();
				r.height = (int)Math.floor(r.width * ((pSpecs.width-pSpecs.x)/(pSpecs.height-pSpecs.y*2)));
			}
		}
		while(r.width > scaledPreview.getWidth());
		
		return r;
	}
	
	void clearCrop() {
		if(beforeCropPreview != null) {
			beforeCropPreview.flush();
			beforeCropPreview = null;
		}
		if(croppedFullPreviewImage != null) {
			croppedFullPreviewImage.flush();
			croppedFullPreviewImage = null;
		}
	}
	
	/**
	 * X co-ordinate of the left upper corner cropping box; this co-ordinate 
	 * is relative to scaled image co-ordinates, representing displacement in 
	 * pixels along its x-axis. Left upper corner of the image relative to 
	 * this co-ordinate is a point cropX,cropY where cropX=0 and cropY=0 (0,0).
	 */
	private int cropX = 0;
	
	/**
	 * Y co-ordinate of the left upper corner cropping box; this co-ordinate 
	 * is relative to scaled image co-ordinates, representing displacement in 
	 * pixels along its y-axis. Left upper corner of the image relative to 
	 * this co-ordinate is a point cropX,cropY where cropX=0 and cropY=0 (0,0).
	 */
	private int cropY = 0;
	
	/**
	 * Calculates printer hardware margins scaled for display. Printers report 
	 * their specs based on portrait orientation, but we are always working in 
	 * landscape orientation, therefore relative to preview orientation printer 
	 * specs are relative to lower left corner which needs to be converted to 
	 * upper left of the landscape preview.
	 */
	private void computeHardwareMargins() {
		if(previewImage == null) return;
		ImageableSpecs specs = getImageableSpecs();
		leftMarginX = paperX + (previewPaperWidth - specs.height);
		bottomMarginY = paperY + specs.width;
		topMarginY = paperY + (previewPaperHeight - specs.width);
		rightMarginX = paperX + specs.height;
		//log.debug("paperX: {}, paperY: {}", paperX, paperY);
		//log.debug("previewPaperWidth: {}, previewPaperHeight: {}", previewPaperWidth, previewPaperHeight);
		//log.debug("leftMarginX: {}, rightMarginX: {}", leftMarginX, rightMarginX);
		//log.debug("topMarginY: {}, bottomMarginY: {}", bottomMarginY, topMarginY);
	}
	
	private void drawHardwareMargins(Graphics g, boolean colorify) {
		if(colorify) {
			g.setColor(Color.YELLOW);
			// top
			g.fillRect(paperX+1, paperY+1, previewPaperWidth-1, getTopMarginHeight()-1);
			// left
			g.fillRect(paperX+1, paperY+1, getLeftMarginWidth()-1, previewPaperHeight-1);
			// bottom
			g.fillRect(paperX+1, bottomMarginY, previewPaperWidth-1, getBottomMarginHeight());
			// right
			g.fillRect(rightMarginX, paperY+1, getRightMarginWidth(), previewPaperHeight-1);
		}
		else {
			g.setColor(Color.LIGHT_GRAY);
			// top
			g.drawLine(leftMarginX, topMarginY, rightMarginX, topMarginY);
			// left
			g.drawLine(leftMarginX, topMarginY, leftMarginX, bottomMarginY);
			// bottom
			g.drawLine(leftMarginX, bottomMarginY, rightMarginX, bottomMarginY);
			// right
			g.drawLine(rightMarginX, topMarginY, rightMarginX, bottomMarginY);
		}
		g.dispose();
	}
	
	static class PrintableSpecs {
		double x;
		double y;
		double width;
		double height;
	}
	
	private PrintableSpecs getPrintableSpecs() {
		MediaPrintableArea m = getMediaPrintableArea();
		PrintableSpecs specs = new PrintableSpecs();
		if(m == null) {
			log.warn("no {} found!", MediaPrintableArea.class.getSimpleName());
			return specs;
		}
		double ppi = new Double(getPpi());
		double x = m.getX(MediaPrintableArea.INCH) * ppi;
		double y = m.getY(MediaPrintableArea.INCH) * ppi;
		double w = m.getWidth(MediaPrintableArea.INCH) * ppi;
		double h = m.getHeight(MediaPrintableArea.INCH) * ppi;
		specs.x = x;
		specs.y = y;
		specs.width = w;
		specs.height = h;
		
		return specs;
	}
	
	/**
	 * Computes translated printer specs fit for currently displayed preview 
	 * paper.
	 * 
	 * @return
	 */
	private ImageableSpecs getImageableSpecs() {
		MediaPrintableArea m = getMediaPrintableArea();
		ImageableSpecs specs = new ImageableSpecs();
		if(m == null) {
			log.warn("no {} found!", MediaPrintableArea.class.getSimpleName());
			return specs;
		}
		double widthRatio = getWidthRatio();
		double heightRatio = getHeightRatio();
		double ppi = new Double(getPpi());
		//log.debug("heightRatio: {}, widthRatio: {}", heightRatio, widthRatio);
		specs.x = (int)Math.ceil((m.getX(MediaPrintableArea.INCH) * heightRatio) * ppi);
		specs.y = (int)Math.ceil((m.getY(MediaPrintableArea.INCH) * widthRatio) * ppi);
		specs.height = (int)Math.floor((m.getHeight(MediaPrintableArea.INCH) * widthRatio) * ppi);
		specs.width = (int)Math.floor((m.getWidth(MediaPrintableArea.INCH) * heightRatio) * ppi);
		//log.debug("SPECS | x: {}, y: {}, w: " + specs.width + ", h: " + specs.height, specs.x, specs.y);
		return specs;
	}
	
	private boolean showCroppingTool;
	
	/**
	 * @param visible true to show, false to hide
	 */
	void setShowCroppingTool(boolean visible) {
		this.showCroppingTool = visible;
	}
	
	private MediaSize getMediaSize() {
		MediaSize mediaSize = MediaSize.getMediaSizeForName(mediaFormat);
		return mediaSize;
	}
	
	private float getMediaSizeWidth() {
		MediaSize ms = getMediaSize();
		return ms.getY(MediaSize.INCH);
	}
	
	private float getMediaSizeHeight() {
		MediaSize ms = getMediaSize();
		return ms.getX(MediaSize.INCH);
	}
	
	private int leftMarginX;
	private int rightMarginX;
	private int topMarginY;
	private int bottomMarginY;
	
	private void drawHorizontalPaperDimension(int startX, int startY, Graphics g) {
		int lineX = startX;
		int lineY = startY;
		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(lineX, lineY, lineX+30, lineY);
		FontMetrics fm = g.getFontMetrics();
		String paperWidthText = getMediaSizeWidth() + "'";
		Rectangle2D strRect = fm.getStringBounds(paperWidthText, g);
		final int hHalfOffset = startX + (previewPaperWidth / 2) - lineX - ((int)(strRect.getWidth()/2))-5;
		g.drawLine(lineX, lineY, lineX + hHalfOffset, lineY);
		lineX += hHalfOffset + 1;
		g.drawLine(lineX, lineY-5, lineX, lineY+5);
		g.setColor(Color.BLACK);
		g.drawString(paperWidthText, lineX+5, lineY + 5);
		lineX += (int)strRect.getWidth() + 8;
		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(lineX, lineY-5, lineX, lineY+5);
		lineX++;
		g.drawLine(lineX, lineY, lineX+((startX+previewPaperWidth)-lineX), lineY);
	}
	
	private void drawVerticalPaperDimension(int startX, int startY, Graphics g) {
		int lineX = startX;
		int lineY = startY;
		String paperHeightText = getMediaSizeHeight() + "'";
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D strRect = fm.getStringBounds(paperHeightText, g);
		final int vHalfOffset = (previewPaperHeight/2) - (int)(strRect.getWidth()/2);
		g.drawLine(lineX, lineY, lineX, lineY + vHalfOffset);
		lineY += vHalfOffset + 1;
		g.drawLine(lineX - 5, lineY, lineX + (int)(strRect.getWidth()), lineY);
		g.setColor(Color.BLACK);
		g.drawString(paperHeightText, lineX-3, lineY + (int)(strRect.getHeight()) + 3); // - (int)(strRect.getWidth()/2) - 5, lineY + (int)(strRect.getHeight()) + 3);
		g.setColor(Color.LIGHT_GRAY);
		lineY += (int)(strRect.getHeight()) + 8;
		g.drawLine(lineX - 5, lineY, lineX + (int)(strRect.getWidth()), lineY);
		lineY++;
		g.drawLine(lineX, lineY, lineX, lineY+((startY+previewPaperHeight)-lineY));			
	}
	
	private double getFormatRatio() {
		return getMediaSizeHeight() / getMediaSizeWidth();
	}
	
	public int getTopMarginHeight() {
		return topMarginY - paperY;
	}
	
	public int getLeftMarginWidth() {
		return leftMarginX - paperX;
	}
	
	public int getBottomMarginHeight() {
		return (paperY + previewPaperHeight) - bottomMarginY;
	}
	
	public int getRightMarginWidth() {
		return (paperX + previewPaperWidth) - rightMarginX;
	}
	
	/**
	 * Scaled preview which fills entire preview paper (width or height, 
	 * depending on the orientation). Used for reference when further rescaling 
	 * the image (user wants it smaller, etc).
	 */
	private BufferedImage scaledPreviewFull;
	
	private void rescaleImage() throws IllegalStateException {
		
		if(previewImage == null) throw new IllegalStateException("Preview image not set!");
		if(previewPaperHeight == 0 || previewPaperWidth == 0) return;
		if(paintDrag) return;
		
		computeHardwareMargins();
		BufferedImage img = previewImage;
		
		if(isPortrait()) {
			scaledPreview = Scalr.rotate(img, Scalr.Rotation.CW_270);
			img = scaledPreview;			
		}
		ImageableSpecs specs = getImageableSpecs();
		if(img.getWidth() > img.getHeight()) {
			scaledPreview = Scalr.resize(
					img, 
					Scalr.Mode.FIT_TO_WIDTH,
					(specs.height-specs.y));
		}
		else {
			scaledPreview = Scalr.resize(
					img, 
					Scalr.Mode.FIT_TO_HEIGHT,
					(specs.width-specs.x));
		}

		int topMarginHeight = getTopMarginHeight();
		int bottomMarginHeight = getBottomMarginHeight();
		int leftMarginWidth = getLeftMarginWidth();
		int rightMarginWidth = getRightMarginWidth();
		int maxHeight = previewPaperHeight - (topMarginHeight + bottomMarginHeight);
		int maxWidth = previewPaperWidth - (leftMarginWidth + rightMarginWidth);
		double maxHeightRatio = maxHeight / (double)scaledPreview.getHeight();
		double maxWidthRatio = maxWidth / (double)scaledPreview.getWidth();
		if(maxHeightRatio < 1d || maxWidthRatio < 1d) {
			// image too large, won't fit on paper
			if(maxHeightRatio > maxWidthRatio) {
				scaledPreview = Scalr.resize(
						scaledPreview, 
						Scalr.Mode.FIT_TO_WIDTH, 
						maxWidth);				
			}
			else {
				scaledPreview = Scalr.resize(
						scaledPreview, 
						Scalr.Mode.FIT_TO_HEIGHT, 
						maxHeight);				
			}
		}
		
		scaledPreviewFull = ImageUtil.deepCopy(scaledPreview);
	}

	public void initialize() throws IllegalStateException{
		if(previewImage == null) throw new IllegalStateException();
		cropX = cropY = 0;
		configurePreviewPaper();
		rescaleImage();
		positionImage();
	}
	
	public boolean isQualityAdjustNecessary() {
		int topMarginHeight = getTopMarginHeight();
		int bottomMarginHeight = getBottomMarginHeight();
		int leftMarginWidth = getLeftMarginWidth();
		int rightMarginWidth = getRightMarginWidth();
		int maxHeight = previewPaperHeight - (topMarginHeight + bottomMarginHeight);
		int maxWidth = previewPaperWidth - (leftMarginWidth + rightMarginWidth);
		double maxHeightRatio = maxHeight / (double)scaledPreview.getHeight();
		double maxWidthRatio = maxWidth / (double)scaledPreview.getWidth();
		return !(maxHeightRatio < 1d || maxWidthRatio < 1d);		
	}
	
	private void updateInfo() {
		previewInfo.scaleRatio = (int)Math.round(renderScale * 100);
		previewInfo.imageHeight = isPortrait() ? previewImage.getWidth() : previewImage.getHeight();
		previewInfo.imageWidth = isPortrait() ? previewImage.getHeight() : previewImage.getWidth();
		previewInfo.printerSpecs = getMediaPrintableArea();
		int imgDim = Math.max(previewImage.getHeight(),previewImage.getWidth());
		float printableHeightInInches = previewInfo.printerSpecs.getHeight(MediaPrintableArea.INCH);// - previewInfo.printerSpecs.getY(MediaPrintableArea.INCH);
		float printableWidthInInches = previewInfo.printerSpecs.getWidth(MediaPrintableArea.INCH);// - previewInfo.printerSpecs.getX(MediaPrintableArea.INCH);
		double specDim = scaledPreview.getHeight() < scaledPreview.getWidth() ? printableHeightInInches : printableWidthInInches;
		//log.debug("imgDim: {}, specDim: {}", imgDim, specDim);
		previewInfo.ppi = (int)Math.floor(new Double(imgDim) / (specDim*renderScale));
	}
	
	/**
	 * percentage based factor indicating how large printed image relative to 
	 * printable area should be; by default, printed image fill up entire 
	 * printable area
	 */
	private double renderScale = 1d;
	
	private boolean scaleAdjusting = false;
	
	public void setScaleAdjusting(boolean scaleAdjusting) {
		this.scaleAdjusting = scaleAdjusting;
	}
	
	public boolean isScaleAdjusting() {
		return scaleAdjusting;
	}
	
	/**
	 * Sanity check for minor pixel discrepancies resulting from a lot of 
	 * percentage based math being applied. This is just for preview purposes 
	 * to make sure that image does not exceed preview hardware margin. 
	 */
	private void normalizeCoordinates() {
		int xDiff = (imgX + scaledPreview.getWidth()) - rightMarginX;
		int yDiff = (imgY + scaledPreview.getHeight()) - bottomMarginY;
		if(xDiff > 0) imgX = imgX - xDiff;
		if(yDiff > 0) imgY = imgY - yDiff;
	}
	
	/**
	 * Defines how much preview image should be scaled relative to it's size 
	 * when filling full paper. This value is used for preview and ultimately 
	 * when printing. Note that image often will be scaled down regardless of 
	 * this setting, just so it can fit full paper but this is done 
	 * automatically. By default this value is 1.0d, meaning that when image 
	 * fills up paper view it is at 100%. 
	 * 
	 * @param renderScale value between 0.1 and 1.0
	 */
	public void setRenderScale(double renderScale) {
		int scaledWidth = scaledPreview.getWidth();
		int scaledheight = scaledPreview.getHeight();
		boolean scaleOk = !scaleAdjusting || 
			(imgX + scaledWidth <= rightMarginX) && 
			(imgY + scaledheight <= bottomMarginY);

		if(!scaleOk) return;
		
		this.renderScale = renderScale;
		if(scaledPreviewFull == null) return;
		cropX = cropY = 0;
		if(scaledPreviewFull.getWidth() > scaledPreviewFull.getHeight()) {
			int targetWidth = (int)Math.round(scaledPreviewFull.getWidth()*renderScale);
			scaledPreview = Scalr.resize(
					scaledPreviewFull, 
					Scalr.Mode.FIT_TO_WIDTH,
					targetWidth);
		}
		else {
			int targetHeight = (int)Math.round(scaledPreviewFull.getHeight()*renderScale);
			scaledPreview = Scalr.resize(
					scaledPreviewFull, 
					Scalr.Mode.FIT_TO_HEIGHT,
					targetHeight);
		}
		updateInfo();
	}
	
	private int getPpi() {
		return JAVA_DPI_BASE;
	}
	
	/**
	 * @return screen to paper width ratio in java units (72 base)
	 */
	private double getWidthRatio() {
		double ppi = new Double(getPpi());
		double ratio = previewPaperWidth / (getMediaSizeWidth() * ppi);
		//log.debug("ratio: {}", ratio);
		return ratio;
	}
	
	/**
	 * @return screen to paper height ratio in java units (72 base)
	 */
	private double getHeightRatio() {
		double ppi = new Double(getPpi());
		double ratio = previewPaperHeight / (getMediaSizeHeight() * ppi);
		////log.debug("ratio: {}", ratio);
		return ratio;
	}
	
	/**
	 * Computes approximate ratio of preview paper to the actual paper for the 
	 * selected format. This is just an approximation since there is no way 
	 * in Java to determine the actual monitor viewable dimension metrics. This 
	 * isn't even guaranteed by the driver so even the underlying OS might not 
	 * know about it. This implementation simply asks the toolkit for what 
	 * it thinks the PPI is, which is typically anwhere between 72 and 96, but 
	 * again, it's just an apporixmation and as such this result is guaranteed 
	 * to be inacurrate. 
	 * 
	 * @return
	 */
	public double getScreenToPaperRatio() {
		return (getMediaSizeWidth() * INCH) / previewPaperWidth;
	}
	
	/**
	 * approximate value of pixels per inch on the display device; this is not 
	 * exact! (java/os/driver limitation)
	 */
	public static final int INCH = 
			Toolkit.getDefaultToolkit().getScreenResolution();
	
	/**
	 * indicates when preview image is being dragged (true)
	 */
	private boolean paintDrag = false;
	
	/**
	 * indicates when cropping box is being dragged (true)
	 */
	private boolean cropDrag = false;
	
	/**
	 * displacement of the X co-ordinate of the cropping box during dragging 
	 * process; used for redrawing cropping box while dragged
	 */
	private int cropXDragDelta = 0;

	/**
	 * displacement of the Y co-ordinate of the cropping box during dragging 
	 * process; used for redrawing cropping box while dragged
	 */
	private int cropYDragDelta = 0;

	@Override
	public void mouseDragged(MouseEvent e) {
		int xDelta = e.getX() - dragStartX;
		int yDelta = e.getY() - dragStartY;
		if(paintDrag) {
			if(imgX + xDelta > leftMarginX && (imgX + xDelta + scaledPreview.getWidth() < rightMarginX)) {
				xDisplacement = xDelta;
			}
			if((imgY + yDelta >= topMarginY) && (imgY + yDelta + scaledPreview.getHeight() <= bottomMarginY))
				yDisplacement = yDelta;
		}
		else if(cropDrag) {
			Rectangle crop = getCropRectangle();
			int cropX = crop.x + xDelta;
			int cropY = crop.y + yDelta;
			
//			log.debug("---");
//			log.debug("this.cropX: {}, this.cropY: {}", this.cropX, this.cropY);
//			log.debug("cropX: {}, cropY: {}", cropX, cropY);
//			log.debug("crop.x: {}, crop.y: {}", crop.x, crop.y);
//			log.debug("xDelta: {}, yDelta: {}", xDelta, yDelta);
//			log.debug("imgX: {}, imgY: {}", imgX, imgY);
//			log.debug("crop.height: {}, crop.width: {}", crop.height, crop.width);
//			log.debug("scaledPreview.getHeight(): {}, scaledPreview.getWidth(): {}", scaledPreview.getHeight(), scaledPreview.getWidth());
//			log.debug("crop.x >= imgX: {}", (crop.x >= imgX));
//			log.debug("crop.y >= imgY: {}", (crop.y >= imgY));
			
			if(cropX + crop.width <= imgX + scaledPreview.getWidth() && cropX >= imgX)
				this.cropXDragDelta = xDelta;
			if(cropY + crop.height <= imgY + scaledPreview.getHeight() && cropY >= imgY)
				this.cropYDragDelta = yDelta;
		}
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(isOverImage(e.getX(), e.getY()))
			setCursor(CustomCursors.Grab.getCursor());
		else if(isOverCropTool(e.getX(), e.getY()))
			setCursor(CustomCursors.Grab.getCursor());
		else
			setCursor(Cursor.getDefaultCursor());
	}
	
	private boolean isOverImage(int xPos, int yPos) {
		boolean overImg = xPos >= imgX && yPos >= imgY && 
				xPos <= imgX + scaledPreview.getWidth() && yPos <= imgY + scaledPreview.getHeight();
		return overImg && (beforeCropPreview != null || !showCroppingTool);
	}
	
	private boolean isOverCropTool(int xPos, int yPos) {
		Rectangle r = getCropRectangle();
		boolean overCrop = xPos >= r.x && yPos >= r.y && 
				xPos <= r.x + r.width && yPos <= r.y + r.height;
		return overCrop && showCroppingTool;
	}

	public void setOrientation(Orientation orientation) {
		//log.debug("{}", orientation);
		this.orientation = orientation;
	}

	public void setMediaFormat(MediaSizeName mediaFormat) {
		//log.debug("{}", mediaFormat);
		this.mediaFormat = mediaFormat;
	}
	
	public void setSelectedPrinter(PrintService selectedPrinter) {
		//log.debug("{}", selectedPrinter.getName());
		this.selectedPrinter = selectedPrinter;
	}
	
	private MediaPrintableArea getMediaPrintableArea() {
		return getMediaPrintableArea(mediaFormat);
	}

	MediaPrintableArea getMediaPrintableArea(MediaSizeName media) {
		MediaPrintableArea pa = null;
		PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
		attr.add(media);
		Object[] o = (Object[])selectedPrinter.getSupportedAttributeValues(
				MediaPrintableArea.class, 
				null, 
				attr);

		final String clazzName = MediaSizeName.class.getSimpleName();
		if(o == null) {
			log.warn("no {} detected!", clazzName);
		}
		else {
			// properly working driver should return 1 record for the requested
			// media, but there are crappy drivers which return all or none 
			// and there isn't much we can do about this other than try to 
			// detect most common formats (letter, legal, etc)
			log.trace("detected {} {} record(s)", o.length, clazzName);
			if(o.length == 1)
				pa = (MediaPrintableArea) o[0];
			else {
				for (int j = 0; j < o.length; j++) {
					MediaPrintableArea p = (MediaPrintableArea) o[j];
					if(MediaSizeName.NA_LETTER.equals(media) && BambiPrint.isLetter(p)) {
						pa = p;
						break;
					}
					else if(MediaSizeName.NA_LEGAL.equals(media) && BambiPrint.isLegal(p)) {
						pa = p;
						break;
					}
				}
			}
		}
		//log.debug("-----------------------------");
		//log.debug("x: {}, y: {}", pa.getX(MediaPrintableArea.INCH), pa.getY(MediaPrintableArea.INCH));
		//log.debug("w: {}, h: {}", pa.getWidth(MediaPrintableArea.INCH), pa.getHeight(MediaPrintableArea.INCH));
		return pa;
	}

	public void setPreviewInfo(PreviewInfo previewInfo) {
		this.previewInfo = previewInfo;
	}
	
	private boolean drawMargins = false;
	
	void setDrawMargins(boolean drawMargins) {
		this.drawMargins = drawMargins;
	}
	
	private int beforeCenterX;
	private int beforeCenterY;
	
	/**
	 * @param center true to center, false to restore last saved x position
	 */
	void centerHorizontal(boolean center) {
		if(center) {
			if(scaledPreview.getWidth() < previewPaperWidth) {
				beforeCenterX = imgX;
				int paperWidth = previewPaperWidth - getLeftMarginWidth() - getRightMarginWidth();
				imgX = leftMarginX + (paperWidth / 2 - scaledPreview.getWidth() / 2);
			}
		}
		else {
			imgX = beforeCenterX;
		}
	}
	
	/**
	 * @param center true to center, false to restore last saved x position
	 */
	void centerVertical(boolean center) {
		if(center) {
			if(scaledPreview.getHeight() < previewPaperHeight) {
				beforeCenterY = imgY;
				int paperHeight = previewPaperHeight - getTopMarginHeight() - getBottomMarginHeight();
				imgY = topMarginY + (paperHeight / 2 - scaledPreview.getHeight() / 2);
			}
		}
		else {
			imgY = beforeCenterY;
		}		
	}
	
	private BufferedImage beforeCropPreview;
	private int beforeCropImgX;
	private int beforeCropImgY;
	private BufferedImage croppedFullPreviewImage;
	/**
	 * @param crop true to crop; false to restore last image before crop;
	 */
	void executeCrop(boolean crop) {
		if(crop) {
			Rectangle cropRect = getCropRectangle();
			double xPct = new Double(cropX) / new Double(scaledPreview.getWidth());
			double yPct = new Double(cropY) / new Double(scaledPreview.getHeight());
			double wPct = new Double(cropRect.getWidth()) / new Double(scaledPreview.getWidth());
			double hPct = new Double(cropRect.getHeight()) / new Double(scaledPreview.getHeight());
			boolean rotated = isPortrait();
//			if(scaledPreview.getWidth() != scaledPreview.getHeight()) {
//				BigDecimal scaleRatio = new BigDecimal(new Double(scaledPreview.getWidth()) / new Double(scaledPreview.getHeight()));
//				BigDecimal fullRatio = new BigDecimal(new Double(previewImage.getWidth()) / new Double(previewImage.getHeight()));
//				scaleRatio = scaleRatio.setScale(1, RoundingMode.CEILING);
//				fullRatio = fullRatio.setScale(1, RoundingMode.CEILING);
//				log.debug("scaleRatio: {}, fullRatio: {}", scaleRatio.doubleValue(), fullRatio.doubleValue());
//				rotated = scaleRatio.doubleValue() != fullRatio.doubleValue();
//			}
			BufferedImage src = rotated ? Scalr.rotate(previewImage, Scalr.Rotation.CW_270) : previewImage;
			int x = (int)Math.round(src.getWidth() * xPct);
			int y = (int)Math.round(src.getHeight() * yPct);
			int w = (int)Math.round(src.getWidth() * wPct);
			int h = (int)Math.round(src.getHeight() * hPct);
			beforeCropPreview = ImageUtil.deepCopy(scaledPreview);
			beforeCropImgX = imgX;
			beforeCropImgY = imgY;
			croppedFullPreviewImage = Scalr.crop(src, x, y, w, h);
			if(!rotated && croppedFullPreviewImage.getWidth() > croppedFullPreviewImage.getHeight()) {
				scaledPreview = Scalr.resize(
						croppedFullPreviewImage, 
						Scalr.Mode.FIT_TO_WIDTH,
						previewPaperWidth - getLeftMarginWidth() - getRightMarginWidth());
			}
			else {
				scaledPreview = Scalr.resize(
						croppedFullPreviewImage, 
						Scalr.Mode.FIT_TO_HEIGHT,
						previewPaperHeight - getTopMarginHeight() - getBottomMarginHeight());				
			}
			if(rotated) {
				// restore cropped to original rotation (needed if used for ptinting)
				croppedFullPreviewImage = Scalr.rotate(croppedFullPreviewImage, Scalr.Rotation.CW_90);
			}
			imgX = leftMarginX;
			imgY = topMarginY;
		}
		else {
			if(beforeCropPreview != null) {
				scaledPreview = ImageUtil.deepCopy(beforeCropPreview);
				beforeCropPreview.flush();
				beforeCropPreview = null;
				imgX = beforeCropImgX;
				imgY = beforeCropImgY;
			}
			if(croppedFullPreviewImage != null) {
				croppedFullPreviewImage.flush();
				croppedFullPreviewImage = null;
			}
		}
	}
	
	public PrintOutputConfig getOutputConfiguration() {
		PrintOutputConfig config = new PrintOutputConfig();
		ImageableSpecs s = getImageableSpecs();
		if(isPortrait()) {
			double x = bottomMarginY - (imgY + scaledPreview.getHeight());
			config.xDisplacementPct = x / new Double(s.width-(s.x*2));
			double y = imgX - leftMarginX;
			config.yDisplacementPct = y / new Double(s.height-(s.y*2));
		}
		else {
			double x = imgX - leftMarginX;
			config.xDisplacementPct = x / new Double(s.height-(s.y*2));
			double y = imgY - topMarginY;
			//log.debug("*** imgY: {}, topMarginY: {}", imgY, topMarginY);
			//log.debug("*** y: {}", y);
			config.yDisplacementPct = y / new Double(s.width-(s.x*2));
		}
		
		config.printerSpecs = getPrintableSpecs();
		config.mediaFormat = mediaFormat;
		if(config.image == null) {
			if(croppedFullPreviewImage == null) {
				config.image = previewImage;
				config.renderScalePct = renderScale;
			}
			else {
				config.image = croppedFullPreviewImage;
				config.renderScalePct = 1d;
			}
		}
		return config;
	}
}