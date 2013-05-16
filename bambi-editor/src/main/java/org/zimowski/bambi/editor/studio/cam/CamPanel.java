package org.zimowski.bambi.editor.studio.cam;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.ViewportMouseListener;
import org.zimowski.bambi.editor.config.Configuration;
import org.zimowski.bambi.editor.studio.eventbus.EventBusManager;
import org.zimowski.bambi.editor.studio.eventbus.events.CamFilterEvent;

import com.google.common.eventbus.Subscribe;

/**
 * Container optimized for displaying web cam video. Managed internally by 
 * {@link BorderLayout}, therefore {@link #setLayout(LayoutManager)} in this 
 * implementation is not allowed.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class CamPanel extends JPanel implements ComponentListener {

	private static final long serialVersionUID = -9154210091604628187L;
	
	private static final Logger log = LoggerFactory.getLogger(CamPanel.class);
    
	final private VideoFrameComponent fc;

	private VideoCapture vc;
	
	private VideoDisplay<MBFImage> display;
	
	/**
	 * resolution width device should use when capturing stream
	 */
	private int captureWidth = 1024;
	
	/**
	 * resolution height device should use when capturing stream
	 */
	private int captureHeight = 768;
	
	private Thread displayThread;
	
	/**
	 * Indicates if this is first webcam session since the program was started. 
	 * This is handy from performance standpoind since initializing cam the 
	 * very first time takes longer than on subsequent runs as native code 
	 * has to be loaded, etc. True if cam initializes first time, false on all 
	 * subsequent session initializations.
	 */
	private boolean virginInit = true;

    /**
     * Indicates if web cam is in the initialization state. This flag must be 
     * set from external source via {@link #setCamInitializing()} as a result 
     * of user action.
     */
    private boolean camInitializing = false;
    
    /**
     * Indicates that web cam initialization did not succeed.
     */
    private boolean camInitializationFailed = false;
    
    /**
     * Blinker flag; used only during cam initialization and indicates the 
     * blink state of cam init image.
     */
    private boolean showCamInitIcon = false;
    
    /**
     * Image to display (blink) during cam initialization.
     */
    private BufferedImage camInitImg = null;
    
    /**
     * Image to display when cam initialization failed, most likely due to 
     * lack of device on the host system. Always null; only loaded when needed.
     */
    private BufferedImage noWebCamImg = null;
    
    /**
     * Message to display along with {@link #noWebCamImg}. If null, error 
     * message is not displayed. May contain exception message or a custom, 
     * more user friendly text. Not affected by {@link #drawMessages}.
     */
    private String noWebCamMsg = null;
    
    /**
     * Allows to draw info messages along with the blinking icon.
     */
    private boolean drawMessages = false;

	/**
	 * Initialization blinker
	 */
	private Timer timer = new Timer(500, new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			showCamInitIcon = !showCamInitIcon;
			if(showCamInitIcon)
				timer.setDelay(200);
			else
				timer.setDelay(550);
			
			if(camInitImg != null) repaint(computeRepaintRect());
		}
		
	    /**
	     * Calculates area that should be repainted during blinking operation. This 
	     * is for efficiency reasons so we avoid repainting entire canvas.
	     * 
	     * @return rectangle which should be repainted
	     */
	    private Rectangle computeRepaintRect() {
	    	Point p = computeWaitImgLeftUpperCorner(camInitImg);
	    	Dimension d = new Dimension(camInitImg.getWidth(), camInitImg.getHeight()-10);
	    	return new Rectangle(p, d);
	    }
	});

    
    public CamPanel() {
		super(new BorderLayout());
		setBorder(new EtchedBorder());
		fc = new VideoFrameComponent();
		fc.setAllowZoom(false);
		fc.setAllowPanning(false);
		fc.setTransparencyGrid(false);
		fc.setShowPixelColours(false);
		fc.setShowXYPosition(false);
		fc.setAutoFit(true);
		fc.setAutoResize(false);
		fc.setAutoPack(false);
		fc.setKeepAspect(false);
		fc.setTransparencyGrid(false);
		add(fc, BorderLayout.CENTER);
		addComponentListener(this);
		
		EventBusManager.getInstance().registerWithBus(this);
	}

	/**
     * To be invoked just before web cam is initialized. Performs painting of 
     * wait message.
     */
    private void setCamInitializing() {
    	if(camInitImg == null) {
			try {
				URL loc = getClass().getResource(Configuration.RESOURCE_PATH + "webcam256x256.png");
				if(loc == null) return;
				camInitImg = ImageIO.read(loc);
			} catch (IOException e) { log.error(e.getMessage()); }
    	}
    	camInitializing = true;
    	timer.start();
    }
    
    /**
     * To be invoked when cam has been initialized. Turns off painting of a 
     * wait message.
     */
    private void setCamInitialized() {
    	camInitializing = false;
    	camInitImg = null;
    	timer.stop();
    	repaint();
    	virginInit = false;
    }
    
    private void setCamInitializationFailed(VideoCaptureException ex) {
		try {
			URL loc = getClass().getResource(Configuration.RESOURCE_PATH + "nowebcam256x256.png");
			if(loc == null) return;
			noWebCamImg = ImageIO.read(loc);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
    	if(ex != null) noWebCamMsg = ex.getMessage();
    	camInitializationFailed = true;
    	camInitializing = false;
    	timer.stop();
    	repaint();
    }
    
    public boolean isCamInitializing() {
    	return camInitializing;
    }
    
    private void drawCamIcon(Graphics2D g2) {
    	drawInfoIcon(g2, camInitImg);
    }
    
    private void drawNoCamIcon(Graphics2D g2) {
    	drawInfoIcon(g2, noWebCamImg);
    }
    
    private void drawInfoIcon(Graphics2D g2, BufferedImage icon) {
    	if(icon != null) {
			Point p = computeWaitImgLeftUpperCorner(icon);
			g2.drawImage(icon, p.x,  p.y, null);
    	}
    	else {
    		log.warn("null icon");
    	}
    }
    
    /**
     * @param g raw graphic context
     */
    private void drawCamWaitText(Graphics g) {
    	
		String msg = (!virginInit ? "Resuming WebCam ..." : "Opening WebCam ...");
		int fontSize = (camInitImg == null ? 40 : 18);
		drawInfoText(g, msg, fontSize, !virginInit);
    }
    
    private void drawNoCamText(Graphics g) {
    	int fontSize = (camInitImg == null ? 40 : 18);
    	if(noWebCamMsg != null) drawInfoText(g, noWebCamMsg, fontSize, true);
    }
    
    private void drawInfoText(Graphics g, String text, int fontSize, boolean drawBox) {
    	
		Font font = new Font("SansSerif", Font.PLAIN, fontSize);
		FontMetrics fm = getFontMetrics(font);
		Rectangle2D fontRect = fm.getStringBounds(text, g);

		Point p = computeInfoTextLeftUpperCorner(fontRect);
		
		int width = (int)fontRect.getWidth();
		int height = (int)fontRect.getHeight();
		
		int widthExtra = (int)(width * 0.3);

		int txtX = p.x;
		int txtY = p.y - 5;

		int x = txtX-(widthExtra/2);
		int y = txtY-height;
		int w = width + widthExtra;
		int h = (int)(height * 1.3);
		
		if(drawBox) {
			// Draw a box
			g.setColor(new Color(0, 0, 0, 0.5f));
			g.fillRect(x, y, w, h);
			// Draw border
			g.setColor(Color.RED);
			g.drawRect(x-1, y-1, w+1, h+1);
			g.setColor(Color.WHITE);
		}
		else {
			txtY -= 20;
			g.setColor(Color.BLACK);
		}
		g.setFont(font);
		g.drawString(text, txtX, txtY);    	
    }
    
    /**
     * Computes x,y coordinates relative to panel's viewport which represents  
     * left upper corner of info text to be drawn possibly along a blinking 
     * init icon (if enabled). The text is positioned either below the icon 
     * (if enabled) or higher in the screen if icon is not shown.
     * 
     * @param fontRect rectangle representing boundaries of the text
     * @return
     */
    private Point computeInfoTextLeftUpperCorner(Rectangle2D fontRect) {
		int fontX = (int)(((double)getWidth() - fontRect.getWidth()) / 2);
		int fontY = getHeight()/2;
		
		if(camInitImg != null) {
			fontY += (camInitImg.getHeight()/2) + 30;
		}

		return new Point(fontX, fontY);
    }

    /**
     * Computes left upper corner relative to canvas (this panel) where image 
     * should be drawn from such that it is astetically centered. This does 
     * not mean the image will be perfectly centered, as it may be visually 
     * more appealing to shift it few pixels in either direction. If image is 
     * null, a point exactly in the center of the screen is returned.
     * 
     * @param img the image whose left upper corner position should be computed
     * @return
     */
    private Point computeWaitImgLeftUpperCorner(BufferedImage img) {
    	
    	int width = 0;
    	int height = 0;
    	
    	if(img != null) {
    		width = img.getWidth();
    		height = img.getHeight() + 25;
    	}
    	
    	int x = (getWidth()/2) - width/2;
    	int y = (getHeight()/2) - height/2 - 15;
    	
    	return new Point(x, y);
    }

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if(camInitializing || camInitializationFailed) {
			if(virginInit) {
				if(noiseImage == null) startNoise();
				g.drawImage(noiseImage, 0, 0, null);
			}
			
			Graphics2D g2 = (Graphics2D)g.create();

			float alpha = virginInit ? 0.8f : 0.5f;
			int type = AlphaComposite.SRC_OVER; 
			AlphaComposite composite = AlphaComposite.getInstance(type, alpha);
		    g2.setComposite(composite);

			if(showCamInitIcon) {
				drawCamIcon(g2);
			}
			if(drawMessages) {
				drawCamWaitText(g);
			}
			
			if(camInitializationFailed) {
				drawNoCamIcon(g2);
				drawNoCamText(g);
			}
			g2.dispose();
		}
	}
	
	/**
	 * noise content
	 */
	private byte[] noiseData = null;
	
	/**
	 * noise frame for display
	 */
	private BufferedImage noiseImage;
	
	/**
	 * generates random noise bytes
	 */
	private Random random;
	
	/**
	 * Paints animated static noise effect similar to that of analog tv. This 
	 * is used when cam is being initialized for the first time, which is when 
	 * delay is the longest.
	 */
	private void startNoise() {
		random = new Random();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							initializeNoise(getWidth(), getHeight());
						}
					});
				}
				catch (Exception e1) { log.error("", e1); }
				
				while (true) {
					random.nextBytes(noiseData);
					repaint();
					try {
						Thread.sleep(1000 / 24);
					} catch (InterruptedException e) { /* die */ }
				}
			}
		}).start();
	}

	private void initializeNoise(int w, int h) {
		int length = ((w + 7) * h) / 8;
		noiseData = new byte[length];
		DataBuffer db = new DataBufferByte(noiseData, length);
		WritableRaster wr = Raster.createPackedRaster(db, w, h, 1, null);
		ColorModel cm = new IndexColorModel(1, 2, new byte[] { (byte) 0,
				(byte) 255 }, new byte[] { (byte) 0, (byte) 255 }, new byte[] {
				(byte) 0, (byte) 255 });
		noiseImage = new BufferedImage(cm, wr, false, null);		
	}
	
	@Override
	public void componentResized(ComponentEvent e) {
		// resize noise if necessary
		if(camInitializing || camInitializationFailed) {
			final int w = e.getComponent().getWidth();
			final int h = e.getComponent().getHeight();
			log.debug("w: {}, h: {}", w, h);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					initializeNoise(w, h);
				}
			});
		}
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}
	
	/**
	 * index of the device as returned by native enumeration
	 */
	private int deviceIndexToUse = 0;

	private CamInitializationObserver camInitObserver = null;
	
	/**
	 * @param o object wishing to be notified when cam device has been 
	 * 	succesfully initialized and is about to begin steaming frames
	 */
	public void setCamInitObserver(CamInitializationObserver o) {
		camInitObserver = o;
	}
	
	/**
	 * @param l object wishing to listen for mouse events fired over cam 
	 * 	viewport
	 */
	public void setViewportMouseListener(ViewportMouseListener l) {
		fc.setMouseListener(l);
	}
	
	/**
	 * Attempts to start a new web cam session. Returns immediately since the 
	 * actual device initialization occurs in a separate thread. Does not check 
	 * if cam is currently busy, therefore it is caller's responsibility to 
	 * ensure cam resource is available and call {@link #stopWebCamSession()} 
	 * if necessary.
	 * 
	 * @param device streaming device to use for this session; null if last 
	 * 	used device should be used, if first time it would be default device
	 */
	public void startWebCamSession(final VideoDevice device) {
		
		log.info("starting cam session...");
		setCamInitializing();
		
		// handle in a separate thread so UI is not locked					
		Runnable camInitRoutine = new Runnable() {
			@Override
			public void run() {
				try {
					List<Device> devices = VideoCapture.getVideoDevices();
					if(devices.size() == 0) {
						throw new VideoCaptureException("No video devices found");
					}
					else if(camInitObserver != null) {
						List<VideoDevice> videoDevices = 
								new LinkedList<VideoDevice>();
						int x = 0;
						for(Device d : devices) {
							VideoDevice vidDev = new VideoDevice(x++, d);
							videoDevices.add(vidDev);
						}
						camInitObserver.camScanComplete(videoDevices, null);
					}
					if(device != null) deviceIndexToUse = device.getIndex();
					try {
						// FIXME: once OpenIMAJ supports cam discovery pick 
						// best (native) resolution. Right now we're guessing
						vc = new VideoCapture(captureWidth, captureHeight, devices.get(deviceIndexToUse));
					}
					catch(ArrayIndexOutOfBoundsException ex) {
						// should not happen ..
						log.error(ex.getMessage());
						throw new VideoCaptureException("Problem selecting device");
					}
					catch(IndexOutOfBoundsException ex) {
						// will happen if usb device is unplugged
						log.error(ex.getMessage());
						throw new VideoCaptureException("Cannot access " + device.getName());
					}
					Dimension dim = new Dimension(getWidth(), getHeight());
					fc.setPreferredSize(dim);
					display = new VideoDisplay<MBFImage>(vc, null, fc);
					display.addVideoListener(fc);
					display.addVideoListener(new VideoDisplayListener<MBFImage>() {

						@Override
						public void afterUpdate(VideoDisplay<MBFImage> display) {
							// not used
						}

						@Override
						public void beforeUpdate(MBFImage frame) {
							if(isCamInitializing()) {
								log.debug("cam initialized");
								setCamInitialized();
								if(camInitObserver != null) camInitObserver.camReady();
							}
						}
						
					});
					displayThread = new Thread(display);
					displayThread.start();
				}
				catch (VideoCaptureException e) {
					log.error(e.getMessage());
					setCamInitializationFailed(e);
					if(camInitObserver != null) camInitObserver.camFailed();
				}
			}								
		};
		new Thread(camInitRoutine).start();
	}
	
	/**
	 * Ends existing web cam session and cleans up cam resources. If cam 
	 * session was already closed, this method returns safely and does nothing.
	 */
	public void stopWebCamSession() {
		
		if(display != null) {
			log.debug("cleaning up display..");
			display.close(); // kills the video carrying thread
			display = null;
		}
		if(vc != null) {
			log.debug("cleaning up video capture ..");
			vc.stopCapture(); // closes the web cam device
			vc = null;
		}
	}

	/**
	 * {@inheritDoc} This implementation will only set layout manager if none 
	 * has been set yet, otherwise it ignores the request and logs a warning.
	 * 
	 * @param mgr layout manager to set
	 */
	@Override
	public void setLayout(LayoutManager mgr) {
		if(getLayout() == null)
			super.setLayout(mgr);
		else
			log.warn("setting layout manager is not allowed {}");
	}

	/**
	 * @param drawMessages true if info text should be painted below the 
	 * 	blinking icon; false does not paint the info text (default)
	 */
	public void setDrawMessages(boolean drawMessages) {
		this.drawMessages = drawMessages;
	}
	
	/**
	 * Indicates if fps stats should be drawn over the display viewport
	 * 
	 * @param showFpsStats true if FPS stats should be drawn; false if they 
	 * 	should be turned off
	 */
	public void setShowFpsStats(boolean showFpsStats) {
		fc.setShowFpsStats(showFpsStats);
	}
	
	public void setFpsObserver(FpsObserver observer) {
		fc.setFpsObserver(observer);
	}
	
	@Subscribe
	public void onFilter(CamFilterEvent ev) {
		CamFilterOps operation = ev.getOperation();
		int oldWidth = captureWidth;
		switch(operation) {
		case Canny:
			captureWidth = 320;
			captureHeight = 240;
			break;
		default:
			captureWidth = 1024;
			captureHeight = 768;
		}
		if(oldWidth != captureWidth) {
			stopWebCamSession();
			startWebCamSession(null);
		}
	}
}