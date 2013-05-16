package org.zimowski.bambi.webstart;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class SplashPanel extends JPanel {

	private static final long serialVersionUID = -3745767628900539100L;

	private BufferedImage backgroundImg = null;
	
	public SplashPanel() {
		String splashBg = "/Forest-Trees450x300.jpg";
		URL url = SplashPanel.class.getResource(splashBg);
		try {
			backgroundImg = ImageIO.read(url);
		} catch (IOException e) {
			System.out.println(splashBg + " not found");
		}
		Dimension size = null;
		if(backgroundImg != null) {
			size = new Dimension(backgroundImg.getWidth(), backgroundImg.getHeight());
		}
		else
			size = new Dimension(450, 300);
		
		setPreferredSize(size);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(backgroundImg != null) {
			Graphics2D g2 = (Graphics2D)g.create();
			AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f);
		    g2.setComposite(composite);
		    g2.drawImage(backgroundImg, 0, 0, null);
		}
	}
}