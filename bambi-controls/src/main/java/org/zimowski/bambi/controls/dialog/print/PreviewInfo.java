package org.zimowski.bambi.controls.dialog.print;

import java.awt.Color;
import java.awt.Graphics;
import java.text.DecimalFormat;

import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.swing.JComponent;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class PreviewInfo extends JComponent {

	private static final long serialVersionUID = -2030707021942961056L;

	int scaleRatio;
	
	int imageWidth;
	
	int imageHeight;
	
	MediaSize mediaSize;
	
	MediaPrintableArea printerSpecs;
	
	private DecimalFormat df = new DecimalFormat("#.###");
	
	int ppi;
	
	@Override
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		int row = 10;
		//g.drawString("Preview Scale Ratio:", 0, row);
		///g.setColor(Color.BLUE);
		//g.drawString("1:" +  df.format(paperScaleRatio), 150, row);
		//row += 15;
		g.setColor(Color.BLACK);
		g.drawString("Imageable Height:", 0, row);
		double imageableWidth = 0d;
		if(printerSpecs != null) {
			g.setColor(Color.BLUE);
			imageableWidth = printerSpecs.getWidth(MediaPrintableArea.INCH) - printerSpecs.getX(MediaPrintableArea.INCH);
			g.drawString(df.format(imageableWidth) + " in", 150, row);
		}
		double imageableHeight = 0;
		row += 15;
		g.setColor(Color.BLACK);
		g.drawString("Imageable Width:", 0, row);
		if(printerSpecs != null) {
			imageableHeight = printerSpecs.getHeight(MediaPrintableArea.INCH) - printerSpecs.getY(MediaPrintableArea.INCH);
			g.setColor(Color.BLUE);
			g.drawString(df.format(imageableHeight) + " in", 150, row);
		}
		double imageableY = 0;
		row += 15;
		g.setColor(Color.BLACK);
		g.drawString("Left Margin:", 0, row);
		if(printerSpecs != null) {
			imageableY = printerSpecs.getY(MediaPrintableArea.INCH);
			g.setColor(Color.BLUE);
			g.drawString(df.format(imageableY) + " in", 150, row);
		}
		row += 15;
		g.setColor(Color.BLACK);
		g.drawString("Right Margin:", 0, row);
		if(printerSpecs != null) {
			//double mediaHeight = mediaSize.getY(MediaSize.INCH);
			//double rightMargin =  mediaHeight - imageableHeight + imageableY;
			g.setColor(Color.BLUE);
			g.drawString(df.format(imageableY) + " in", 150, row);
		}
		double imageableX = 0d;
		row += 15;
		g.setColor(Color.BLACK);
		g.drawString("Bottom Margin:", 0, row);
		if(printerSpecs != null) {
			imageableX = printerSpecs.getX(MediaPrintableArea.INCH);
			g.setColor(Color.BLUE);
			g.drawString(df.format(imageableX) + " in", 150, row);
		}
		row += 15;
		g.setColor(Color.BLACK);
		g.drawString("Top Margin:", 0, row);
		if(printerSpecs != null) {
			//double mediaWidth = mediaSize.getX(MediaSize.INCH);
			//double topMargin = mediaWidth - imageableWidth + imageableX;
			g.setColor(Color.BLUE);
			g.drawString(df.format(imageableX) + " in", 150, row);
		}
		row += 15;
		g.setColor(Color.BLACK);
		g.drawString("Output Resolution:", 0, row);
		g.setColor(Color.BLUE);
		g.drawString(Integer.toString(ppi) + " PPI", 150, row);
		row += 15;
		g.setColor(Color.BLACK);
		g.drawString("Output Quality:", 0, row);
		g.setColor(Color.BLUE);
		g.drawString(getPrintQuality(ppi), 150, row);		
		row += 15;
		g.setColor(Color.BLACK);
		g.drawString("Image Resolution:", 0, row);
		g.setColor(Color.BLUE);
		g.drawString(imageWidth + "x" + imageHeight + " px", 150, row);		
		row += 15;
		g.setColor(Color.BLACK);
		g.drawString("Render Scale:", 0, row);
		g.setColor(Color.BLUE);
		g.drawString(scaleRatio + "%", 150, row);		
	}

	private String getPrintQuality(int dpi) {
		if(dpi > 1000)
			return "AMAZING";
		else if(dpi > 600)
			return "VERY HIGH";
		else if(dpi > 300)
			return "HIGH";
		else if(dpi > 150)
			return "GOOD";
		else if(dpi > 72)
			return "FAIR";
		else
			return "POOR";
	}
}