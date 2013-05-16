package org.zimowski.bambi.editor.studio.image;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.stream.ImageInputStream;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.commons.ImageUtil;
import org.zimowski.bambi.editor.config.Configuration;
import org.zimowski.bambi.editor.filters.ColorAdjustFilter;
import org.zimowski.bambi.editor.filters.ColorAdjustFilter.AdjustType;
import org.zimowski.bambi.editor.filters.FilterListener;
import org.zimowski.bambi.editor.filters.ImageFilterOps;
import org.zimowski.bambi.editor.filters.SepiaFilter;
import org.zimowski.bambi.editor.studio.ScaleViewException;
import org.zimowski.bambi.editor.studio.eventbus.EventBusManager;
import org.zimowski.bambi.editor.studio.eventbus.ImageFilterQueue;
import org.zimowski.bambi.editor.studio.eventbus.events.ImageFilterEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ImageFilterMonitorEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ImageLoadEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelLifecycleEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelLifecycleEvent.ModelPhase;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelResetRequestEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelRotateEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.RotateEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ScaleEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ThumbAddEvent;
import org.zimowski.bambi.editor.studio.image.ImageTransformListener.RotateDirection;
import org.zimowski.bambi.jhlabs.image.NoiseFilter;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.DitherFilter;
import com.jhlabs.image.EdgeFilter;
import com.jhlabs.image.EmbossFilter;
import com.jhlabs.image.FlipFilter;
import com.jhlabs.image.GrayscaleFilter;
import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.KaleidoscopeFilter;
import com.jhlabs.image.LightFilter;
import com.jhlabs.image.MarbleFilter;
import com.jhlabs.image.PosterizeFilter;
import com.jhlabs.image.SolarizeFilter;
import com.jhlabs.image.StampFilter;
import com.jhlabs.image.TwirlFilter;

/**
 * Represents the underlying image and manages all changes to it. Rotation 
 * and zooming are buit in, and all other transformations (filters) are applied 
 * from external classes. This model is driven internally by display image 
 * struct ({@link ImageModel#displayImage}), and two helper buffers 
 * ({@link ImageModel#unscaledDisplayImage}, {@link ImageModel#unscaledReferenceImage}). 
 * While not ideally memory efficient design, it is an optimized model given 
 * the tradeoff of memory usage vs. runtime performance.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class ImageModel implements ImageConduit {
    
	private static final Logger log = LoggerFactory.getLogger(ImageModel.class);
	
    /**
     * contains the system path of the image
     */
    private String filePath;
    
    private BufferedImage unscaledReferenceImage;
    
    private BufferedImage unscaledDisplayImage;
        
    private BufferedImage displayImage;
    
    private ColorAdjustFilter colorFilter = new ColorAdjustFilter();
    
    private Double scale = 1.0;
    
    /**
     * Current rotation angle. 0 = no rotation, 90: rotated right, etc. 
     * Negative values supported (-90: rotated left).
     */
    private int angle = 0;
    
    /**
     * contains queued up filters user added for processing. Generally FIFO, 
     * with minor exception. For example, {@link ScaleWorker} is always added 
     * to the beginning of the list.
     */
    private LinkedList<AbstractFilterWorker> filterQueue; 

    
    public ImageModel() {
    	filterQueue = new ImageFilterQueue();
    	EventBusManager.getInstance().registerWithBus(this);
    }
    
    @Subscribe
    public void onImageLoad(ImageLoadEvent ev) throws Exception {
    	File imageFile = ev.getImageFile();
    	if(imageFile == null) return;
    	this.filePath = imageFile.getAbsolutePath();
    	filterQueue.addFirst(new RefreshWorker(getImage()==null, ev.isLoadThumb()));
    }
    
    private BufferedImage buildDummyImage() {
    	
    	BufferedImage notFoundImage;
    	URL url = getClass().getResource(Configuration.RESOURCE_PATH + "imageNotFound.jpg");
    	
    	try {
    		notFoundImage = ImageIO.read(url);
    	}
    	catch(IOException e) {
	    	notFoundImage = new BufferedImage(
	    			ImageContainer.DEFAULT_CANVAS_WIDTH, 
	    			ImageContainer.DEFAULT_CANVAS_HEIGHT, 
	    			BufferedImage.TYPE_BYTE_GRAY
	    			);
    	}

    	return notFoundImage;
    }

    private void setupImageParams() {
        angle = 0;
        scale = 1.0;
    }

    /**
     * Scales the image to desired percentage.
     * 
     * @param scalePct The scale which should be applied to the image.
     * @throws ScaleViewException This exception is thrown if the scale is out  
     * 		of bounds. Accepted value domain is between 0.1 to 1.0 inclusive.
     */
    public void scale(double scalePct) throws ScaleViewException {
        
        if (scalePct > 1D || scalePct < 0.1D) {
            throw new ScaleViewException(scalePct + " out of range!");
        } else {
            this.scale = scalePct;
        }

        displayImage = EditorImageUtil.rotate(
        		unscaledDisplayImage, null, null, this.scale, this.scale);
    }
    
    /**
     * Rotates the image 90 degrees left or right. Preserves any zooming that 
     * may have been applied.
     * 
     * @param direction - the direction in which the image is rotated.
     */
    public void rotate(final RotateDirection direction) {
    	angle += direction.getAngle();
    	filterQueue.add(new RotateWorker(direction));
    }

    /**
     * Returns a BufferedImage with an applied affinetransform on it.
     * @return resultImg A BufferedImage with applied affinetransform
     */
    public BufferedImage getImage() {
        return displayImage;
    }

    private void oldPhoto() {
    	final SepiaFilter f = new SepiaFilter();
    	f.setAmount(30);
    	f.setDensity(0.1f);
    	f.setDistribution(NoiseFilter.GAUSSIAN);
    	f.setMonochrome(false);    	
    	filterQueue.add(new FilterWorker(f));
    }

	private void negative() {
		filterQueue.add(new FilterWorker(new InvertFilter()));
	}

	private void emboss() {
		filterQueue.add(new FilterWorker(new EmbossFilter()));
	}

	private void deamonize() {
		filterQueue.add(new FilterWorker(new EdgeFilter()));
	}

	private void solarize() {
		filterQueue.add(new FilterWorker(new SolarizeFilter()));
	}

	private void grayscale() {
		filterQueue.add(new FilterWorker(new GrayscaleFilter()));
	}

	private void chessboard() {
		filterQueue.add(new FilterWorker(new DitherFilter()));
	}

	private void marble() {
		filterQueue.add(new FilterWorker(new MarbleFilter()));
	}

	private void cartoonize() {
		final LightFilter f = new LightFilter();
		f.setBumpShape(LightFilter.BUMPS_FROM_BEVEL);
		f.setBumpSoftness(1.5F);
		/*
		try {
			//BufferedImage bumpMap = ImageIO.read(new File("/home/zima/bumpo.jpg"));
			BufferedImage bumpMap = ImageIO.read(new File("/home/zima/bump20x22.jpg"));
			Function2D bumpFunc = new ImageFunction2D(bumpMap);
			f.setBumpSource(LightFilter.BUMPS_FROM_MAP);
			f.setBumpFunction(bumpFunc);
			f.setBumpHeight(0.2f);
			//f.getLights().clear();
			//f.addLight(new LightFilter.PointLight());
			Light light = new LightFilter.AmbientLight();
			//light.setAzimuth(0.5f);
			//light.setDistance(200f);
			//f.addLight(light);
			f.setBumpSoftness(0f);
			//f.setViewDistance(20000f);
			//f.setBumpShape(5); // 1-5
			Material m = new Material();
			m.setDiffuseColor(2);
			m.setOpacity(0.5f);
			//f.setMaterial(m);
			//f.setViewDistance(0.1f);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		filterQueue.add(new FilterWorker(f));
	}

	private void mirror() {
		final FlipFilter f = new FlipFilter();
		f.setOperation(FlipFilter.FLIP_H);
		filterQueue.add(new FilterWorker(f));
	}

	public void twirl() {
		final TwirlFilter f = new TwirlFilter();
		f.setEdgeAction(TwirlFilter.WRAP);
		f.setInterpolation(TwirlFilter.BILINEAR);
		f.setRadius(Math.max(displayImage.getWidth(), displayImage.getHeight())*2);
		f.setAngle(1.2f);		
		filterQueue.add(new FilterWorker(f));
	}

	private void stamp() {
		filterQueue.add(new FilterWorker(new StampFilter()));
	}

	private void posterize() {
		filterQueue.add(new FilterWorker(new PosterizeFilter()));
	}

	private void kaleidoscope() {
		final KaleidoscopeFilter f = new KaleidoscopeFilter();
    	f.setSides(4);
    	f.setEdgeAction(2);
		filterQueue.add(new FilterWorker(f));
	}
	
	void adjustColors() {
    	unscaledDisplayImage = colorFilter.filter(unscaledReferenceImage, null);
    	displayImage = EditorImageUtil.rotate(unscaledDisplayImage, null, null, scale, scale);
	}

	private void notifyModelInitialized() {
		getBus().post(new ModelLifecycleEvent(ModelPhase.Initialized));
	}
	
	private void notifyModelAboutToChange() {
		getBus().post(new ModelLifecycleEvent(ModelPhase.BeforeChange));
	}
	
	private void notifyModelChanged(boolean rgbReset, boolean hsReset, boolean cbReset) {
		ModelLifecycleEvent event = new ModelLifecycleEvent(ModelPhase.AfterChange);
		event.setRgbReset(rgbReset);
		event.setHsReset(hsReset);
		event.setCbReset(cbReset);
		event.setImageConduit(this);
		getBus().post(event);
	}
	
	private void notifyModelReset() {
		ModelLifecycleEvent event = new ModelLifecycleEvent(ModelPhase.Reset);
		event.setImageConduit(this);
		getBus().post(event);
	}
	
	private void notifyModelRotated(int angleOfRotation, int cumulativeAngle) {
		getBus().post(new ModelRotateEvent(angleOfRotation, cumulativeAngle));
	}
	
	private void hscb(ImageFilterOps op, int value) {
		
		float adjVal = 0;
		
		switch(op) {
		case Hue:
		case Saturation:
			adjVal = value / 10f;
			break;
		case Contrast:
		case Brightness:
			adjVal = (value + 10) / 10f;
		default:
		}
		if(log.isDebugEnabled()) {
			log.debug(String.format("%s | v: {}, av: {}", op.toString()), value, adjVal);
		}
		filterQueue.add(new ColorFilterWorker(op, adjVal));
	}
	
	private void rgb(ImageFilterOps op, int value) {
		final float adjVal = value / 10f;
		log.debug(String.format("%s | v: {}, av: {}", op.toString()), value, adjVal);
		filterQueue.add(new ColorFilterWorker(op, adjVal));
	}
	
	@Subscribe
	public void onFilter(ImageFilterEvent ev) {
		
		// guava's ugly side :-(
		if(ev instanceof ImageFilterMonitorEvent) return;
		
		ImageFilterOps filter = ev.getFilter();
		
		switch(filter) {
		case Red:
		case Green:
		case Blue:
			rgb(filter, ev.getValue());
			break;
		case Hue:
		case Saturation:
		case Brightness:
		case Contrast:
			hscb(filter, ev.getValue());
			break;
		case Cartoonize:
			cartoonize();
			break;
		case Daemonize:
			deamonize();
			break;
		case Chessboard:
			chessboard();
			break;
		case Emboss:
			emboss();
			break;
		case Mirror:
			mirror();
			break;
		case Grayscale:
			grayscale();
			break;
		case Negative:
			negative();
			break;
		case Kaleidoscope:
			kaleidoscope();
			break;
		case Marble:
			marble();
			break;
		case OldPhoto:
			oldPhoto();
			break;
		case Posterize:
			posterize();
			break;
		case Solarize:
			solarize();
			break;
		case Stamp:
			stamp();
			break;
		case Twirl:
			twirl();
			break;
		default:
			// either not handled or on separate subscription
			break;
		}
	}

	@Subscribe
	public void onReset(ModelResetRequestEvent ev) {
		filterQueue.addFirst(new RefreshWorker(ev == null, false));
	}

	@Subscribe
	public void onRotate(RotateEvent ev) {
		rotate(ev.getDirection());
	}

	@Subscribe
	public void onScale(ScaleEvent ev) {
		
		int percent = ev.getValue();
		/*notifyModelAboutToChange();
		log.debug("scale {}", percent);
		scale(percent/100d);
		notifyModelChanged();*/
		filterQueue.addFirst(new ScaleWorker(percent));
	}
	
	/**
	 * @author Adam Zimowski (mrazjava)
	 */
	class RefreshWorker extends AbstractFilterWorker {

		private boolean initialize;
		
		private boolean loadThumb;
		
		public RefreshWorker(boolean initialize, boolean loadThumb) {
			this.initialize = initialize;
			this.loadThumb = loadThumb;
		}
		
		@Override
		public ImageFilterOps getMetaData() {
			return ImageFilterOps.Refresh;
		}

		@Override
		protected Void doInBackground() throws Exception {
			log.trace("on EDT ? {}", SwingUtilities.isEventDispatchThread());
			notifyModelAboutToChange();
			boolean callGc = (unscaledReferenceImage != null);
	    	unscaledReferenceImage = null;
	    	unscaledDisplayImage = null;
	    	displayImage = null;
	    	ImageFilterMonitorEvent event = new ImageFilterMonitorEvent(ImageFilterOps.Refresh, ImageFilterMonitorEvent.PHASE_START);
	    	event.showFilterNameOnOutput = !initialize;
	    	//event.setPctComplete(0);
	    	if(callGc) {
		    	event.setStatus("releasing resources to optimize image load ...");
		    	getBus().post(event);
	    		// seems to help prevent crashing jvm (6) with out of mem on rare occasions
	    		System.gc();
	    	}
	    	event.setStatus("loading: " + filePath);
	    	getBus().post(event);
	    	final File file = new File(filePath);
	        try {
	        	event.setPhase(ImageFilterMonitorEvent.PHASE_PROGRESS);
	        	log.info("image path: [{}]", filePath);
	        	FileInputStream fileInputStream = new FileInputStream(file);
	        	String extension = FilenameUtils.getExtension(filePath).toUpperCase();
	        	Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(extension);
	        	ImageReader imageReader = readers.next();
	        	ImageInputStream imageInputStream = ImageIO.createImageInputStream(fileInputStream);
	        	imageReader.setInput(imageInputStream, false);
	        	imageReader.addIIOReadProgressListener(new ReadProgressListener(event));
	        	unscaledReferenceImage = imageReader.read(0);
	        	fileInputStream.close();
	        	applyExifRotation(file);
	        }
	        catch (IOException e) {
	        	// create dummy image to avoid npe and other error nastiness
	        	log.error(e.getMessage());
	        	unscaledReferenceImage = buildDummyImage();
	        }
	        event.setPhase(ImageFilterMonitorEvent.PHASE_FINALIZE);
	        if(loadThumb) {
	        	getBus().post(new ThumbAddEvent(unscaledReferenceImage, file));
	        }
	        //event.setPctComplete(33);
	        event.setStatus("finalizing, step 1");
	        getBus().post(event);
			unscaledDisplayImage = ImageUtil.deepCopy(unscaledReferenceImage);
	        //event.setPctComplete(66);
	        event.setStatus("finalizing, step 2");
	        getBus().post(event);
			displayImage = ImageUtil.deepCopy(unscaledReferenceImage);
			setupImageParams();
			colorFilter.resetRgb();
			colorFilter.resetHscb(true, true);
			return null;
		}
		
		private void applyExifRotation(File file) {
			int orientation = 0;
			try {
				Metadata meta = ImageMetadataReader.readMetadata(file);
				Directory directory = meta
						.getDirectory(ExifIFD0Directory.class);
				if (directory != null) {
					if(directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
						orientation = directory
								.getInt(ExifIFD0Directory.TAG_ORIENTATION);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			if(orientation > 0) {
				// we have exif info on orientation
				try {
					AffineTransform tfm = ImageUtil.getExifTransformation(orientation, unscaledReferenceImage.getWidth(), unscaledReferenceImage.getHeight());
					unscaledReferenceImage = ImageUtil.transformImage(unscaledReferenceImage, tfm);
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		}

		@Override
		protected void done() {
			super.done();
			if(initialize) {
				notifyModelInitialized();
			}
			else {
				notifyModelReset();
			}
			notifyModelChanged(false, false, false);
		}
		
		/**
		 * @author Adam Zimowski (mrazjava)
		 *
		 */
		class ReadProgressListener implements IIOReadProgressListener {

			private ImageFilterMonitorEvent event;
			
			public ReadProgressListener(ImageFilterMonitorEvent e) {
				this.event = e;
			}
			
			@Override
			public void sequenceStarted(ImageReader source, int minIndex) {
				
			}

			@Override
			public void sequenceComplete(ImageReader source) {
				
			}

			@Override
			public void imageStarted(ImageReader source, int imageIndex) {
				
			}

			@Override
			public void imageProgress(ImageReader source, float percentageDone) {
				log.debug("read: {}", percentageDone);
				event.setPctComplete(Math.round(percentageDone));
				getBus().post(event);
			}

			@Override
			public void imageComplete(ImageReader source) {
				
			}

			@Override
			public void thumbnailStarted(ImageReader source, int imageIndex,
					int thumbnailIndex) {
				
			}

			@Override
			public void thumbnailProgress(ImageReader source,
					float percentageDone) {
				
			}

			@Override
			public void thumbnailComplete(ImageReader source) {
				
			}

			@Override
			public void readAborted(ImageReader source) {
				
			}
		}
	}
	
	/**
	 * @author Adam Zimowski (mrazjava)
	 */
	class RotateWorker extends AbstractFilterWorker {
		
		private RotateDirection direction;
		
		private ImageFilterOps metaInfo;
		
		public RotateWorker(RotateDirection direction) {
			this.direction = direction;
			if(RotateDirection.LEFT.equals(direction)) {
				metaInfo = ImageFilterOps.RotateLeft;
				displayValue = "-90°";
			}
			else {
				metaInfo = ImageFilterOps.RotateRight;
				displayValue = "+90°";
			}
		}

		@Override
		protected Void doInBackground() throws Exception {
			notifyModelAboutToChange();
			ImageFilterMonitorEvent event = new ImageFilterMonitorEvent(metaInfo, ImageFilterMonitorEvent.PHASE_START);
			getBus().post(event);
	    	displayImage = EditorImageUtil.rotate(unscaledDisplayImage, direction.getAngle(), null, scale, scale);
			event.setPhase(ImageFilterMonitorEvent.PHASE_PROGRESS);
			event.setPctComplete(33);
			getBus().post(event);
			unscaledDisplayImage = EditorImageUtil.rotate(unscaledDisplayImage, direction.getAngle(), null, null, null);
			event.setPctComplete(66);
			getBus().post(event);
			unscaledReferenceImage = EditorImageUtil.rotate(unscaledReferenceImage, direction.getAngle(), null, null, null);
			return null;
		}

		@Override
		protected void done() {
			super.done();
			notifyModelRotated(direction.getAngle(), angle);
			notifyModelChanged(false, false, false);
		}

		@Override
		public ImageFilterOps getMetaData() {
			return metaInfo;
		}
	}
	
	/**
	 * @author Adam Zimowski (mrazjava)
	 */
	class ScaleWorker extends AbstractFilterWorker {

		private int percent;
		
		public ScaleWorker(int percent) {
			this.percent = percent;
			this.displayValue = new Integer(percent).toString()+" %";
		}
		
		@Override
		public ImageFilterOps getMetaData() {
			return ImageFilterOps.Scale;
		}

		@Override
		protected Void doInBackground() throws Exception {
			// very fast operation so don't bother with events and feedback
			notifyModelAboutToChange();
			log.debug("scale {}", percent);
			scale(percent/100d);
			return null;
		}

		@Override
		protected void done() {
			super.done();
			notifyModelChanged(false, false, false);
		}
	}
	
	/**
	 * @author Adam Zimowski (mrazjava)
	 */
	class ColorFilterWorker extends FilterWorker {
	
		private ImageFilterOps filterOp;
		private float value;
		private AdjustType adjustType;
		
		public ColorFilterWorker(ImageFilterOps filterOp, float value) {
			super(colorFilter);
			this.filterOp = filterOp;
			this.value = value;
			displayValue = (value >= 0f ? "+" : "") + Float.toString(value);
			this.adjustType = ColorAdjustFilter.getAdjustType(filterOp);
		}

		@Override
		protected Void doInBackground() throws Exception {
			//notifyModelAboutToChange(); // uncomment if want busy cursor
			startTime = System.currentTimeMillis();
			filter.setFilterListener(this);
			colorFilter.setCurrentOp(filterOp);
			switch(filterOp) {
			case Red:
				colorFilter.setRed(value);
				break;
			case Green:
				colorFilter.setGreen(value);
				break;
			case Blue:
				colorFilter.setBlue(value);
				break;
			case Hue:
				// bug in the filter? 1.0f doesn't work (no time to research)
				if(value == 1.0f) value = 1.1f; // cheap workaround
				colorFilter.setHue(value);
				break;
			case Saturation:
				colorFilter.setSaturation(value);
				break;
			case Brightness:
				colorFilter.setBrightness(value);
				break;
			case Contrast:
				colorFilter.setContrast(value);
				break;
			default:
				log.error("color filter mismatch! [{}]", filter.toString());
			}
			AdjustType lastAdjust = colorFilter.getLastAdjust();
			if(adjustType != null && lastAdjust != null) {
				log.debug("lastAdjust: {}, currentAdjust: {}", lastAdjust, adjustType);
				if(AdjustType.Hs.equals(adjustType)) {
					colorFilter.resetRgb();
				}
				else {
					colorFilter.resetHscb(true, false);
				}
				if(!adjustType.equals(lastAdjust)) {
					unscaledReferenceImage = ImageUtil.deepCopy(unscaledDisplayImage);
				}
			}
			unscaledDisplayImage = colorFilter.filter(unscaledReferenceImage, null);
	    	displayImage = EditorImageUtil.rotate(unscaledDisplayImage, null, null, scale, scale);
	    	colorFilter.printValues();
	    	return null;
		}

		@Override
		protected void done() {
			boolean resetRgb = AdjustType.Hs.equals(adjustType);
			boolean resetHs = AdjustType.Rgb.equals(adjustType);
			notifyModelChanged(resetRgb, resetHs, false);
			logTime();
		}

		@Override
		public ImageFilterOps getMetaData() {
			return filterOp;
		}
	}
	
	/**
	 * @author Adam Zimowski (mrazjava)
	 */
	class FilterWorker extends AbstractFilterWorker implements FilterListener {

		/**
		 * pass number in the sequence of transformation tasks
		 */
		protected int passNo;
		
		protected long startTime;
		
		protected AbstractBufferedImageOp filter;
		
		public FilterWorker(AbstractBufferedImageOp filter) {
			this.filter = filter;
		}

		@Override
		protected Void doInBackground() throws Exception {
			//notifyModelAboutToChange(); // uncomment if you want wait cursor
			log.trace("on EDT ? {}", SwingUtilities.isEventDispatchThread());
			startTime = System.currentTimeMillis();
			filter.setFilterListener(this);
			passNo = 1;
	    	unscaledDisplayImage = filter.filter(unscaledDisplayImage, null);
	    	passNo = 2;
	    	unscaledReferenceImage = filter.filter(unscaledReferenceImage, null);
	    	passNo = 3;
	    	displayImage = EditorImageUtil.rotate(unscaledDisplayImage, null, null, scale, scale);
			return null;
		}

		@Override
		protected void done() {
			super.done();
			boolean resetColors = getResetColorFilters();
			if(resetColors) {
				unscaledReferenceImage = ImageUtil.deepCopy(unscaledDisplayImage);
				colorFilter.resetRgb();
				colorFilter.resetHscb(true, true);
			}
			notifyModelChanged(resetColors, resetColors, resetColors);
			logTime();
		}
		
		private boolean getResetColorFilters() {
			ImageFilterOps op = filter.getMetaData();
			switch(op) {
			case Cartoonize:
			case Chessboard:
			case Daemonize:
			case Emboss:
			case Grayscale:
			case Marble:
			case Negative:
			case OldPhoto:
			case Posterize:
			case Solarize:
			case Stamp:
			case Hue:
			case Saturation:
				// these filters recompute pixel colors so color filters should 
				// be reset
				return true;
			default:
				return false;
			}
		}
		
		protected void logTime() {
			long endTime = System.currentTimeMillis();
			log.info("{} TIME [ms]: {}", filter.toString(), (endTime-startTime));
		}

		@Override
		public void filterInitialize() {
			ImageFilterMonitorEvent event = new ImageFilterMonitorEvent(filter.getMetaData(), ImageFilterMonitorEvent.PHASE_START);
			event.setPctComplete(0);
			event.setStatus("initializing pass " + passNo);
			getBus().post(event);			
		}

		@Override
		public void filterStart(int totalPixels) {
			ImageFilterMonitorEvent event = new ImageFilterMonitorEvent(filter.getMetaData(), ImageFilterMonitorEvent.PHASE_START);
			if(passNo > 0) {
				event.setPctComplete(passNo == 1 ? 0 : 50);
				event.setStatus("pass " + passNo);
			}
			getBus().post(event);
		}

		@Override
		public void filterProgress(int percentComplete) {
			int percent = percentComplete;
			if(passNo > 0) {
				percent /= 2;
				if(passNo == 2) percent += 50;
			}
			ImageFilterMonitorEvent event = new ImageFilterMonitorEvent(filter.getMetaData(), ImageFilterMonitorEvent.PHASE_PROGRESS);
			event.setPctComplete(percent);
			getBus().post(event);
		}

		@Override
		public void filterDone() {
			ImageFilterMonitorEvent event = new ImageFilterMonitorEvent(filter.getMetaData(), ImageFilterMonitorEvent.PHASE_FINALIZE);
			event.setPctComplete(passNo == 1 ? 50 : 100);
			if(passNo > 0) event.setStatus("finalizing pass " + passNo);
			getBus().post(event);
		}

		@Override
		public ImageFilterOps getMetaData() {
			return filter.getMetaData();
		}
	};
	
	private EventBus eventBus = null;
	private EventBus getBus() {
		if(eventBus == null) eventBus = EventBusManager.getInstance().getBus();
		return eventBus;
	}

	@Override
	public BufferedImage getModifiedImage() {
		return unscaledDisplayImage;
	}
}