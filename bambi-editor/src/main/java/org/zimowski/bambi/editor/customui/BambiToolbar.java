package org.zimowski.bambi.editor.customui;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.zimowski.bambi.editor.studio.resources.toolbar.ToolbarIcons;

/**
 * Clone of {@link JToolBar} with slightly different separator which presents 
 * a vertical line rather than empty space, and allows for setting 
 * {@link EmptyBorder} to control insets.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class BambiToolbar extends JToolBar {

	private static final long serialVersionUID = 7219307519541562739L;

	@Override
	public void addSeparator() {
		add(buildSeparator());
	}

	@Override
	public void addSeparator(Dimension size) throws IllegalStateException {
		throw new IllegalStateException("Operation not supported");
	}
	
	public void addSeparator(EmptyBorder border) {
		JLabel separator = buildSeparator();
		separator.setBorder(border);
		add(separator);
	}

	private JLabel buildSeparator() {
		ImageIcon i = ToolbarIcons.Separator.getIcon();
		return new JLabel(i);
	}
}