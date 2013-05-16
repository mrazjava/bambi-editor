package org.zimowski.bambi.controls.dialog.print;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.print.attribute.standard.MediaPrintableArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.controls.dialog.print.PrintPreview.PrintOutputConfig;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class BambiPrint implements Printable {

	private static final Logger log = LoggerFactory.getLogger(BambiPrint.class);
	
	private PrintOutputConfig printConfig;


	public BambiPrint(PrintOutputConfig config) {
		this.printConfig = config;
	}
	
	@Override
	public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
		
		if(printConfig.image == null)
			throw new PrinterException("no image specified to be printed");

		// we have only one page because we always print image on a single 
		// page; 'page' is zero-based
		if (pi > 0) {
			return NO_SUCH_PAGE;
		} 
		 
		Point2D p = getLeftUpCorner(pf);
		Paper paper = pf.getPaper();

		final double imageableWidth = pf.getImageableWidth() - paper.getImageableX();
		final double imageableHeight = pf.getImageableHeight() - paper.getImageableY();
		final double imageableWidthInch = imageableWidth / 72d;
		final double imageableHeightInch = imageableHeight / 72d;
		
		//log.info("imageable width: {} px", imageableWidth);
		//log.info("imageable height: {} px", imageableHeight);
		//log.info("imageable width: {} in", imageableWidthInch);
		//log.info("imageable height: {} in", imageableHeightInch);
		//log.info("image width: {} px", printConfig.image.getWidth());
		//log.info("image height: {} px", printConfig.image.getHeight());
		
		double fullDpi = 0d;
		//double scaledDpi = 0d;
		if(printConfig.image.getWidth() > printConfig.image.getHeight())
			fullDpi = new Double(printConfig.image.getWidth()) / imageableWidthInch;
		else
			fullDpi = new Double(printConfig.image.getHeight()) / imageableHeightInch;
		
		//log.info("fullsize dpi: {}", fullDpi);
		
		double fullSizePct = 72d / fullDpi;
		
		//log.info("fullSizePct: {}", fullSizePct);
		//log.info("renderScalePct: {}", printConfig.renderScalePct);
		
		double scale = fullSizePct * printConfig.renderScalePct;
		
		log.info("scale: {}", scale);
		
		Graphics2D g2d = (Graphics2D) g;
		
		log.info("p.getX(): {} px, p.getY(): {} px", p.getX(), p.getY());
		log.info("xDisplacementPct: {}, yDisplacementPct: {}", printConfig.xDisplacementPct, printConfig.yDisplacementPct);
		int xDisplacement = (int)Math.round((imageableWidth) * printConfig.xDisplacementPct);
		int yDisplacement = (int)Math.round((imageableHeight) * printConfig.yDisplacementPct);
		log.info("xDisplacement: {} px, yDisplacement: {} px", xDisplacement, yDisplacement);

		g2d.translate(p.getX()+xDisplacement, p.getY()+yDisplacement);
		g2d.scale(scale, scale);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		                     RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				
		g2d.drawImage(printConfig.image, 0, 0, null);

		return PAGE_EXISTS;
	}
	
	/**
	 * Fix to BUG: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6726691
	 * 
	 * @param pf
	 * @return
	 */
	private Point2D getLeftUpCorner(PageFormat pf) {

		Paper p = pf.getPaper();

		if (pf.getOrientation() == PageFormat.PORTRAIT)
			return new Point2D.Double(p.getImageableX(), p.getImageableY());
		else
			return new Point2D.Double(p.getImageableY(), p.getImageableX());

	}
	
	public static boolean isLetter(MediaPrintableArea pa) {
		if(pa == null) return false;
		double w = Math.round(pa.getWidth(MediaPrintableArea.INCH)*2d)/2d;
		double h = Math.round(pa.getHeight(MediaPrintableArea.INCH));
		return w==8.5d && h==11d;
	}
	
	public static boolean isLegal(MediaPrintableArea pa) {
		if(pa == null) return false;
		double w = Math.round(pa.getWidth(MediaPrintableArea.INCH)*2d)/2d;
		double h = Math.round(pa.getHeight(MediaPrintableArea.INCH));
		return w==8.5d && h==14d;
	}
}