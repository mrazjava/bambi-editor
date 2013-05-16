package org.zimowski.bambi.editor.customui.statusbar;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.apache.commons.lang3.StringUtils;

import com.oracle.layout.SpringUtilities;

/**
 * A cell which contains color box whose background can be adjusted. If this 
 * cell draws text, the foreground color is dynamically computed to best 
 * contrast the background. Good for reporting color changes, such as pixel 
 * color over mouse movement.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class ColoredBackgroundCell extends TextCell {
	
	private static final long serialVersionUID = -2236089114041606507L;
	
	//private static final Logger log = LoggerFactory.getLogger(ColoredBackgroundCell.class);
	
	private JPanel cell = new JPanel();

	
	public ColoredBackgroundCell() {
		
		super();
		
		setLayout(new SpringLayout());
		
		add(cell);
		SpringUtilities.makeGrid(this, 1, 1, -1, -1, 0, 0);
	}
	
	/**
	 * @param bg initial background
	 */
	public ColoredBackgroundCell(Color bg) {
		this();
		cell.setBackground(bg);
	}

	@Override
	protected void paintText(Graphics g) {
		if(StringUtils.isNotBlank(text)) {
			// compute foreground color based on avarage shade of back color
			Color bg = cell.getBackground();
			int avg = (bg.getRed() + (int)(bg.getGreen()*1.55) + bg.getBlue()) / 3;
			//log.debug("avg: {}", avg);
			Color fore = (avg > 128 ? Color.BLACK : Color.WHITE);
			g.setColor(fore);
		}
		super.paintText(g);
	}

	@Override
	protected int getYOffset() {
		return super.getYOffset() - 2;
	}

	@Override
	public void setBackground(Color bg) {
		if(cell != null && bg != null) cell.setBackground(bg);
	}
}