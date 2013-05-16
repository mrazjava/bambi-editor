package org.zimowski.bambi.editor.customui.statusbar;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.zimowski.bambi.editor.studio.Editor.EditorView;
import org.zimowski.bambi.editor.studio.cam.VideoDevice;

/**
 * A status bar that reports various info. It is meant to be docked at the 
 * bottom of the window. This implementation is pre-configured. Future 
 * enhancement to this class should make this a generic container.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class StatusBar extends JPanel {

	private static final long serialVersionUID = -8742086939984443083L;
	
	/**
	 * Shows image format selected for the final output.
	 */
	private TextCell outputFormatCell;
	
	/**
	 * Shows angle of rotation applied to the image.
	 */
	private TextCell angleCell;
	
	/**
	 * Generic info cell to display real time information pertaining to the 
	 * moment in time. At all other times, it default to display location of 
	 * image being currently edited.
	 */
	private ImageTaskCell uploadCell;
	
	/**
	 * Shows position of the selector on the image canvas. This value changes 
	 * all the time as user moves the selector by dragging the mouse.
	 */
	private CheckboxCell selectorPositionCell;
	
	/**
	 * Shows rectangular size of the selector. This value changes as user 
	 * resizes the selector. If the selector is oval, the value still reports 
	 * dimensions of the enclosing rectangle.
	 */
	private CheckboxCell selectorSizeCell;
	
	/**
	 * Displays red, green and blue intensities from a given pixel's of the 
	 * image. 
	 */
	private RgbCell rgbCell;
	
	/**
	 * Displays frames per second rendered for video and live stream; the 
	 * checkbox allows to display more fps stats on screen
	 */
	private CheckboxCell fpsCell;
	
	/**
	 * Displays video device currently streaming frames and allows to select 
	 * other devices if available.
	 */
	private DropdownCell<VideoDevice> videoDeviceCell;
	
	/**
	 * Height of this status bar.
	 */
	private int preferredHeight = 20;

	
	public StatusBar() {
		init();
	}
	
	public StatusBar(int statusBarHeight) {
		
		if(statusBarHeight <= 0) 
			throw new IllegalArgumentException("Height must be a positive integer!");
		
		this.preferredHeight = statusBarHeight;
		init();
	}
	
	/**
	 * Builds status bar components
	 */
	private void init() {
		outputFormatCell = new TextCell();
		angleCell = new TextCell();
		uploadCell = new ImageTaskCell();
		videoDeviceCell = new DropdownCell<VideoDevice>();
		selectorPositionCell = new CheckboxCell();
		selectorSizeCell = new CheckboxCell();
		rgbCell = new RgbCell();
		fpsCell = new CheckboxCell("0.0");

		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(0, preferredHeight));
		
		JPanel west = new JPanel();
		JPanel center = new JPanel();
		JPanel east = new JPanel();
		
		west.setLayout(new BoxLayout(west, BoxLayout.X_AXIS));
		center.setLayout(new BoxLayout(center, BoxLayout.X_AXIS));
		east.setLayout(new BoxLayout(east, BoxLayout.X_AXIS));

		outputFormatCell.setPreferredSize(new Dimension(30, preferredHeight));
		angleCell.setPreferredSize(new Dimension(40, preferredHeight));
		west.add(outputFormatCell);
		west.add(angleCell);
		west.add(fpsCell);
		center.add(uploadCell);
		center.add(videoDeviceCell);
		
		selectorPositionCell.setPreferredSize(new Dimension(110, preferredHeight));
		selectorSizeCell.setPreferredSize(new Dimension(95, preferredHeight));
		rgbCell.setPreferredSize(new Dimension(180, preferredHeight));
		fpsCell.setPreferredSize(new Dimension(100, preferredHeight));
		
		east.add(selectorPositionCell);
		east.add(selectorSizeCell);
		east.add(rgbCell);
		
		add(west, BorderLayout.WEST);
		add(center, BorderLayout.CENTER);
		add(east, BorderLayout.EAST);
	}
	
	/**
	 * Switches status bar to requested view, that is old view cells are 
	 * hidden and new view cells are shown. If view argument is null nothing 
	 * is done.
	 * 
	 * @param view requested view, null safe
	 */
	public void setView(EditorView view) {
		boolean wel = EditorView.Welcome.equals(view);
		boolean pic = EditorView.Picture.equals(view);
		boolean cam = EditorView.WebCam.equals(view);
		
		outputFormatCell.setVisible(pic);
		angleCell.setVisible(pic);
		uploadCell.setVisible(pic || wel);
		videoDeviceCell.setVisible(cam);
		selectorPositionCell.setVisible(pic);
		selectorSizeCell.setVisible(pic);
		rgbCell.setVisible(pic || cam);
		fpsCell.setVisible(cam);
		
		rgbCell.getPixelCell().setText(null);
		rgbCell.setPixelColor(0);
		rgbCell.resetColor();
		rgbCell.repaint();
	}

	public TextCell getOutputFormatCell() {
		return outputFormatCell;
	}
	
	public TextCell getAngleCell() {
		return angleCell;
	}

	public ImageTaskCell getUploadCell() {
		return uploadCell;
	}

	public CheckboxCell getSelectorPositionCell() {
		return selectorPositionCell;
	}

	public CheckboxCell getSelectorSizeCell() {
		return selectorSizeCell;
	}
	
	public RgbCell getRgbCell() {
		return rgbCell;
	}
	
	public CheckboxCell getFpsCell() {
		return fpsCell;
	}
	
	public DropdownCell<VideoDevice> getVideoDeviceCell() {
		return videoDeviceCell;
	}
}