package org.zimowski.bambi.editor.studio.image;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link JLabel} with scrolling functionality and capability to adjust scroll 
 * unit (in pixels).
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class ScrollableJLabel extends JLabel implements Scrollable {
	
	private static final long serialVersionUID = 1314994477636206981L;

	private static final Logger log = 
			LoggerFactory.getLogger(ScrollableJLabel.class);

	/**
	 * Unit of scrolling, in pixels. Default is 1 pixel.
	 */
	private int maxUnitIncrement = 1;

	@Override
	public int getScrollableUnitIncrement(
			Rectangle visibleRect,
			int orientation, 
			int direction) {

		// get the current offset
		int currentTotalOffset = 0;
		if (orientation == SwingConstants.HORIZONTAL) {
			currentTotalOffset = visibleRect.x;
		} else {
			currentTotalOffset = visibleRect.y;
		}

		// return the number of pixels between currentPosition and the nearest
		// tick mark in the indicated direction.
		int newOffset;
		if (direction < 0) {
			int pos = currentTotalOffset
					- (currentTotalOffset / maxUnitIncrement)
					* maxUnitIncrement;
			newOffset = (pos == 0) ? maxUnitIncrement : pos;
		} else {
			newOffset = ((currentTotalOffset / maxUnitIncrement) + 1)
					* maxUnitIncrement - currentTotalOffset;
		}

		return newOffset;
	}

	@Override
	public int getScrollableBlockIncrement(
			Rectangle visibleRect,
			int orientation, 
			int direction) {

		log.trace("o: {}, d: {}", orientation, direction);

		if (orientation == SwingConstants.HORIZONTAL) {
			return visibleRect.width - maxUnitIncrement;
		} else {
			return visibleRect.height - maxUnitIncrement;
		}
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

	public void setMaxUnitIncrement(int pixels) {
		maxUnitIncrement = pixels;
	}
}