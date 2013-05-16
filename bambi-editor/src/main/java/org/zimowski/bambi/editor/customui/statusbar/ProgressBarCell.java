package org.zimowski.bambi.editor.customui.statusbar;

import java.awt.BorderLayout;

import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

/**
 * A status bar cell which contains {@link JProgressBar}. If instantiated via 
 * a default constructor, the progress bar is auto configured. Since getter is 
 * exposed, the default configuration can be easily customized. This cell can 
 * also be constructed with a progress bar built externally and passed at 
 * instantiation time.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class ProgressBarCell extends TextCell {

	private static final long serialVersionUID = -4870208843440166991L;
	
	//private static final Logger log = LoggerFactory.getLogger(ProgressBarCell.class);

	protected JProgressBar progressBar;
	
	/**
	 * Instantiates progress bar with value range 0 to 100 and current value 
	 * set to 0. It is configured to paint string display and the string is 
	 * left aligned. The status bar is borderless, although border effect 
	 * exists since the cell itself provides a border. This cell is managed 
	 * internally via {@link BorderLayout} therefore changing border is not 
	 * allowed.
	 */
	public ProgressBarCell() {
		super();
		progressBar = new JProgressBar(0, 100);
		progressBar.setBorderPainted(false);
		progressBar.setBorder(new EmptyBorder(0, 0, 1, 0));
		progressBar.setStringPainted(false);
		init();
	}
	
	/**
	 * Instantiates a cell with a progress bar defined externally.
	 * 
	 * @param progressBar
	 * @throws IllegalArgumentException if progressBar is null
	 */
	public ProgressBarCell(JProgressBar progressBar) {
		
		super();
		
		if(progressBar == null)
			throw new IllegalArgumentException("null progress bar not allowed");
		
		this.progressBar = progressBar;
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout()); // BoxLayout X does not respect height
		progressBar.setFont(getFont(progressBar.getFont().getName()));
		add(progressBar, BorderLayout.CENTER);
	}

	/**
	 * @return progressbar managed by this cell
	 */
	public JProgressBar getProgressBar() {
		return progressBar;
	}
}