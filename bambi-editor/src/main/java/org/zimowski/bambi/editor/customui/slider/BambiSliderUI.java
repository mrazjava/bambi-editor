package org.zimowski.bambi.editor.customui.slider;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * Custom {@link JSlider} UI that conveys Bambi look. It paints custom track, 
 * thumb and adds icons at both ends of the track. The icons and color track 
 * are defined via {@link SliderDecor}.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class BambiSliderUI extends BasicSliderUI {
	
	protected JSlider slider;
	
	protected Image smallIcon = null;
	
	protected Image largeIcon = null;
	
	private Icon thumbIcon = null;

	private SliderDecor decor;
	
	
	/**
	 * @param slider - slider for which this UI strategy should be applied
	 * @param decor - grapical artifacts to decorate slider with
	 * @throws IllegalArgumentException if either argument is null
	 */
	public BambiSliderUI(JSlider slider, SliderDecor decor) {
		super(null);
		if(slider == null) throw new IllegalArgumentException("slider not bound!");
		if(decor == null) throw new IllegalArgumentException("decor not defined!");
		
		smallIcon = decor.getMinIcon();
		largeIcon = decor.getMaxIcon();
		thumbIcon = new ImageIcon(SliderDecor.Thumb.getMaxIcon());

		this.slider = slider;
		this.decor = decor;
	}

	@Override
	public void paintTrack(Graphics g) {

		int trackX = slider.getWidth()/2;

		// main track line
		g.setColor(decor.getColor());
		g.drawLine(trackX, 5, trackX, slider.getHeight()-8);
		// middle line
		for(int x=0; x<=2; x++) {
			g.drawLine(trackX-5, (slider.getHeight()/2)+x, trackX+5, (slider.getHeight()/2)+x);
		}

		if(largeIcon != null) {
			g.drawImage(largeIcon, trackX-(largeIcon.getWidth(null)/2), 2, null);
		}
		if(smallIcon != null && slider != null) {
			g.drawImage(smallIcon, trackX-4, slider.getHeight()-smallIcon.getHeight(null)-4, null);
		}
	}

	@Override
	public void paintThumb(Graphics g) {
		g.translate(thumbRect.x, thumbRect.y);
		thumbIcon.paintIcon(slider, g, 0, 0);
		g.translate(-thumbRect.x, -thumbRect.y);
	}
	
	@Override
    protected Dimension getThumbSize() {
        Dimension size = new Dimension();
        size.width = thumbIcon.getIconWidth();
        size.height = thumbIcon.getIconHeight();
        return size;
    }
	
	@Override
    public void paintFocus(Graphics g)  {        
    }
}