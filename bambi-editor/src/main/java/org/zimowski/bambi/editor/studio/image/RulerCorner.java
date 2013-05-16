package org.zimowski.bambi.editor.studio.image;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;


/**
 * Corner filler for the {@link Ruler}.
 * 
 * @author Sun Microsystems (0.01)
 * @author Adam Zimowski (mrazjava) (1.0+)
 * 
 * @version 0.01 - initial release; part of Accessible Scroll Demo
 * 	http://www.java2s.com/Code/Java/Swing-JFC/AccessibleScrollDemo.htm
 * @version 0.1 - (2010) integrated to imager applet
 * @version 1.0 - (2012) enabled in imager applet; javadoc
 */
public class RulerCorner extends JComponent {

	private static final long serialVersionUID = -1102233010678848563L;

	protected void paintComponent(Graphics g) {
        // Fill me with dirty brown/orange.
        g.setColor(new Color(230, 163, 4));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}