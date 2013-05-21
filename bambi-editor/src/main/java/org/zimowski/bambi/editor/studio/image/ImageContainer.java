package org.zimowski.bambi.editor.studio.image;

import java.awt.Adjustable;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.commons.resources.cursors.CustomCursors;
import org.zimowski.bambi.editor.ViewportMouseListener;
import org.zimowski.bambi.editor.config.Configuration;
import org.zimowski.bambi.editor.config.ImageOutputConfigFacade;
import org.zimowski.bambi.editor.config.ImageOutputFormat;
import org.zimowski.bambi.editor.studio.eventbus.EventBusManager;
import org.zimowski.bambi.editor.studio.eventbus.events.ImageFilterQueueEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelLifecycleEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelLifecycleEvent.ModelPhase;
import org.zimowski.bambi.editor.studio.eventbus.events.SelectorBackgroundEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.SelectorTypeEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.SelectorVisibilityEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.SelectorVisibilityEvent.Command;
import org.zimowski.bambi.editor.studio.resources.toolbar.ToolbarIcons;

import com.google.common.eventbus.Subscribe;

/**
 * Canvas for displaying an image. Provides scrolling, clipping and preview  
 * functionality. Internals of image manipulation delegated to 
 * {@link ImageModel}.
 * 
 * @author Sun Microsystems (0.01) - initial demo release; non-functional very 
 * 	limited
 * @author Adam Zimowski (mrazjava) (1.0+) - integrated into a working 
 * 	application
 * 
 * @version 0.01 - initial demo release; part of image viewer demo app; very 
 * 	limited functionality
 * @version 0.1 - integrated into a working image upload application; added 
 * 	movable & resizable selector feature along with preview of sub image 
 *  defined by the selector bounds.
 * @version 1.0 - major improvements to the selector; Rearchitected to work more 
 * 	efficiently with the {@link ImageModel}
 */
public class ImageContainer extends ScrollableJLabel 
		implements AdjustmentListener, MouseListener, MouseMotionListener {

	private static final long serialVersionUID = 1967907657775221061L;
	
	private static final Logger log = 
			LoggerFactory.getLogger(ImageContainer.class);
	
	/**
	 * Number of pixels on each side of the edge that creates invisible buffer 
	 * detection zone to display proper resize cursor.
	 */
	public static final int EDGE_CURSOR_DETECTION = 3;
	
	public static final int DEFAULT_CANVAS_WIDTH = 320;
	
	public static final int DEFAULT_CANVAS_HEIGHT = 480;
	
	public static final int STARTING_POS_SELECTOR_X = 50;
	
	public static final int STARTING_POS_SELECTOR_Y = 50;
	
	public static final int DEFAULT_SELECTOR_WIDTH = 100;
	
	public static final int DEFAULT_SELECTOR_HEIGHT = 100;
	
	private int startingSelectorWidth = DEFAULT_SELECTOR_WIDTH;
	
	private int startingSelectorHeight = DEFAULT_SELECTOR_HEIGHT;
	
    private boolean missingPicture = false;
        
    /**
     * x-coordinate of a main reference point (left upper corner) for the 
     * selector
     */
    private int xSelectorPos = STARTING_POS_SELECTOR_X;
    
    /**
     * y-coordinate of a main reference point (left upper corner) for the 
     * selector
     */
    private int ySelectorPos = STARTING_POS_SELECTOR_Y;
    
    /**
     * used to compute selector position during movement (mouse drag). Refers 
     * to x-coordinate of left upper corner
     */
    private int xSelectorDragOffset = 0;
    
    /**
     * used to compute selector position during movement (mouse drag). Refers 
     * to y-coordinate of left upper corner
     */
    private int ySelectorDragOffset = 0;
    
    /**
     * used to compute selector position during horizontal scrolling. Refers 
     * to x-coordinate of left upper corner
     */
    private int xSelectorScrollOffset = 0;
    
    /**
     * used to compute selector position during vertical scrolling. Refers 
     * to y-coordinate of left upper corner
     */
    private int ySelectorScrollOffset = 0;
    
    private int selectorWidth = startingSelectorWidth;
    
    private int selectorHeight = startingSelectorHeight;
    
    private SelectorState selectorState = SelectorState.Still;
    
    private ImageModel imageModel;
    
    private int selectorMaxHeight;
    
    private int selectorMaxWidth;
    
    private Polygon selectorDragHandle;
    
    private float widthToHeightRatio;
    
    private int prevX, prevY;
    
    private ImageOutputConfigFacade ios;
    
    private Color jpgBgColor = Color.WHITE;
    
    private SelectorObserver selectorObserver;
    
    boolean showSelectorPosition = false;
    
    boolean showSelectorSize = false;
    
    private Image selectorCloseIcon;
    
    private boolean isSelectorVisible = true;
    
    /**
     * true if selector was reset prior to next paint event; false if it was 
     * drawn at least once. This is used when informing 
     * {@link ImageContainer#selectorObserver} of initial size within the 
     * paint event, but may have other useful purposes in the future.
     */
    private boolean selectorReset = false;
    
    private Image resizeHandIcon = CustomCursors.Drag.getImage();
    
    private BufferedImage abortIcon;
    
    /**
     * Instance interested in being notified of mouse events over display 
     * image.
     */
    private ViewportMouseListener mouseInputListener = null;
    
    private Font queueFont;
    private FontMetrics queueFontMetrics;

    /**
     * Constructs a UI view of the image, backed by the model driving editing 
     * operations. If path argument is invalid it will be gracefully handled 
     * by a backing model which will load dummy image instead. 
     * 
     * @param iConfig output config handler
     */
    public ImageContainer(ImageOutputConfigFacade iConfig) {

    	super();
    	imageModel = new ImageModel();    	
    	ios = iConfig;
    	
    	startingSelectorWidth = Math.round(
    			ios.getTargetWidth() * ios.getSelectorFactor()
    		);
    	startingSelectorHeight = Math.round(
    			ios.getTargetHeight() * ios.getSelectorFactor()
    		);

        //Let the user scroll by dragging to outside the window.
        setAutoscrolls(true); //enable synthetic drag events
        addMouseMotionListener(this);
        addMouseListener(this);
        
        try {
			selectorCloseIcon = ImageIO.read(getClass().getResource(Configuration.RESOURCE_PATH + "close32x32.png"));
			abortIcon = ImageIO.read(getClass().getResource(Configuration.RESOURCE_PATH + "cancel12x12.png"));
		} catch (IOException e) {
			log.error("could not load an icon image");
		}

        EventBusManager.getInstance().registerWithBus(this);
    }
    
    public void setSelectorObserver(SelectorObserver observer) {
    	selectorObserver = observer;
    }

    private void initSelector() {

    	if(log.isTraceEnabled()) {
			log.trace(String.format("before -> xPos: %s, yPos: %s, xScrollOffset: %s, yScrollOffset: %s, width: %s, height: %s", 
					xSelectorPos, 
					ySelectorPos,
					xSelectorScrollOffset, 
					ySelectorScrollOffset, 
					selectorWidth,
					selectorHeight));
    	}

		xSelectorDragOffset = ySelectorDragOffset = 0;

		selectorWidth = startingSelectorWidth;
		selectorHeight = startingSelectorHeight;

		if(log.isTraceEnabled()) {
			log.trace(String.format("after -> xPos: %s, yPos: %s, xScrollOffset: %s, yScrollOffset: %s, width: %s, height: %s", 
					xSelectorPos, 
					ySelectorPos,
					xSelectorScrollOffset, 
					ySelectorScrollOffset, 
					selectorWidth,
					selectorHeight));
		}

		widthToHeightRatio = (float)selectorWidth / (float)selectorHeight;
		
		log.debug("widthToHeightRatio: {}", widthToHeightRatio);
		
		Point p = getSelectorLeftUpperCorner();
		calculateSelectorDragHandle(p.x + selectorWidth, p.y + selectorHeight);
		
		selectorReset = true;
    }
    
    private void resetSelector() {

    	int width = ios.getTargetWidth();
    	int height = ios.getTargetHeight();
    	float factor = ios.getSelectorFactor();
    	
		log.debug(String.format("w: %d, h: %d, f: %f", width, height, factor));
    	
    	startingSelectorWidth = Math.round(width * factor);
    	startingSelectorHeight = Math.round(height * factor);

    	initSelector();
    	handleModelReset();
    	repaint();
    	selectorReset = true;
    }
    
    private int getPixelColorUnderMouse(MouseEvent e) {
    	// mouse co-ordinates correspond exactly to display image co-ordinates
    	try { return imageModel.getImage().getRGB(e.getX(), e.getY()); }
    	catch(NullPointerException npe) {
    		// happens when image is refreshed
    		return 0;
    	}
    	catch(ArrayIndexOutOfBoundsException ex) {
    		// may happen if mouse exited canvas too quickly;
    		return 0; // tell status bar no color available
    	}
    }

    public void mouseMoved(MouseEvent e) {
    	
    	if(mouseInputListener != null) {
    		mouseInputListener.mouseMoved(e, getPixelColorUnderMouse(e));
    	}    	

    	if(!isSelectorVisible || ios.getTargetShape() == Configuration.TARGET_SHAPE_FULL) 
    		return;
    	
    	if(isWithinSelector(e)) {
    		if(selectorDragHandle.contains(e.getX(), e.getY())) {
    			Cursor seResizeCursor = 
    					Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
    			setCursor(seResizeCursor);
    		}
    		else if(!ios.isRatioPreserved() && isOverLeftEdge(e))
    			setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
    		else if(!ios.isRatioPreserved() && isOverRightEdge(e))
    			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
    		else if(!ios.isRatioPreserved() && isOverTopEdge(e))
    			setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
    		else if(!ios.isRatioPreserved() && isOverBottomEdge(e))
    			setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
    		else
    			setCursor(CustomCursors.Grab.getCursor());
    	}
    	else if(isWithinCloseIcon(e)) {
    		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    	}
    	else {
    		setCursor(Cursor.getDefaultCursor());
    		xSelectorDragOffset = ySelectorDragOffset = 0;
    	}
    }
    
    /**
     * Convenience for {@link #isOverLeftEdge(MouseEvent, boolean)} with 
     * false passed every time.
     * 
     * @param e
     * @return
     */
    private boolean isOverLeftEdge(MouseEvent e) {
    	return isOverLeftEdge(e, false);
    }
    
    /**
     * Determines if cursor under mouse is currently over left edge of the 
     * selector. The second argument, if true, automatically sets the value 
     * of {@link #selectorState} with {@link SelectorState#DragLeftEdge}.
     *  
     * @param e
     * @param setResizingFlag
     * @return
     */
    private boolean isOverLeftEdge(MouseEvent e, boolean setResizingFlag) {
    	Point p = getSelectorLeftUpperCorner();
    	boolean result = 
    			((e.getX() >= p.x - EDGE_CURSOR_DETECTION && e.getX() <= (p.x + EDGE_CURSOR_DETECTION)) && 
    			(e.getY() >= p.y && e.getY() <= (p.y + selectorHeight)));
    	if(result && setResizingFlag) selectorState = SelectorState.DragLeftEdge;
    	return result;
    	
    }
    
    /**
     * Convenience for {@link #isOverRightEdge(MouseEvent, boolean)} with 
     * false passed every time.
     * 
     * @param e
     * @return
     */
    private boolean isOverRightEdge(MouseEvent e) {
    	return isOverRightEdge(e, false);
    }
    
    /**
     * Determines if cursor under mouse is currently over right edge of the 
     * selector. The second argument, if true, automatically sets the value 
     * of {@link #selectorState} with {@link SelectorState#DragRightEdge}.
     *  
     * @param e
     * @param setResizingFlag
     * @return
     */
    private boolean isOverRightEdge(MouseEvent e, boolean setResizingFlag) {
    	Point p = getSelectorLeftUpperCorner();
    	boolean result = 
    			((e.getX() >= (p.x + selectorWidth - EDGE_CURSOR_DETECTION) && e.getX() <= (p.x + selectorWidth + EDGE_CURSOR_DETECTION)) && 
    			(e.getY() >= p.y && e.getY() <= (p.y + selectorHeight)));
    	if(result && setResizingFlag) selectorState = SelectorState.DragRightEdge;
    	return result;
    }
    
    /**
     * Convenience for {@link #isOverTopEdge(MouseEvent, boolean)} with 
     * false passed every time.
     * 
     * @param e
     * @return
     */
    private boolean isOverTopEdge(MouseEvent e) {
    	return isOverTopEdge(e, false);
    }
    
    /**
     * Determines if cursor under mouse is currently over top edge of the 
     * selector. The second argument, if true, automatically sets the value 
     * of {@link #selectorState} with {@link SelectorState#DragTopEdge}.
     *  
     * @param e
     * @param setResizingFlag
     * @return
     */
    private boolean isOverTopEdge(MouseEvent e, boolean setResizingFlag) {
    	Point p = getSelectorLeftUpperCorner();
    	boolean result = 
    			((e.getX() >= p.x && e.getX() <= (p.x + selectorWidth)) && 
    			(e.getY() >= p.y - EDGE_CURSOR_DETECTION && e.getY() <= (p.y + EDGE_CURSOR_DETECTION)));
    	if(result && setResizingFlag) selectorState = SelectorState.DragTopEdge;
    	return result;
    }

    /**
     * Convenience for {@link #isOverBottomEdge(MouseEvent, boolean)} with 
     * false passed every time.
     * 
     * @param e
     * @return
     */
    private boolean isOverBottomEdge(MouseEvent e) {
    	return isOverBottomEdge(e, false);
    }
    
    /**
     * Determines if cursor under mouse is currently over bottom edge of the 
     * selector. The second argument, if true, automatically sets the value 
     * of {@link #selectorState} with {@link SelectorState#DragBottomEdge}.
     *  
     * @param e
     * @param setResizingFlag
     * @return
     */
    private boolean isOverBottomEdge(MouseEvent e, boolean setResizingFlag) {
    	Point p = getSelectorLeftUpperCorner();
    	boolean result = 
    			((e.getX() >= p.x && e.getX() <= (p.x + selectorWidth)) && 
    			(e.getY() >= (p.y + selectorHeight - EDGE_CURSOR_DETECTION) && e.getY() <= (p.y + selectorHeight + EDGE_CURSOR_DETECTION)));
    	if(result && setResizingFlag) selectorState = SelectorState.DragBottomEdge;
    	return result;
    }

    private boolean isWithinCloseIcon(MouseEvent e) {
    	Point p = getSelectorLeftUpperCorner();
    	p.x += selectorWidth;
    	p.y -= 15;
    	int width = selectorCloseIcon.getWidth(null);
    	int height = selectorCloseIcon.getHeight(null);
    	return ((e.getX() >= p.x && e.getX() <= (p.x + width)) && 
    			(e.getY() >= p.y && e.getY() <= (p.y + height)));
    }
    
    private boolean isWithinSelector(MouseEvent e) {
    	Point p = getSelectorLeftUpperCorner();
    	return ((e.getX() >= p.x && e.getX() <= (p.x + selectorWidth)) && 
    			(e.getY() >= p.y && e.getY() <= (p.y + selectorHeight)));
    }
    
    public void mouseDragged(MouseEvent e) {

		if(!isSelectorVisible || ios.getTargetShape() == Configuration.TARGET_SHAPE_FULL)
			return;

    	if(mouseInputListener != null) {
    		mouseInputListener.mouseDragged(e, getPixelColorUnderMouse(e));
    	}    	

    	if(xSelectorDragOffset > 0) {
    		if(isSelectorResized()) {

    			final int sign = SelectorState.DragLeftEdge.equals(selectorState) || 
    					SelectorState.DragTopEdge.equals(selectorState) ? -1 : 1;
    			int xDiff = (e.getX() - prevX) * sign;
    			int yDiff = (e.getY() - prevY) * sign;
    			
    			int newWidth = selectorWidth + xDiff;
    			int newHeight = selectorHeight + yDiff;
    			
    			if(newWidth <= 20) return;
    			if(newHeight <= 20) return;
    			
    			// make sure leftcorner + width is not extended beyond pic boundary 
    			// and if so, reduce the width; do same check for height and bottom 
    			// pic edge
    			Point corner = getSelectorLeftUpperCorner();
    			final int imageWidth = getImage().getWidth();
    			final int imageHeight = getImage().getHeight();
    			log.trace(String.format("x: %s, y: %s, s-w: %s, s-h: %s, p-w: %s, p-h: %s", 
    					corner.x, 
    					corner.y,
    					selectorWidth, 
    					selectorHeight, 
    					imageWidth, 
    					imageHeight));
    			if(corner.x + newWidth > imageWidth) return;
    			if(corner.y + newHeight > imageHeight) return;
    			
    			if(ios.isRatioPreserved()) {
	    			if(newWidth >= startingSelectorWidth && newWidth <= selectorMaxWidth) {
	    				selectorWidth = newWidth;
	    				selectorHeight = Math.round(selectorWidth / widthToHeightRatio);
	    			}
	    			if(newHeight >= startingSelectorHeight && newHeight <= selectorMaxHeight) {
	    				selectorHeight = newHeight;
	    				selectorWidth = Math.round(selectorHeight * widthToHeightRatio);
	    			}
    			}
    			else {
    				if(isSelectorResizedHorizontally()) {
    					selectorWidth = newWidth;
    					boolean xOk = e.getX() > 0;
    					if(SelectorState.DragLeftEdge.equals(selectorState) && xOk) {
    						xSelectorPos = e.getX();
    					}
    				}
    				else if(isSelectorResizedVertically()) {
    					selectorHeight = newHeight;
    					boolean yOk = e.getY() > 0;
    					if(SelectorState.DragTopEdge.equals(selectorState) && yOk) {
    						ySelectorPos = e.getY();
    					}
    				}
    				else { //  if(selectorDragHandle.contains(e.getX(), e.getY()))
	    				selectorWidth = newWidth;
	    				selectorHeight = newHeight;
    				}

    			}
    			log.trace(String.format("xSelectorDragOffset: %s, prevX: %s, prevY: %s, xDiff: %s, yDiff: %s, newWidth: %s, newHeight: %s", 
    					xSelectorDragOffset, 
    					prevX, 
    					prevY, 
    					xDiff, 
    					yDiff, 
    					newWidth, 
    					newHeight));
    			prevX = e.getX();
    			prevY = e.getY();
    		}
    		else if(isSelectorMoved()) {
    			BufferedImage image = imageModel.getImage();
    			if(image == null) return;
    			int imageWidth = image.getWidth();
    			int imageHeight = image.getHeight();
    			if((e.getX() >= 0 && e.getX() <= imageWidth) && 
    					(e.getY() >= 0 && e.getY() <= imageHeight)) {
	    			int newX = e.getX() - xSelectorDragOffset;
	    			int newY = e.getY() - ySelectorDragOffset;
	
	    			if(newX >= 0) {
	    				xSelectorPos = newX;
	    			}
	    			else {
	    				xSelectorPos = 0;
	    				xSelectorDragOffset = e.getX();
	    			}
			    	
	    			if(newY >= 0) { 
	    				ySelectorPos = newY;
	    			}
	    			else {
	    				ySelectorPos = 0;
	    				ySelectorDragOffset = e.getY();
	    			}
    			}
    		}

    		repaint();
    	}
    }
    
    /**
     * Computes position and shape (fixed) of the selector's drag handle which 
     * is used to resize it. User grabs this handle by left clicking the mouse 
     * then dragging which causes the selector to resize proportionally. Shape 
     * of this handle is always fixed. 
     * 
     * @param x x-coordinate of handle's left upper corner relative to canvas
     * @param y y-coordinate of handle's left upper corner relative to canvas
     */
    private void calculateSelectorDragHandle(int x, int y) {
    	
    	int offset = 20;
    	
    	if(ios.getTargetShape() == Configuration.TARGET_SHAPE_ELIPSE)
    		offset--;
    	
    	selectorDragHandle = new Polygon();
    	selectorDragHandle.addPoint(x - offset, y);
    	selectorDragHandle.addPoint(x, y);
    	selectorDragHandle.addPoint(x, y - offset);
    }
    
    final static BasicStroke dash1 = new BasicStroke(1.0f,
    	      BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 8.0f, new float[]{5.0f, 2.5f}, 5.0f);

    final static BasicStroke dash2 = new BasicStroke(1.0f,
  	      BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 4.0f, new float[]{5.0f, 2.5f}, 0f);

    /**
     * Paints selector and other artifacts on top of the image.
     */
    @Override
	public void paint(Graphics g) {
    	super.paint(g);
    	initializeQueueFont(g);
    	paintSelector(g);
    	if(eventQueueIcons != null) paintEventQueue(g);
    }
    
    @Subscribe
    public void onEventQueue(ImageFilterQueueEvent ev) {
    	eventQueueIcons = ev.getIcons();
    	eventQueueAborted = ev.isAborted();
    	repaint();
    }
    
    private List<ToolbarIcons> eventQueueIcons = null;
    private boolean eventQueueAborted = false;
    
    /**
     * Initializes queue font 
     * 
     * @param g
     */
    private void initializeQueueFont(final Graphics g) {
    	if(queueFont == null) {
	    	queueFont = new Font("Courier", Font.BOLD, 9);
	    	queueFontMetrics = g.getFontMetrics(queueFont);
    	}
    }
    
    private void paintEventQueue(Graphics g) {
    	
    	if(eventQueueIcons.size() == 0) return;
    	Iterator<ToolbarIcons> walker = eventQueueIcons.iterator();
    	
    	final int OFFSET_LEFT = 10;	// pixels
    	final int OFFSET_TOP = 8;	// pixels
    	final int SPACE = 5;		// pixels

    	g.setFont(queueFont);
		int type = AlphaComposite.SRC_OVER;
		AlphaComposite composite1 = AlphaComposite.getInstance(type, 0.75f);
		AlphaComposite composite2 = AlphaComposite.getInstance(type, 0.45f);
		AlphaComposite composite3 = AlphaComposite.getInstance(type, 0.85f);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setComposite(composite1);
		g2.setColor(Color.WHITE);
    	Image img = null;
    	ToolbarIcons icon = null;
    	if(walker.hasNext()) {
    		icon = walker.next();
    		img = icon.getIcon().getImage();
    	}
		int leftRectWidth = OFFSET_LEFT*2 + (img != null ? img.getWidth(null) : 0);
		g2.fillRect(xSelectorScrollOffset, ySelectorScrollOffset, leftRectWidth, 40 + queueFontMetrics.getHeight());
	    g2.setComposite(composite2);
		g2.setColor(Color.GRAY);
		g2.fillRect(leftRectWidth + xSelectorScrollOffset, ySelectorScrollOffset, getWidth(), 40 + queueFontMetrics.getHeight());
		g2.setComposite(composite3);
		int x = OFFSET_LEFT + xSelectorScrollOffset;
		int y = OFFSET_TOP + ySelectorScrollOffset + queueFontMetrics.getHeight();
		if(icon != null) {
			g.drawImage(img, x, y, null);
			g.setColor(Color.BLACK);
			if(icon.getMetaInfo() != null) g.drawString(icon.getMetaInfo(), x, y-7);
			g.setColor(Color.YELLOW);
		}
		int count = 1;

		while(walker.hasNext()) {
			icon = walker.next();
			if(icon == null) continue;
			img = icon.getIcon().getImage();
			x = OFFSET_LEFT*3 + (SPACE*count) + (img.getWidth(null)*count) + xSelectorScrollOffset;
			g2.drawImage(img, x, y, null);
			if(eventQueueAborted) {
				g.drawImage(abortIcon, x+15, y+15, null); 
			}
			if(icon.getMetaInfo() != null) {
				g.drawString(icon.getMetaInfo(), x, y-7);
			}
			count++;
		}
		g2.dispose();
    }
    
    private void paintSelector(Graphics g) {
    	
    	if(!isSelectorVisible) return;

    	Point selectorPos = getSelectorLeftUpperCorner();
    	int targetShape = ios.getTargetShape();
    	
    	if(targetShape == Configuration.TARGET_SHAPE_RECT) {
    		g.setColor(Color.RED);
    		g.drawRect(selectorPos.x, selectorPos.y, selectorWidth, selectorHeight);
    		g.setColor(Color.GRAY);
    		g.drawRect(selectorPos.x+1, selectorPos.y+1, selectorWidth-2, selectorHeight-2);
    		g.setColor(Color.RED);
    		g.drawRect(selectorPos.x+2, selectorPos.y+2, selectorWidth-4, selectorHeight-4);
    	}
    	else if(targetShape == Configuration.TARGET_SHAPE_ELIPSE) {
    		Graphics2D g2 = (Graphics2D)g.create();
    		// ellipse border
    		g2.setColor(Color.RED);
    		g2.drawOval(selectorPos.x+1, selectorPos.y+1, selectorWidth-2, selectorHeight-2);
    		g2.drawOval(selectorPos.x+2, selectorPos.y+2, selectorWidth-4, selectorHeight-4);
    		// dashed border for containing rectangle
    		g2.setColor(Color.BLACK);
        	g2.setStroke(dash1);
    		g2.drawRect(selectorPos.x, selectorPos.y, selectorWidth, selectorHeight);
    		g2.setColor(Color.GRAY);
    		g2.setStroke(dash2);
    		g2.drawRect(selectorPos.x, selectorPos.y, selectorWidth, selectorHeight);
    		// rectangular container of ellipse (excludes dashed border)
    		Rectangle2D.Double rect = new Rectangle2D.Double();
    		rect.setFrame(selectorPos.x+1D, selectorPos.y+1D, selectorWidth-1D, selectorHeight-1D);
    		// ellipse (includes borders)
    		Ellipse2D.Double ellipse = new Ellipse2D.Double();
    		ellipse.setFrame(rect);
    		Area rectArea = new Area(rect);
    		Area ellipseArea = new Area(ellipse);
    		// compute are outside of ellipse so we can draw on it
    		rectArea.subtract(ellipseArea);
    		// setup transparency
			float alpha = 0.5f;
			int type = AlphaComposite.SRC_OVER; 
			AlphaComposite composite = AlphaComposite.getInstance(type, alpha);
			g2.setComposite(composite);
			g2.setColor(ImageOutputFormat.jpg.equals(ios.getImageOutputFormat()) ? jpgBgColor : Color.DARK_GRAY);
    		g2.fill(rectArea);

    		g2.dispose();
    	}
    	else if(targetShape == Configuration.TARGET_SHAPE_FULL) {
    		int imgWidth = getImage().getWidth();
    		int imgHeight = getImage().getHeight();
    		g.setColor(Color.RED);
    		g.drawRect(0, 0, imgWidth-1, imgHeight-1);
    		g.setColor(Color.GRAY);
    		g.drawRect(1, 1, imgWidth-3, imgHeight-3);
    		g.setColor(Color.RED);
    		g.drawRect(2, 2, imgWidth-5, imgHeight-5);
    		return;
    	}
    	
    	final int x = selectorPos.x + selectorWidth;
    	final int y = selectorPos.y + selectorHeight;

    	g.setColor((isSelectorResized() ? Color.GREEN : Color.CYAN));
    	calculateSelectorDragHandle(x, y);
    	g.fillPolygon(selectorDragHandle);
    	
    	if(selectorCloseIcon != null) {
    		g.drawImage(selectorCloseIcon, selectorPos.x + selectorWidth, selectorPos.y-15, null);
    	}

    	if(isSelectorResized()) {
    		if(SelectorState.DragHandle.equals(selectorState)) {
    			g.drawImage(resizeHandIcon, x-10, y-10, null);
    		}
    		selectorObserver.selectorResized(selectorWidth, selectorHeight);
    	}
    	else {
    		selectorObserver.selectorMoved(selectorPos.x, selectorPos.y);
    	}
    	
    	if(selectorReset) {
    		selectorObserver.selectorResized(selectorWidth, selectorHeight);
    		selectorReset = false;
    	}
    	
    	// approximate 11 pixels per character
    	if(showSelectorPosition || showSelectorSize) {
    		
    		FontMetrics metrics = g.getFontMetrics();
	    	
    		String sizeText = selectorWidth + "x" + selectorHeight;
	    	Rectangle2D sizeTextRect = metrics.getStringBounds(sizeText, g);
	    	String positionText = "x: " + selectorPos.x + ", y: " + selectorPos.y;
	    	Rectangle2D positionTextRect = metrics.getStringBounds(positionText, g);
	    	
	    	int sizeTextWidth = (int)sizeTextRect.getWidth();
	    	int sizeTextHeight = (int)sizeTextRect.getHeight();
	    	int positionTextWidth = (int)positionTextRect.getWidth();
	    	int positionTextHeight = (int)positionTextRect.getHeight();
	    	
	    	int sizeTextX = (selectorPos.x+(selectorWidth/2))-(sizeTextWidth/2);
	    	int sizeTextY = selectorPos.y+(selectorHeight/2);
	    	
	    	if(showSelectorSize) {
		    	// size box: center black background
		    	g.setColor(Color.BLACK);
		    	g.fillRect(sizeTextX-10, sizeTextY-15, sizeTextWidth+20, sizeTextHeight+7);
		    	// size border: center white rectangle
		    	g.setColor(Color.WHITE);
		    	g.drawRect(sizeTextX-10, sizeTextY-15, sizeTextWidth+20, sizeTextHeight+7);
		    	// size text
		    	g.setColor(Color.GREEN);
		    	g.drawChars(sizeText.toCharArray(), 0, sizeText.length(), sizeTextX, sizeTextY);
	    	}
	    	if(showSelectorPosition) {
		    	// position box: left upper white background
		    	g.setColor(Color.WHITE);
		    	g.fillRect(selectorPos.x+1, selectorPos.y+1, positionTextWidth+20, positionTextHeight+7);
		    	// position border: left upper red rectangle
		    	g.setColor(Color.RED);
		    	g.drawRect(selectorPos.x+1, selectorPos.y+1, positionTextWidth+20, positionTextHeight+7);
		    	// position text
		    	g.setColor(Color.BLACK);
		    	g.drawChars(positionText.toCharArray(), 0, positionText.length(), selectorPos.x+10, selectorPos.y+17);
	    	}
    	}    	
    }
    
    @Override
	public Dimension getPreferredSize() {
    	
    	Dimension preferredSize = null;
        
    	if (missingPicture) {
            preferredSize = new Dimension(
            		DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT
            );
        } else {
        	preferredSize = super.getPreferredSize();
        }
        
        return preferredSize;
    }
    
    private Point getSelectorLeftUpperCorner() {

    	int xPos = xSelectorPos + xSelectorScrollOffset;
    	int yPos = ySelectorPos + ySelectorScrollOffset;
    	
    	return new Point(xPos, yPos);
    }
    
    /**
     * Clips main image to bounds defined by the selector and returns sub 
     * image.
     * 
     * @return image within the bounds of a selector
     * @throws RasterFormatException if part of a selector is out of picture 
     * 	bounds
     */
    public BufferedImage getClippedImage() throws RasterFormatException {
    	
    	Point selectorPos = getSelectorLeftUpperCorner();
    	BufferedImage buffer = getImage();
    	
    	int x, y, width, height;
    	if(ios.getTargetShape() == Configuration.TARGET_SHAPE_FULL) {
    		x = 0;
    		y = 0;
    		width = buffer.getWidth();
    		height = buffer.getHeight();
    	}
    	else {
    		x = selectorPos.x;
    		y = selectorPos.y;
    		width = selectorWidth;
    		height = selectorHeight;
    	}
    	
		BufferedImage subimage = buffer.getSubimage(x, y, width, height);
		BufferedImage result = null;
		
		boolean clip = 
				(ios.getTargetShape() == Configuration.TARGET_SHAPE_ELIPSE);

		if(ImageOutputFormat.jpg.equals(ios.getImageOutputFormat())) {
			result = EditorImageUtil.makeJpg(subimage, jpgBgColor, clip);
		}
		else
			result = EditorImageUtil.makePng(subimage, clip);

		return result;
    }

    public BufferedImage getImage() {
		return imageModel.getImage();    	
    }
    
	private void previewClip(boolean hideSelector) {
		
		Frame parent = (Frame)SwingUtilities.getWindowAncestor(this);
		
		try {
			BufferedImage previewImage = getClippedImage();
			if(hideSelector) {
				selectorObserver.selectorClosed();
			}

	        JDialog preview = 
	        		new ImagePreviewDialog(parent, "Preview", previewImage);

	        preview.setModal(true);
	        preview.setVisible(true);
		}
		catch(RasterFormatException rfe) {
			JOptionPane.showMessageDialog(
					parent,
				    "Clipping area is out of bounds.",
				    "Clipping Error",
				    JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
		if(!isSelectorVisible || ios.getTargetShape() == Configuration.TARGET_SHAPE_FULL)
			return;
		
		if(isWithinSelector(e)) {
		}
		else if(isWithinCloseIcon(e)) {
			isSelectorVisible = false;
			selectorObserver.selectorClosed();
		}
		else {
			if(e.getButton() == MouseEvent.BUTTON1) {
				previewClip(false);
			}
			else if(e.getButton() == MouseEvent.BUTTON3) {
				previewClip(true);
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if(isSelectorResized() || isSelectorMoved()) {
			Cursor dragCursor = CustomCursors.Grabbed.getCursor();
			setCursor(dragCursor);
			selectorState = SelectorState.Moving;
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if(mouseInputListener != null) {
			mouseInputListener.mouseExited(e);
		}
		setCursor(Cursor.getDefaultCursor());
	}

	@Override
	public void mousePressed(MouseEvent e) {

		if(!isSelectorVisible || ios.getTargetShape() == Configuration.TARGET_SHAPE_FULL)
			return;
		
		if(e.getButton() == MouseEvent.BUTTON1 && isWithinSelector(e)) {
			
			xSelectorDragOffset = e.getX() - xSelectorPos;
			ySelectorDragOffset = e.getY() - ySelectorPos;
			if(selectorDragHandle.contains(e.getX(), e.getY())) {
				prevX = e.getX();
				prevY = e.getY();
				selectorState = SelectorState.DragHandle;
				setCursor(CustomCursors.Blank.getCursor());
				repaint();
			}
			else if(isOverLeftEdge(e, true) || isOverRightEdge(e, true) || isOverBottomEdge(e, true) || isOverTopEdge(e, true)) {
				prevX = e.getX();
				prevY = e.getY();
				repaint();				
			}
			else {
				selectorState = SelectorState.Moving;
				Cursor grabbedCursor = CustomCursors.Grabbed.getCursor();
				setCursor(grabbedCursor);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		if(!isSelectorVisible || ios.getTargetShape() == Configuration.TARGET_SHAPE_FULL)
			return;
		
		if(e.getButton() == MouseEvent.BUTTON1) {

			if(isSelectorResized()) selectorState = SelectorState.Still;
			
			Cursor cursor;
			if(selectorDragHandle.contains(e.getX(), e.getY()))
				cursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
			else if(ios.getTargetShape() != Configuration.TARGET_SHAPE_FULL && isWithinSelector(e))
				cursor = CustomCursors.Grab.getCursor();
			else
				cursor = Cursor.getDefaultCursor();
			
			setCursor(cursor);
		}
		repaint();
	}

	/**
	 * Scroll listener reports scroll bar click events.
	 * {@inheritDoc}
	 */
	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		
		Adjustable a = e.getAdjustable();
		int type = e.getAdjustmentType();
		int val = a.getValue();

		if(!e.getValueIsAdjusting()) {
			log.trace("t: {}, v: {}", type, val);
		}
		
		if(a.getOrientation() == Adjustable.HORIZONTAL) {
			xSelectorScrollOffset = val;
		}
		else {
			ySelectorScrollOffset = val;
		}
	}

	public void setSelectorVisible(boolean isSelectorVisible) {
		this.isSelectorVisible = isSelectorVisible;
		repaint();
	}

	@Subscribe
	public void onModelChanged(ModelLifecycleEvent ev) {
		ModelPhase type = ev.getPhase();
		switch(type) {
		case AfterChange:
			BufferedImage img = imageModel.getImage();
			if(img != null) setIcon(new ImageIcon(img));
			break;
		case Reset:
			handleModelReset();
			break;
		case Initialized:
			resetSelector();
			//BufferedImage img = imageModel.getImage();
			//selectorMaxHeight = img.getHeight() - 30;
			//selectorMaxWidth = img.getWidth() - 30;
		default:
		}
	}

	public void handleModelReset() {
		BufferedImage img = imageModel.getImage();
    	setIcon(new ImageIcon(img));
		selectorMaxHeight = img.getHeight() - 30;
		selectorMaxWidth = img.getWidth() - 30;
	}

	public void setMouseInputListener(ViewportMouseListener mouseInputListener) {
		this.mouseInputListener = mouseInputListener;
	}
	
	@Subscribe
	public void onSelectorChange(SelectorTypeEvent ev) {
		resetSelector();
	}
	
	@Subscribe
	public void onSelectorBackgroundColor(SelectorBackgroundEvent ev) {
		jpgBgColor = ev.getBackgroundColor();
		repaint();
	}

	@Subscribe
	public void onShowSelector(SelectorVisibilityEvent ev) {
		
		Command cmd = ev.getCommand();
		boolean visible = ev.getVisibility();
		
		switch(cmd) {
		case ShowSelector:
			setSelectorVisible(visible);
			break;
		case ShowPosition:
			showSelectorPosition = visible;
			repaint();
			break;
		case ShowSize:
			showSelectorSize = visible;
			repaint();
		}
	}
	
	private boolean isSelectorResized() {
		return selectorState != null && 
				!SelectorState.Still.equals(selectorState) && 
				!SelectorState.Moving.equals(selectorState);
	}
	
	private boolean isSelectorMoved() {
		return selectorState != null && SelectorState.Moving.equals(selectorState);
	}
	
	private boolean isSelectorResizedHorizontally() {
		return isSelectorResized() && (
				SelectorState.DragLeftEdge.equals(selectorState) || 
				SelectorState.DragRightEdge.equals(selectorState));
	}
	
	private boolean isSelectorResizedVertically() {
		return isSelectorResized() && (
				SelectorState.DragTopEdge.equals(selectorState) || 
				SelectorState.DragBottomEdge.equals(selectorState));		
	}
	
	/**
	 * Defines possible ways in which selector position can be changed.
	 * 
	 * @author Adam Zimowski (mrazjava)
	 */
	enum SelectorState {
		Still, 
		Moving, 
		DragRightEdge, 
		DragLeftEdge, 
		DragTopEdge, 
		DragBottomEdge, 
		DragHandle;
	}
}