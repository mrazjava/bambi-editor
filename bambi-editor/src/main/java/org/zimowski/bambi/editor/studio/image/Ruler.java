package org.zimowski.bambi.editor.studio.image;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Draws ruler acros horizontal and vertical axes in the picture pane. Enables 
 * to switch between metric and english system.
 * 
 * @author Sun Microsystems (0.01)
 * @author Adam Zimowski (mrazjava) (1.0+)
 * 
 * @version 0.01 - initial release; part of Accessible Scroll Demo 
 * 	http://www.java2s.com/Code/Java/Swing-JFC/AccessibleScrollDemo.htm
 * @version 0.1 - (2010) integrated to imager applet
 * @version 1.0 - (2012) enabled in imager applet; javadoc
 */
public class Ruler extends JComponent {

	private static final long serialVersionUID = -4902015460097934935L;
	
	private static final Logger log = LoggerFactory.getLogger(Ruler.class);
	
	public static final int INCH = 
			Toolkit.getDefaultToolkit().getScreenResolution();
	
    public static final int ORIENT_HORIZONTAL = 0;
    
    public static final int ORIENT_VERTICAL = 1;
    
    /**
     * Fixed portion of the size, in pixels. If ruler is horizontal, this 
     * would be the height. If ruler is vertical, this would be the width.
     */
    public static final int SIZE = 32;

    public int orientation;
    
    public boolean isMetric;
    
    private int increment;
    
    private int units;
    
    // ruler's length within the window, in pixels
    private int length;


    public Ruler(int o, boolean m) {
        orientation = o;
        isMetric = m;
        setIncrementAndUnits();
        //EventBusManager.getInstance().registerWithBus(this);
    }

    public void setIsMetric(boolean isMetric) {
        this.isMetric = isMetric;
        setIncrementAndUnits();
        repaint();
    }

    private void setIncrementAndUnits() {
        if (isMetric) {
            units = (int)((double)INCH / (double)2.54); // dots per centimeter
            increment = units;
        } else {
            units = INCH;
            increment = units / 2;
        }
    }

    public boolean isMetric() {
        return this.isMetric;
    }

    public int getIncrement() {
        return increment;
    }

    protected void paintComponent(Graphics g) {

    	super.paintComponent(g);
    	    	
    	Rectangle drawHere = new Rectangle(length, length);

        log.trace("w [{}] h [{}]", drawHere.width, drawHere.height);
        
        // Fill clipping area with dirty brown/orange.
        g.setColor(new Color(230, 163, 4));
        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);

        // Do the ruler labels in a small font that's black.
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.setColor(Color.black);

        // Some vars we need.
        int end = 0;
        int start = 0;
        int tickLength = 0;
        String text = null;

        // Use clipping bounds to calculate first and last tick locations.
        if (orientation == ORIENT_HORIZONTAL) {
            start = (drawHere.x / increment) * increment;
            end = (((drawHere.x + drawHere.width) / increment) + 1)
                  * increment;
        } else {
            start = (drawHere.y / increment) * increment;
            end = (((drawHere.y + drawHere.height) / increment) + 1)
                  * increment;
        }

        // Make a special case of 0 to display the number
        // within the rule and draw a units label.
        if (start == 0) {
            //text = (isMetric ? " cm" : " in");
            tickLength = 10;
            if (orientation == ORIENT_HORIZONTAL) {
                g.drawLine(0, SIZE-1, 0, SIZE-tickLength-1);
                g.drawString(Integer.toString(0), 2, 15);
            } else {
                g.drawLine(SIZE-1, 0, SIZE-tickLength-1, 0);
                g.drawString(Integer.toString(0), 7, 12);
                //g.drawString(Integer.toString(0), 9, 12);
                //int x = (isMetric ? 2 : 6);
                //g.drawString((isMetric ? " cm" : " in"), x, 23);
            }
            text = null;
            start = increment;
        }

        // ticks and labels
        for (int i = start; i < end; i += increment) {
            if (i % units == 0)  {
                tickLength = 10;
                text = Integer.toString(i/units);
            } else {
                tickLength = 7;
                text = null;
            }

            if (tickLength != 0) {
                if (orientation == ORIENT_HORIZONTAL) {
                    g.drawLine(i, SIZE-1, i, SIZE-tickLength-1);
                    if (text != null)
                        g.drawString(text, i-3, 15);
                } else {
                    g.drawLine(SIZE-1, i, SIZE-tickLength-1, i);
                    if (text != null)
                        g.drawString(text, 7, i+3);
                }
            }
        }
    }
    
    public void setLength(int length) {
    	
    	this.length = length;
    	
    	if(orientation == ORIENT_HORIZONTAL)
    		setPreferredSize(new Dimension(length, SIZE));
    	else
    		setPreferredSize(new Dimension(SIZE, length));
    }
/*    
    @Subscribe
    public void onModelReset(ModelLifecycleEvent ev) { 
    	if(ModelPhase.Reset.equals(ev.getPhase())) {
    		repaint();
    	}
    }*/
}