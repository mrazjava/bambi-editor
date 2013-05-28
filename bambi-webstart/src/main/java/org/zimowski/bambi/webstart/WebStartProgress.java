package org.zimowski.bambi.webstart;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;

import javax.jnlp.DownloadServiceListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * Custom splash screen and download progress displayed when web start 
 * performs an application download. Because we want to keep the size of 
 * distributable JAR for this as small as possible we choose to log to System 
 * rather than typicall logger. The application for this downloader is packaged 
 * of course separately so it is free to include as much dependencies as it 
 * desires.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class WebStartProgress implements DownloadServiceListener {

	private JFrame frame = null;
	
	private JProgressBar progressBar = null;
	
	boolean uiCreated = false;

	public WebStartProgress() {
	}

	private void create() {
		
		String sysLNF = UIManager.getSystemLookAndFeelClassName();
		try {
			UIManager.setLookAndFeel(sysLNF);
		} catch (Exception e1) {
			System.err.println(e1.getMessage());
		}

		JPanel top = createComponents();
		frame = new JFrame();

		Container cont = frame.getContentPane();
		cont.setLayout(new BorderLayout());
		cont.add(top, BorderLayout.CENTER);

		frame.setUndecorated(true);
		frame.setTitle("BambiEditor - loading...");
		frame.pack();
		frame.setLocationRelativeTo(null);
		updateProgressUI(0);
	}

	private JPanel createComponents() {
		JPanel top = new SplashPanel();
		top.setOpaque(false);
		top.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5, 5,
				5, 5), BorderFactory.createCompoundBorder(new EtchedBorder(),
				new EmptyBorder(5, 5, 5, 5))));
		top.setLayout(new BorderLayout(2, 0));

		JPanel leftLogoPanel = new JPanel();
		JPanel rightLogoPanel = new JPanel();
		leftLogoPanel.setLayout(new BoxLayout(leftLogoPanel, BoxLayout.Y_AXIS));
		rightLogoPanel
				.setLayout(new BoxLayout(rightLogoPanel, BoxLayout.Y_AXIS));

		JPanel northPanel = new JPanel(new BorderLayout());

		URL url = WebStartProgress.class.getResource("/bambi_logo_ws.png");
		Icon logo = new ImageIcon(url);
		JLabel logoLbl = new JLabel(logo);
		JLabel versionLbl = new JLabel();
		// http://stackoverflow.com/questions/2712970/how-to-get-maven-artifact-version-at-runtime
		//String version = getClass().getPackage().getImplementationVersion();
		String version = System.getProperty("bambieditor.version");
		if(version != null) {
			versionLbl.setText("v. " + version);
		}
		
		logoLbl.setBorder(new EmptyBorder(20, 0, 0, 0));
		// versionLbl.setBackground(Color.PINK);
		// versionLbl.setOpaque(true);
		rightLogoPanel.add(versionLbl);
		leftLogoPanel.add(Box.createHorizontalStrut(125));
		rightLogoPanel.add(Box.createHorizontalStrut(125));

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		northPanel.add(leftLogoPanel, BorderLayout.WEST);
		northPanel.add(logoLbl, BorderLayout.CENTER);
		northPanel.add(rightLogoPanel, BorderLayout.EAST);

		JPanel centerPanel = new JPanel();
		// centerPanel.setBackground(Color.PINK);
		// centerPanel.setBorder(new LineBorder(Color.red));
		centerPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		JLabel line1 = new JLabel("<html><h1>Have Fun with Images!</h1></html>");
		// line1.setBackground(Color.yellow);
		// line1.setOpaque(true);
		line1.setHorizontalAlignment(JLabel.CENTER);
		line1.setBorder(new EmptyBorder(20, 0, 0, 0));
		centerPanel.add(line1, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 1;
		JLabel line2 = new JLabel();
		//line2.setText("2010 - 2013 mrazjava");
		line2.setOpaque(false);
		line2.setHorizontalAlignment(JLabel.LEFT);
		centerPanel.add(line2, c);
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.VERTICAL;
		centerPanel.add(Box.createVerticalGlue(), c);
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 3;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		JLabel line3 = new JLabel("www.bambieditor.com");
		// line3.setBackground(Color.yellow);
		// line3.setOpaque(true);
		line3.setHorizontalAlignment(JLabel.RIGHT);
		centerPanel.add(line3, c);

		top.add(northPanel, BorderLayout.NORTH);
		top.add(centerPanel, BorderLayout.CENTER);
		top.add(progressBar, BorderLayout.SOUTH);

		return top;
	}

	private void updateProgressUI(int overallPercent) {
		if (overallPercent > 0 && overallPercent < 99) {
			if (!uiCreated) {
				uiCreated = true;
				// create custom progress indicator's
				// UI only if there is more work to do,
				// meaning overallPercent > 0 and
				// < 100 this prevents flashing when
				// RIA is loaded from cache
				create();
			}
			progressBar.setValue(overallPercent);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					frame.setVisible(true);
				}
			});
		} else {
			// hide frame when overallPercent is
			// above 99
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if(frame == null) return;
					frame.setVisible(false);
					frame.dispose();
				}
			});
		}
	}

	@Override
	public void downloadFailed(URL arg0, String arg1) {
	}

	@Override
	public void progress(URL url, String version, long readSoFar, long total,
			int overallPercent) {
		updateProgressUI(overallPercent);
	}

	@Override
	public void upgradingArchive(java.net.URL url, java.lang.String version,
			int patchPercent, int overallPercent) {
		updateProgressUI(overallPercent);
	}

	@Override
	public void validating(java.net.URL url, java.lang.String version,
			long entry, long total, int overallPercent) {
		updateProgressUI(overallPercent);
	}

	public static void main(String[] args) {
		WebStartProgress test = new WebStartProgress();
		test.updateProgressUI(1);
	}
}