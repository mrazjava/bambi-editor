package org.zimowski.bambi.editor.customui.statusbar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import com.oracle.layout.SpringUtilities;

/**
 * A cell that can display RGB color information. Highly specific to RGB info, 
 * for more generic color cell see {@link ColoredBackgroundCell}.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class RgbCell extends EmptyCell {

	private static final long serialVersionUID = -2061647644578092215L;
	
	//private static final Logger log = LoggerFactory.getLogger(RgbCell.class);

	private ColoredBackgroundCell pixelCell;
	
	private ColoredBackgroundCell redCell;
	
	private ColoredBackgroundCell greenCell;
	
	private ColoredBackgroundCell blueCell;
	
	
	public RgbCell() {
		
		super();
		setLayout(new BorderLayout());
		
		pixelCell = new ColoredBackgroundCell();
		pixelCell.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(1,1,1,0), new EtchedBorder()));
		pixelCell.setPreferredSize(new Dimension(65, 0));
		pixelCell.setTextAlignment(SwingConstants.CENTER);
		
		JPanel rgbPanel = new JPanel(new SpringLayout());
		
		redCell = new ColoredBackgroundCell(Color.RED);
		greenCell = new ColoredBackgroundCell(Color.GREEN);
		blueCell = new ColoredBackgroundCell(Color.BLUE);
		
		redCell.setTextAlignment(SwingConstants.CENTER);
		greenCell.setTextAlignment(SwingConstants.CENTER);
		blueCell.setTextAlignment(SwingConstants.CENTER);
		
		rgbPanel.add(redCell);
		rgbPanel.add(greenCell);
		rgbPanel.add(blueCell);
		
		SpringUtilities.makeGrid(rgbPanel, 1, 3, 1, 1, 1, 0);

		add(pixelCell, BorderLayout.WEST);
		add(rgbPanel, BorderLayout.CENTER);
	}
	
	public TextCell getPixelCell() {
		return pixelCell;
	}
	
	public TextCell getRedCell() {
		return redCell;
	}
	
	public TextCell getGreenCell() {
		return greenCell;
	}
	
	public TextCell getBlueCell() {
		return blueCell;
	}

	public void setPixelColor(int pixelColor) {
		Color color = pixelColor != 0 ? new Color(pixelColor) : getBackground();
		pixelCell.setBackground(color);
		redCell.setText(String.valueOf(color.getRed()));
		greenCell.setText(String.valueOf(color.getGreen()));
		blueCell.setText(String.valueOf(color.getBlue()));
	}

	public void resetColor() {
		pixelCell.setBackground(getBackground());
		redCell.setText(null);
		greenCell.setText(null);
		blueCell.setText(null);
	}
}