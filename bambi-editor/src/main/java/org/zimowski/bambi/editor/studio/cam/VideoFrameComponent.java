package org.zimowski.bambi.editor.studio.cam;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.ViewportMouseListener;
import org.zimowski.bambi.editor.studio.eventbus.EventBusManager;
import org.zimowski.bambi.editor.studio.eventbus.events.CamFilterEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.CamMirrorFilterEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.CamPictureRequestEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.CamPictureTakenEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.CamRgbFilterEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Individual video frame captured by the device. Provides support for 
 * real-time reporting of individual pixel color from under the mouse, 
 * based on a frame rate and mouse movement. Capable of displaying frame 
 * rendering times.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class VideoFrameComponent extends ImageComponent implements VideoDisplayListener<MBFImage> {
	
	private static final long serialVersionUID = -3761572457039762654L;
	
	private final static Logger log = LoggerFactory.getLogger(VideoFrameComponent.class);

	private ViewportMouseListener mouseListener;
	
	private long framesRendered;
	
	/**
	 * number of frames to sample from when computing avg fps
	 */
	private static final int AVG_FPS_SAMPLE = 20;
	
	private double lastFps = 0d;
	
	private double lastAvgFps = 0d;
	
	/**
	 * history of most recent captured FPS results
	 */
	private double[] sampleFps = new double[AVG_FPS_SAMPLE];
	
	private static final DecimalFormat df = new DecimalFormat("#.#######");
	
	private boolean showFpsStats = false;
	
	private FpsObserver fpsObserver;
	
	private CamFilterOps filterOp = CamFilterOps.None;
	
	private float redBand = 1f;
	
	private float greenBand = 1f;
	
	private float blueBand = 1f;

	
	public VideoFrameComponent() {
		super();
		EventBusManager.getInstance().registerWithBus(this);
	}
	
	private void updateAvgFps() {
		if((framesRendered % AVG_FPS_SAMPLE) == 0) {
			double avgFps = 0d;
			for(double fps : sampleFps) avgFps += fps;
			lastAvgFps = avgFps / sampleFps.length;
		}
	}
	
	@Subscribe
	public void onFilter(CamFilterEvent ev) {
		filterOp = ev.getOperation();
		log.debug("filter: {}", filterOp);
	}
	
	@Subscribe
	public void onMirror(CamMirrorFilterEvent ev) {
		int band = ev.getBand();
		log.debug("band {} on {}", band, ev.isOn());
		switch(band) {
		case CamFilterEvent.BAND_RED:
			redMirror = ev.isOn();
			break;
		case CamFilterEvent.BAND_GREEN:
			greenMirror = ev.isOn();
			break;
		case CamFilterEvent.BAND_BLUE:
			blueMirror = ev.isOn();
			break;
		}
	}
	
	@Subscribe
	public void onRgb(CamRgbFilterEvent ev) {
		int value = ev.getValue();
		float pct = (10 + value)/10f;
		log.debug(String.format("%s: {}, {}", ev.getBand()), pct, ev.getValue());
		switch(ev.getBand()) {
		case CamFilterEvent.BAND_RED:
			redBand = pct;
			break;
		case CamFilterEvent.BAND_GREEN:
			greenBand = pct;
			break;
		case CamFilterEvent.BAND_BLUE:
			blueBand = pct;
			break;
		}
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// tell us when frames begin to appear on display (they likely 
		// will have been already streamed from the device)
		sampleFps[(int)(framesRendered % AVG_FPS_SAMPLE)] = lastFps = display.getDisplayFPS();
		if(framesRendered == 0) framesRendered++;
		dispatchEvent(new MouseEvent(
				this,
				MouseEvent.MOUSE_MOVED, 
				System.currentTimeMillis(),
				MouseEvent.NOBUTTON, 
				(int)getCurrentMousePosition().getX(), 
				(int)getCurrentMousePosition().getY(), 
				0, 
				false)
		);
	}
	
	private boolean takePicture = false;
	
	@Subscribe
	public void onPictureRequest(CamPictureRequestEvent ev) {
		takePicture = true;
	}
	
	private boolean redMirror = false;
	private boolean greenMirror = false;
	private boolean blueMirror = false;

	@Override
	public void beforeUpdate(MBFImage frame) {
		if(frame == null) return;
		switch(filterOp) {
		case None: break;
		case Rgb:
			if(redBand != 1f) frame.getBand(0).multiplyInplace(redBand);
			if(greenBand != 1f) frame.getBand(1).multiplyInplace(greenBand);
			if(blueBand != 1f) frame.getBand(2).multiplyInplace(blueBand);
			break;
		case Canny:
			frame.processInplace(new CannyEdgeDetector());
			break;
		case Negative:
			frame.inverse();
			break;
		case Solarize:
			solarize(frame);
			break;
		case Grayscale:
			transformGrayScale(frame);
			break;
		case Mirror:
			if(redMirror) frame.getBand(0).flipX();
			if(greenMirror) frame.getBand(1).flipX();
			if(blueMirror) frame.getBand(2).flipX();
			break;
		default:
		}
		
		if(takePicture) {
			EventBus bus = EventBusManager.getInstance().getBus();
			CamPictureTakenEvent ev = new CamPictureTakenEvent(frame);
			bus.post(ev);
			takePicture = false;
		}

		//rgbAdjust(frame.getBand(0), 2.25f);
		//transformGrayScale(frame);
		//transformHue(frame); // works but slow
		//RGB_TO_RGB_NORMALISED(frame); // 352x288
		//RGB_TO_CIEXYZ(frame);
		//RGB_TO_HSV(frame); // 800x600
		//calculateSaturation(frame); GOOD!
		//calculateHue(frame); 320x240 crappy
		//mainColors(frame); // GREAT!
		//hsFilter(frame);
	}
	
	public void solarize(MBFImage in) {
		final float threshold = 0.5f;
		float[][] red = in.getBand(0).pixels;
		float[][] green = in.getBand(1).pixels;
		float[][] blue = in.getBand(2).pixels;
		for(int y = 0; y < in.getHeight(); y++)
			for(int x = 0; x < in.getWidth(); x++) {
				float r = red[y][x];
				float g = green[y][x];
				float b = blue[y][x];
				red[y][x] = r > threshold ? 2*(r-threshold) : 2*(threshold-r);
				green[y][x] = g > threshold ? 2*(g-threshold) : 2*(threshold-g);
				blue[y][x] = b > threshold ? 2*(b-threshold) : 2*(threshold-b);
				
				if(red[y][x] > 1f) red[y][x] = 1f;
				if(green[y][x] > 1f) green[y][x] = 1f;
				if(blue[y][x] > 1f) blue[y][x] = 1f;
			}
	}

	public void calculateHue(MBFImage in) {
		
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");

		float [][] ra = in.getBand(0).pixels;
		float [][] ga = in.getBand(1).pixels;
		float [][] ba = in.getBand(2).pixels;

		for (int rr = 0; rr < in.getHeight(); rr++) {
			for (int c = 0; c < in.getWidth(); c++) {
				double r = ra[rr][c];
				double g = ga[rr][c];
				double b = ba[rr][c];
				double i = (r + g + b) / 3.0; 

				//from Sonka, Hlavac & Boyle; p.26
				double num = 0.5 * ((r - g) + (r - b));
				double den = Math.sqrt( ((r-g)*(r-g)) + ((r-b)*(g-b)));

				float pixel = 0f;
				if (den != 0)
					pixel = (float) Math.acos(num / den);
				
				double dpi = 2*Math.PI;

				if ((b/i) > (g/i)) pixel = (float)(dpi - pixel);

				//normalise to 0..1
				pixel /= dpi;
				
				ra[rr][c] = ga[rr][c] = ba[rr][c] = pixel;
			}
		}
	}
	
	public void calculateSaturation(MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");
		
		float [][] ra = in.getBand(0).pixels;
		float [][] ga = in.getBand(1).pixels;
		float [][] ba = in.getBand(2).pixels;

		for (int rr = 0; rr < in.getHeight(); rr++) {
			for (int c = 0; c < in.getWidth(); c++) {
				double r = ra[rr][c];
				double g = ga[rr][c];
				double b = ba[rr][c];

				float pixel = (float) (1.0 - ((3.0 / (r + g + b)) * Math.min(r, Math.min(g, b))));
				if (Float.isNaN(pixel)) pixel = 0;
				
				ra[rr][c] = ga[rr][c] = ba[rr][c] = pixel;
			}
		}
	}

	public void RGB_TO_HSV(MBFImage in) {
		if (in.colourSpace != ColourSpace.RGB && in.colourSpace != ColourSpace.RGBA)
			throw new IllegalArgumentException("RGB or RGBA colourspace is required");
		
		int width = in.getWidth();
		int height = in.getHeight();
		
		MBFImage out = new MBFImage(width, height, 3);

		float [][] R = in.getBand(0).pixels;
		float [][] G = in.getBand(1).pixels;
		float [][] B = in.getBand(2).pixels;

		float [][] H = out.getBand(0).pixels;
		float [][] S = out.getBand(1).pixels;
		float [][] V = out.getBand(2).pixels;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				//Blue Is the dominant color
				if((B[y][x] > G[y][x]) && (B[y][x] > R[y][x]))
				{
					//Value is set as the dominant color
					V[y][x] = B[y][x];
					if(V[y][x] != 0)
					{
						float min;
						if(R[y][x] > G[y][x]) 
							min = G[y][x];
						else
							min = R[y][x];
						
						//Delta is the difference between the most dominant color 
						//and the least dominant color. This will be used to compute saturation.
						float delta = V[y][x] - min;
						if(delta != 0) { 
							S[y][x] = (delta/V[y][x]); 
							H[y][x] = 4 + (R[y][x] - G[y][x]) / delta; 
						} else {
							S[y][x] = 0;
							H[y][x] = 4 + (R[y][x] - G[y][x]);
						}
						
						//Hue is just the difference between the two least dominant 
						//colors offset by the dominant color. That is, here 4 puts 
						//hue in the blue range. Then red and green just tug it one 
						//way or the other. Notice if red and green are equal, hue 
						//will stick squarely on blue
						H[y][x] *= 60; 
						if(H[y][x] < 0) 
							H[y][x] += 360;
						
						H[y][x] /= 360;
					}
					else
					{ 
						S[y][x] = 0; 
						H[y][x] = 0;
					}
				}
				//Green is the dominant color
				else if(G[y][x] > R[y][x])
				{
					V[y][x] = G[y][x];
					if(V[y][x] != 0)
					{
						float min;
						if(R[y][x] > B[y][x]) 
							min = B[y][x];
						else
							min = R[y][x];
						
						float delta = V[y][x] - min;
						
						if(delta != 0) {
							S[y][x] = (delta/V[y][x]); 
							H[y][x] = 2 + (B[y][x] - R[y][x]) / delta; 
						} else { 
							S[y][x] = 0;
							H[y][x] = 2 + (B[y][x] - R[y][x]); 
						}
						H[y][x] *= 60; 
						if(H[y][x] < 0) 
							H[y][x] += 360;
						
						H[y][x] /= 360;
					} else {
						S[y][x] = 0;
						H[y][x] = 0;
					}
				}
				//Red is the dominant color
				else
				{
					V[y][x] = R[y][x];
					if(V[y][x] != 0)
					{
						float min;
						if(G[y][x] > B[y][x]) 
							min = B[y][x];
						else
							min = G[y][x];
						
						float delta = V[y][x] - min;
						if(delta != 0) { 
							S[y][x] = (delta/V[y][x]); 
							H[y][x] = (G[y][x] - B[y][x]) / delta; 
						} else { 
							S[y][x] = 0;         
							H[y][x] = (G[y][x] - B[y][x]); 
						}
						H[y][x] *= 60;

						if(H[y][x] < 0) 
							H[y][x] += 360;
						H[y][x] /= 360;
					}
					else
					{ 
						S[y][x] = 0;
						H[y][x] = 0;
					}
				}
			}
		}
		in.getBand(0).pixels = out.getBand(0).pixels;
		in.getBand(1).pixels = out.getBand(1).pixels;
		in.getBand(2).pixels = out.getBand(2).pixels;
	}
	
	public void RGB_TO_CIEXYZ(MBFImage in) {	
		int height = in.getHeight();
		int width = in.getWidth();
		in.colourSpace = ColourSpace.CIE_XYZ;
		
		FImage Rb = in.getBand(0);
		FImage Gb = in.getBand(1);
		FImage Bb = in.getBand(2);
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				float R = Rb.pixels[y][x];
				float G = Gb.pixels[y][x];
				float B = Bb.pixels[y][x];
				
				//inverse sRGB companding 
				double r = (R <= 0.04045) ? (R / 12.92) : ( Math.pow((R + 0.055) / 1.055, 2.4));
				double g = (G <= 0.04045) ? (G / 12.92) : ( Math.pow((G + 0.055) / 1.055, 2.4));
				double b = (B <= 0.04045) ? (B / 12.92) : ( Math.pow((B + 0.055) / 1.055, 2.4));
				
				//XYZ linear transform
				Rb.pixels[y][x] = (float) (r*0.4124564 + g*0.3575761 + b*0.1804375);
				Gb.pixels[y][x] = (float) (r*0.2126729 + g*0.7151522 + b*0.0721750);
				Bb.pixels[y][x] = (float) (r*0.0193339 + g*0.1191920 + b*0.9503041);
			}
		}
	}
	
	private void transformGrayScale(MBFImage frame) {
		FImage redBand = frame.getBand(0);
		FImage greenBand = frame.getBand(1);
		FImage blueBand = frame.getBand(2);
		
		int width = frame.getWidth();
		int height = frame.getHeight();
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				float redPixel = redBand.getPixel(x, y);
				float greenPixel = greenBand.getPixel(x, y);
				float bluePixel = blueBand.getPixel(x, y);
				float avg = (redPixel + greenPixel + bluePixel) / 3;
				redBand.setPixel(x, y, avg);
				greenBand.setPixel(x, y, avg);
				blueBand.setPixel(x, y, avg);
			}		
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		super.mouseDragged(e);
		try { mouseListener.mouseDragged(e, getCurrentDisplayedPixelColour()); }
		catch(ArrayIndexOutOfBoundsException ex) {}
		catch(NullPointerException npe) { /* if frame hasn't rendered yet */ }
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
		try { mouseListener.mouseMoved(e, getCurrentDisplayedPixelColour()); }
		catch(ArrayIndexOutOfBoundsException ex) {}
		catch(NullPointerException npe) { /* if frame hasn't rendered yet */ }
	}

	@Override
	public void mouseExited(MouseEvent e) {
		super.mouseExited(e);
		if(mouseListener != null) mouseListener.mouseExited(e);
	}

	public void setMouseListener(ViewportMouseListener mouseListener) {
		this.mouseListener = mouseListener;
	}
	
	private String[] buildFpsMessages() {
		
		int currentSampleIndex = (int)(framesRendered % AVG_FPS_SAMPLE);
		return new String[] {
				"Live FSP: " + df.format(sampleFps[currentSampleIndex]), 
				"Avg FPS: " + df.format(lastAvgFps)
		};
	}
	
	private void paintFpsStats(Graphics g) {
		String[] fps = buildFpsMessages();
		final int fontSize = 12;
		int type = AlphaComposite.SRC_OVER; 
		AlphaComposite composite = AlphaComposite.getInstance(type, 0.45f);
		Graphics2D g2 = (Graphics2D) g.create();
	    g2.setComposite(composite);
		g2.setColor(Color.DARK_GRAY);
		Font font = new Font("SansSerif", Font.PLAIN, fontSize);
		FontMetrics fm = g.getFontMetrics(font);				
		g2.fillRect(0, 0, getWidth(), (fm.getHeight()*fps.length)+(fontSize/2));
		g2.dispose();
		if(framesRendered > 0) {
			g.setFont(font);
			g.setColor(Color.WHITE);
			int x = 1;
			final int xOffset = 3;
			for(; x<=fps.length; ++x) {
				g.drawString(fps[x-1], xOffset, fm.getHeight()*x);
			}
		}
		else {
			g.setColor(Color.YELLOW);
			g.drawString("Initializing ...", 3, fm.getHeight());
		}
	}


	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(framesRendered > 0) {
			framesRendered++;
			updateAvgFps();
			if(fpsObserver != null) {
				fpsObserver.fpsComputed(lastFps, lastAvgFps);
			}
		}
		if(showFpsStats) paintFpsStats(g);
	}

	/**
	 * Determines if frame per second stats are to be painted.
	 * 
	 * @return true if FPS stats are being drawn; false if they are turned off
	 */
	public boolean isShowFpsStats() {
		return showFpsStats;
	}

	/**
	 * @param showFpsStats true if FPS stats should be drawn; false if they 
	 * 	should be turned off
	 */
	public void setShowFpsStats(boolean showFpsStats) {
		this.showFpsStats = showFpsStats;
	}
	
	public void setFpsObserver(FpsObserver observer) {
		fpsObserver = observer;
	}
}