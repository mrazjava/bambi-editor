package org.zimowski.bambi.editor.customui.statusbar;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic text cell for the status bar. Supports horizontal text alignment. 
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class TextCell extends EmptyCell implements MouseListener {

	private static final long serialVersionUID = 234118198405215724L;
	
	private static final Logger log = LoggerFactory.getLogger(TextCell.class);

	private final int X_OFFSET = 5;
	
	private final int Y_OFFSET = 13;

	protected String text;
	
	private boolean textOnDisplayTrimmed = false;
	
	/**
	 * Cell's default tooltip; need to cache since it changes dynamically
	 */
	protected String toolTipText;
	
	/**
	 * Valid values: {@link SwingConstants#LEFT}, 
	 * {@link SwingConstants#CENTER} and {@link SwingConstants#RIGHT} 
	 */
	protected int textAlignment = SwingConstants.LEFT;
	
	/**
	 * Computed at paint time to properly align text
	 */
	private int alignmentXOffset;
	
	/**
	 * Defines if tooltip should be managed dynamically; true if yes. Dynamic 
	 * tooltip management means changing tooltip display when text on display 
	 * is trimmed to help identify what is the context of the said trimmed 
	 * text
	 */
	private boolean dynamicTooltip = true;

	
	public TextCell() {
		super();
		setLayout(new BorderLayout());
		addMouseListener(this);
	}
	
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		paintText(g);
	}
	
	/**
	 * Used when computing distance at which string should be trimmed. Child 
	 * classes that may draw additional artifacts (such as icons) may want to 
	 * override this to account for those.
	 * 
	 * @return offset always 10 
	 */
	protected int getTrimOffset() {
		return 10;
	}
	
	/**
	 * @return true if text currently displayed is trimmed because it can't 
	 * 	fit in the cell; false if it is fully displayed
	 */
	public boolean isTextOnDisplayTrimmed() {
		return textOnDisplayTrimmed;
	}
	
	/**
	 * @return cell tooltip string when display string is trimmed due to cell 
	 * 	size being too small
	 */
	public String getTooltipTextWhenTrimmed() {
		return this.text;
	}
	
	/**
	 * Draws text inside the cell taking into account horizontal alignment.
	 * 
	 * @param g graphic context used to paint the text
	 */
	protected void paintText(Graphics g) {
		if(StringUtils.isNotEmpty(text)) {
			Font f = g.getFont();
			Font newFont = getFont(f.getName());
			g.setFont(newFont);
			FontMetrics fm = g.getFontMetrics();
			Rectangle2D bounds = fm.getStringBounds(text, g);
			if(textAlignment != SwingConstants.LEFT) {
				if(textAlignment == SwingConstants.CENTER)
					alignmentXOffset = (getWidth() - (int)bounds.getWidth()) / 2 - X_OFFSET;
				else
					alignmentXOffset = getWidth() - (int)bounds.getWidth() - Y_OFFSET;
			}
			else {
				alignmentXOffset = 0;
			}
			String text = null;
			int acceptedWidth = getWidth() - getTrimOffset();
			textOnDisplayTrimmed = bounds.getWidth() > acceptedWidth;
			if(textOnDisplayTrimmed) {
				text = trimLine(this.text, fm, acceptedWidth, " ...");
			}
			else {
				text = this.text;
			}
			if(text != null) g.drawString(text, getXOffset(), getYOffset());
		}
	}
	
	protected int getXOffset() {
		return X_OFFSET + alignmentXOffset;
	}
	
	protected int getYOffset() {
		return Y_OFFSET;
	}
	
	/**
	 * Sets horizontal text alignment. For custom cells which include GUI  
	 * controls such as checkbox, these controls may also be aligned.
	 * 
	 *  @param textAlignment {@link SwingConstants#LEFT}, 
	 *  {@link SwingConstants#CENTER}, or {@link SwingConstants#RIGHT} 
	 */
	public void setTextAlignment(int textAlignment) {
		if(textAlignment != SwingConstants.LEFT && 
				textAlignment != SwingConstants.CENTER && 
				textAlignment != SwingConstants.RIGHT) {
			
			log.warn("invalid alignment option");
			return;
		}
		this.textAlignment = textAlignment;
	}
	
	protected String trimLine(String line, FontMetrics fm, int maxWidth, String linePostfix) {
		String postfix = (linePostfix != null ? linePostfix : "");
		int lineWidth = fm.stringWidth(line);
		String tmp = line.substring(0, line.length()-postfix.length());
		while(lineWidth > maxWidth) {
			tmp = tmp.substring(0, tmp.length()-1);
			lineWidth = fm.stringWidth(tmp + linePostfix);
		}
		return tmp + linePostfix;
	}

	/**
	 * {@inheritDoc} This implementation caches the tool tip.
	 */
	@Override
	public void setToolTipText(String text) {
		log.trace("tip [{}] | cached [{}]", text, toolTipText);
		setToolTipText(text, true);
	}

	/**
	 * @param text
	 * @param cacheTip true if tool tip should be cached
	 */
	public void setToolTipText(String text, boolean cacheTip) {
		super.setToolTipText(text);
		if(cacheTip) toolTipText = text;
	}

	public void setDynamicTooltip(boolean dynamicTooltip) {
		this.dynamicTooltip = dynamicTooltip;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if(dynamicTooltip) {
			if(isTextOnDisplayTrimmed()) {
				String tip = getTooltipTextWhenTrimmed();
				setToolTipText(tip, false);
			}
			else {
				setToolTipText(toolTipText);
			}
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}