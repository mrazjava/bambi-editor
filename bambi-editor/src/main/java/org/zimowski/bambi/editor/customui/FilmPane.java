package org.zimowski.bambi.editor.customui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Rotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.studio.eventbus.EventBusManager;
import org.zimowski.bambi.editor.studio.eventbus.events.CamPictureTakenEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ImageLoadEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelLifecycleEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelLifecycleEvent.ModelPhase;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelResetRequestEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ThumbAddEvent;
import org.zimowski.bambi.editor.studio.image.EditorImageUtil;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Horizontal film-like list that displays thumb images. It is capable of 
 * handling various mouse operations such as selecting a film slot, responding 
 * to hover events and removing individual slot items.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class FilmPane extends JComponent implements MouseMotionListener, MouseListener {

	private static final long serialVersionUID = -8617366274843184051L;

	private static final Logger log = LoggerFactory.getLogger(FilmPane.class);
	
	private static final String ACTIONMAPKEY_DELETE = "del";

	private LinkedList<FilmThumb> thumbList;

	private BufferedImage[] film;
	
	/**
	 * in-memory cache; not used directly
	 */
	private BufferedImage deleteIconUnselected;
	
	/**
	 * in-memory cache; not used directly
	 */
	private BufferedImage deleteIconSelected;
	
	/**
	 * delete thumb icon to be drawn as needed; computed depending on mouse 
	 * position, either selected or unselected.
	 */
	private BufferedImage deleteIcon;
	
	/**
	 * x-coordinate of the left upper corner of the close icon
	 */
	private static final int CLOSE_X_POS = 22;
	
	/**
	 * y-coordinate of the left upper corner of the close icon
	 */
	private static final int CLOSE_Y_POS = 38;

	/**
	 * selected film icon even when control is out of focus
	 */
	private static final int IMG_FILM_SELECTED = 0;
	
	/**
	 * selected film icon with mouse hovering over it
	 */
	private static final int IMG_FILM_SELECTED_HOVER = 1;
	
	/**
	 * unselected film icon including when control is out of focus
	 */
	private static final int IMG_FILM_UNSELECTED_NOHOVER = 2;
	
	/**
	 * unselected film icon with mouse hovering over it
	 */
	private static final int IMG_FILM_UNSELECTED_HOVER = 3;
	
	private int hoverThumb = 0;
	
	private int selectedThumb = 0;
	
	/**
	 * used for naming temporary files
	 */
	private SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
	
	private Action visualDelete = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			showCloseIcon = !showCloseIcon;
			repaint();
		}
	};
	
	private boolean showCloseIcon = false;
	
	private boolean manageVisibility = true;


	public FilmPane() {
		thumbList = new LinkedList<FilmThumb>();
		film = getFilmIcons();
		int height = film[IMG_FILM_UNSELECTED_NOHOVER].getHeight();
		setPreferredSize(new Dimension(0, height));
		addMouseMotionListener(this);
		addMouseListener(this);
		deleteIconUnselected = EditorImageUtil.getLocalIcon("redx25x30_unselected.png");
		deleteIconSelected = EditorImageUtil.getLocalIcon("redx25x30_selected.png");
		setFocusable(true);
		getInputMap().put(KeyStroke.getKeyStroke("SPACE"), ACTIONMAPKEY_DELETE);
		getInputMap().put(KeyStroke.getKeyStroke("released SPACE"), ACTIONMAPKEY_DELETE);
		getActionMap().put(ACTIONMAPKEY_DELETE, visualDelete);
		EventBusManager.getInstance().registerWithBus(this);
	}
	
	public int getThumbCount() {
		return thumbList.size();
	}

	/**
	 * Adds thumbnail scaled image to the film. The source image can be passed 
	 * if the image had already been loaded elsewhere (avoid read IO again). If 
	 * source is null, image is loaded using provided path argument. This 
	 * method could be lengthy therefore it is highly recommended the caller 
	 * invokes it off the EDT, especially if passing null for source.
	 * 
	 * @param source already loaded source to be reused; can be null
	 * @param path path the image represented by this thumb
	 * @param selected true if this new thumb should be immediately selected
	 * @see SwingUtilities#invokeLater(Runnable)
	 */
	private void addThumb(BufferedImage source, String path, boolean selected) {
		try {
			FilmThumb filmThumb = new FilmThumb();
			filmThumb.file = new File(path);
			BufferedImage image; 
			if(source != null) {
				image = source;
			}
			else {
				image = ImageIO.read(filmThumb.file);
			}
			int w = image.getWidth();
			int h = image.getHeight();
			boolean rotate = image.getHeight() > image.getWidth();
			double ratio = (double) Math.max(w, h) / (double) Math.min(w, h);
			log.debug("path: {}, ratio: {}", path, ratio);
			log.debug("source width: {}, height: {}", image.getWidth(), image.getHeight());

			BufferedImage thumb = null;
			if (ratio > 1.5) {
				thumb = Scalr.resize(image, 60);
			} else {
				int width = rotate ? 40 : 60;
				int height = rotate ? 60 : 40;
				thumb = Scalr.resize(image, Scalr.Mode.FIT_EXACT, width, height);
			}

			if (rotate) {
				thumb = Scalr.rotate(thumb, Rotation.CW_90);
			}
			filmThumb.thumb = thumb;
			thumbList.addFirst(filmThumb);
			if(selected) 
				selectedThumb = 1;
			else
				selectedThumb++;
			
			int width = film[IMG_FILM_SELECTED].getWidth() * thumbList.size();
			int height = film[IMG_FILM_SELECTED].getHeight();
			setPreferredSize(new Dimension(width, height));
			repaint();

		} catch (IOException e) {
			log.error("could not add thumb [{}]; {}", path, e.getMessage());
		}
		if(manageVisibility) setVisible(thumbList.size() > 0);
	}
	
	@Subscribe
	public void onAddThumb(ThumbAddEvent event) {
		addThumb(
				event.getThumbSource(), 
				event.getThumbFile().getAbsolutePath(), 
				event.isSelected());
	}
	
	@Subscribe
	public void onPictureTaken(final CamPictureTakenEvent ev) {
		String tmpDir = System.getProperty("java.io.tmpdir");
		String tmpFile = "bambi_campic_" + df.format(ev.getTimeTaken()) + ".jpg";
		final File file = new File(tmpDir + "/" + tmpFile);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					ImageIO.write(ev.getPicture(), "jpg", file);
				} catch (IOException e) {
					log.debug("could not save picture to {} - {}", 
							file.getAbsolutePath(), 
							e.getMessage());
				}				
			}
		});
		addThumb(ev.getPicture(), file.getAbsolutePath(), false);
	}
	
	/**
	 * Indicates if model is initialized with an image on display. We need to know 
	 * this because with only one thumb in the film pane we want to load the image 
	 * rather than reset the model if model has no image (we can't assume model has 
	 * an image).
	 */
	private boolean modelInitialized = false;
	
	@Subscribe
	public void onModelLifecycle(ModelLifecycleEvent ev) {
		ModelPhase phase = ev.getPhase();
		if(ModelPhase.AfterChange.equals(phase)) {
			modelInitialized = (ev.getImage() != null);
			setCursor(Cursor.getDefaultCursor());
		}
	}
	
	/**
	 * Computes left upper corner of the close icon used to delete a film slot.
	 * 
	 * @param xPos x-coordinate of the mouse on film
	 * @return left upper corner of a close icon currently rendered
	 */
	private Point getCloseLeftUpperCorner(int xPos) {
		int x = (getSlotWidth() * (getThumbNoFromMouseX(xPos)-1)) + CLOSE_X_POS;
		Point p = new Point(x, CLOSE_Y_POS);
		return p;
	}
	
	/**
	 * Determines if current mouse position in the film list (specifically a 
	 * film slot) is over a close icon.
	 * 
	 * @param xPos
	 * @param yPos
	 * @return
	 */
	private boolean isOverClose(int xPos, int yPos) {
		Point p = getCloseLeftUpperCorner(xPos);
		boolean isit = (xPos > p.x && xPos < (p.x + deleteIconUnselected.getWidth())) && 
			(yPos > p.y && yPos < (p.y + deleteIconUnselected.getHeight()));
		//log.debug("x: " + x + ", y: " + y + ", xPos: " + xPos + ", yPos: " + yPos);
		return isit;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int x = 0;
		int tx = 5;
		int ty = 12;
		for (int i = 0; i < thumbList.size(); i++) {
			int thumbNo = i + 1;
			BufferedImage slot;
			if(thumbNo == selectedThumb && thumbNo == hoverThumb) {
				slot = film[IMG_FILM_SELECTED_HOVER];
			}
			else if(thumbNo == hoverThumb && thumbNo != selectedThumb) {
				slot = film[IMG_FILM_UNSELECTED_HOVER];
			}
			else if(thumbNo != hoverThumb && thumbNo == selectedThumb) {
				slot = film[IMG_FILM_SELECTED];
			}
			else {
				slot = film[IMG_FILM_UNSELECTED_NOHOVER];
			}
			g.drawImage(slot, x, 0, null);
			g.drawImage(thumbList.get(i).thumb, tx + x, ty, null);

			if(showCloseIcon && thumbNo == hoverThumb) {
				BufferedImage img = deleteIcon != null ? deleteIcon : deleteIconUnselected;
				g.drawImage(img, x + CLOSE_X_POS, CLOSE_Y_POS, null);
			}
			x += getSlotWidth();
		}
	}

	private BufferedImage[] getFilmIcons() {
		String paths[] = {
				"film_selected.jpeg",
				"film_selected_nohover.jpeg",
				"film_unselected_hover.jpeg",
				"film_unselected_nohover.jpeg"
		};
		BufferedImage[] icons = new BufferedImage[paths.length];
		for(int x=0; x<paths.length; x++) {
			icons[x] = EditorImageUtil.getLocalIcon(paths[x]);
			if(icons[x] == null) continue;
			icons[x] = Scalr.resize(icons[x], 70);
		}
		
		return icons;
	}

	/**
	 * Computes width of a single film slot, that is thumb container. This is 
	 * not the width of the thumb image inside of the slot, rather entire slot 
	 * as if you had cut it out of a film.
	 * 
	 * @return width of the individual film slot
	 */
	private int getSlotWidth() {
		// could use any of the four slot icons for this
		return film[IMG_FILM_UNSELECTED_NOHOVER].getWidth();
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
	}
	
	private int getThumbNoFromMouseX(int xPos) {
		return (xPos / getSlotWidth()) + 1;
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		setCursor(e.getX());
		int thumbNo = getThumbNoFromMouseX(e.getX());
		if(hoverThumb != thumbNo) {
			hoverThumb = thumbNo;
			repaint();
		}
		//boolean selectedCheck = (thumbNo == selectedThumb);
		//if(selectedCheck) {
		//	showCloseIcon = showCloseIcon && (!selectedCheck || (selectedCheck && thumbs.size() > 1));
		//}
		deleteIcon = isOverClose(e.getX(), e.getY()) ? deleteIconSelected : deleteIconUnselected;
		Point p = getCloseLeftUpperCorner(e.getX());
		repaint(p.x-2, p.y-2, deleteIconUnselected.getWidth()+5, deleteIconUnselected.getHeight()+5);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() != MouseEvent.BUTTON1) return;
		int thumbNo = (e.getX() / getSlotWidth()) + 1;
		if(thumbNo > thumbList.size()) return;
		if(thumbNo == selectedThumb && modelInitialized) {
			if(showCloseIcon && isOverClose(e.getX(), e.getY()))
				deleteSlot(thumbNo-1);
			else
				refreshImage();
		}
		else if(showCloseIcon) {
			if(isOverClose(e.getX(), e.getY())) {
				deleteSlot(thumbNo-1);
			}
		}
		else {
			// user selected different image from film
			selectedThumb = thumbNo;
			loadImage(thumbNo-1);			
		}
	}
	
	private void loadImage(int index) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		File f = thumbList.get(index).file;
		ImageLoadEvent ev = new ImageLoadEvent(f);
		ev.setLoadThumb(false);
		getBus().post(ev);		
	}
	
	private void refreshImage() {
		getBus().post(new ModelResetRequestEvent());
	}
	
	/**
	 * Determines if mouse is over the film (regardless of slot number).
	 * 
	 * @param xPos x-coordinate of the mouse
	 * @return true if mouse is over film, false if it is over unfilled empty 
	 * 	space in the component
	 */
	private boolean isOverFilm(int xPos) {
		return (double)xPos / (double)(getSlotWidth() * thumbList.size()) <= 1.0;
	}
	
	private void setCursor(int xPos) {
		if(isOverFilm(xPos)) {
			if(getCursor().getType() != Cursor.HAND_CURSOR)
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		else if(getCursor().getType() != Cursor.DEFAULT_CURSOR) {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(thumbList.size() < 2) return;
		boolean rightClick = e.getButton() == MouseEvent.BUTTON3;
		final int x = e.getX();
		final int y = e.getY();
		if(rightClick && isOverFilm(x)) {
			int thumbNo = getThumbNoFromMouseX(e.getX());
			DeleteSlotMenu m = new DeleteSlotMenu(thumbNo);
			m.show(this, x, y);
		}
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		requestFocusInWindow();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		hoverThumb = 0;
		showCloseIcon = false;
		repaint();
	}
	
	/**
	 * Deletes slot indicated by container position taking into account all 
	 * dependent configuration (such as re-configuring selected slot if 
	 * necessary, etc). This method does not repaint the container.
	 * 
	 * @param index zero based index indicating slot within the vector to be 
	 * 	deleted
	 */
	private void deleteSlot(int index) {
		thumbList.remove(index);
		if(thumbList.size() == 0) {
			ImageLoadEvent ev = new ImageLoadEvent(null);
			getBus().post(ev);
		}
		else if(index+1 <= selectedThumb) {
			if(thumbList.size() == index) {
				index--;
				selectedThumb--;
			}
			else {
				ImageLoadEvent ev = new ImageLoadEvent(thumbList.get(index).file);
				getBus().post(ev);
			}
		}
	}
	
	/**
	 * A simple data struct representing thumb inside a film slot.
	 * 
	 * @author Adam Zimowski (mrazjava)
	 */
	class FilmThumb {
		BufferedImage thumb;
		File file;
	}
	
	/**
	 * Delete film slot popup menu.
	 * 
	 * @author Adam Zimowski (mrazjava)
	 */
	class DeleteSlotMenu extends JPopupMenu {
		private static final long serialVersionUID = 1L;
		private JMenuItem deleteItem;
		public DeleteSlotMenu(final int slotNumber) {
			String txt = "Delete Thumb #" + slotNumber;
			deleteItem = new JMenuItem(txt, new ImageIcon(Scalr.resize(deleteIconSelected, 16)));
			Font f = new Font(deleteItem.getFont().getName(), Font.PLAIN, 10);
			deleteItem.setFont(f);
			deleteItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteSlot(slotNumber-1);
					repaint();
				}
			});
			add(deleteItem);
		}
	}

	public boolean isManageVisibility() {
		return manageVisibility;
	}

	/**
	 * Set to true if this component should automatically manage its visibility. 
	 * The management is based on the content. If the film pane is empty, it 
	 * hides itself (visible=false), otherwise it makes itself visible. There 
	 * is little point to showing the film pane if there is not a single film 
	 * slot in it. If this value is false, film pane adhers to its visible 
	 * property and it must be managed from the outside. The default is true.
	 * 
	 * @param manageVisibility
	 */
	public void setManageVisibility(boolean manageVisibility) {
		this.manageVisibility = manageVisibility;
	}	
	
	/**
	 * Convenience function to look up event bus.
	 * 
	 * @return event bus for this component's window context
	 */
	private EventBus getBus() {
		return EventBusManager.getInstance().getBus();
	}
}