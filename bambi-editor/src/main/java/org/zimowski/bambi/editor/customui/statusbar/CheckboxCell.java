package org.zimowski.bambi.editor.customui.statusbar;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Status bar cell with a checkbox support. Using default constructor, the 
 * checkbox is left aligned, but alignment can be controlled using alternate 
 * constructor. Entire cell responds to checkbox click.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class CheckboxCell extends TextCell {

	private static final long serialVersionUID = 7882800542089629364L;

	private static final Logger log = LoggerFactory.getLogger(CheckboxCell.class);
	
	private JCheckBox check;
	
	
	/**
	 * Constructs a cell with a plain {@link JCheckBox} and default horizontal 
	 * alignment.
	 */
	public CheckboxCell() {
		super();
		initCheckbox(new JCheckBox());
	}
	
	public CheckboxCell(String text) {
		this();
		setText(text);
	}
	
	private void initCheckbox(JCheckBox checkBox) {
		check = checkBox;
		alignCheckbox();
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				check.doClick();
				MouseListener[] ml = check.getMouseListeners();
				for(MouseListener l : ml) l.mouseClicked(e);
			}
		});
	}

	/**
	 * Limited base functionality by restricting center alignment which does 
	 * not make a whole lot of sense from UI checkbox perspective. Only right 
	 * and left alignment are allowed.
	 * 
	 * @param textAlignment {@link SwingConstants#LEFT}, or 
	 *  	{@link SwingConstants#RIGHT}
	 */
	@Override
	public void setTextAlignment(int textAlignment) {
		if(textAlignment == SwingConstants.CENTER) {
			log.warn("invalid alignment option");
			return;
		}
		super.setTextAlignment(textAlignment);
		remove(check);
		alignCheckbox();
	}
	
	private void alignCheckbox() {
		String constraint = null;
		Border border = null;
		if(textAlignment == SwingConstants.RIGHT) {
			constraint = BorderLayout.EAST;
			border = new EmptyBorder(0,3,0,5);
		}
		else if(textAlignment == SwingConstants.LEFT) {
			constraint = BorderLayout.WEST;
			border = new EmptyBorder(0,5,0,3);
		}
		if(border != null) check.setBorder(border);
		if(constraint != null) add(check, constraint);

	}

	public JCheckBox getCheckbox() {
		return check;
	}

	@Override
	protected int getXOffset() {
		int factor = (textAlignment == SwingConstants.RIGHT ? -1 : 1);
		return super.getXOffset() + (check.getWidth() * factor);
	}
}