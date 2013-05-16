package org.zimowski.bambi.editor.customui.statusbar;

import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

/**
 * The most basic status bar cell. Provides no content display features, other 
 * than direct painting. Provides means of font standardization across all 
 * cells in the status bar, as it defines base font. Can also be used as a 
 * filler for empty space. Good starting point for custom cells.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class EmptyCell extends JPanel {

	private static final long serialVersionUID = -1131524531867797768L;

	public EmptyCell() {
		setBorder(new EtchedBorder(EtchedBorder.LOWERED));
	}
	
	// FIXME: either refactor names or redesign font function stuff
	protected Font getFont(String name) {
		return new Font(name, getFontStyle(), getFontSize());
	}
	
	/**
	 * May be overwritten by child classes to customize font style. 
	 * 
	 * @return Always {@link Font#PLAIN}
	 */
	protected int getFontStyle() {
		return Font.PLAIN;
	}
	
	/**
	 * May be overwritten by child classes to customize font size. Customizing 
	 * font size will likely require also customizing {@link #getYOffset()}.
	 * 
	 * @return Always 10
	 */
	protected int getFontSize() {
		return 10;
	}
}