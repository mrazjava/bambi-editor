package org.zimowski.bambi.editor.studio;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.config.Configuration;

/**
 * Custom dropdown cells which contain no text and are filled with the 
 * background color to indicate color selection. In addition, draws check 
 * icon in the middle of a selected or mouse hovered cell.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class ComboBoxColorCellRenderer implements ListCellRenderer<Color> {
	
	private static final Logger log = 
			LoggerFactory.getLogger(ComboBoxColorCellRenderer.class);

	public static final Dimension PREFERRED_SIZE = new Dimension(40, 23);
	
	public ComboBoxColorCellRenderer() {
	}
	
	@Override
	public Component getListCellRendererComponent(
			final JList<? extends Color> list, 
			Color value,
			final int index, 
			final boolean isSelected, 
			final boolean cellHasFocus) {

		final Color color = (Color)value;
		JPanel cell = new JPanel() {

			private static final long serialVersionUID = 1728658006615510018L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				final int width = getWidth();
				final int height = getHeight();
				
				Color selectedColor = list.getSelectedValue();
				
				log.trace("c: {}, sc: {}", color.getRGB(), selectedColor.getRGB());
				
				g.setColor(color);
				g.fillRect(0, 0, width, height);

				URL checkImageUrl = 
						getClass().getResource(Configuration.RESOURCE_PATH + "check16x16.png");

				if(isSelected || color.getRGB() == selectedColor.getRGB()) {
					int x = 4;
					int y = 1;
					if(isSelected) {
						x = 5;
						y = 3;
					}
					try {
						BufferedImage img = ImageIO.read(checkImageUrl);
						g.drawImage(img, x, y, null);
					}
					catch(IOException e) {}
					if(isSelected) {
						list.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}	
				}
			}
			
		};
		cell.setPreferredSize(ComboBoxColorCellRenderer.PREFERRED_SIZE);

		return cell;
	}
}