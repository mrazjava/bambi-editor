package org.zimowski.bambi.editor.studio;

import java.applet.AppletContext;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.commons.ImageUtil;
import org.zimowski.bambi.controls.dialog.image.ImageChooser;
import org.zimowski.bambi.controls.dialog.login.LoginDialog;
import org.zimowski.bambi.controls.dialog.login.LoginDialogAdapter;
import org.zimowski.bambi.controls.dialog.print.PrintDialog;
import org.zimowski.bambi.editor.ViewportMouseListener;
import org.zimowski.bambi.editor.config.ConfigManager;
import org.zimowski.bambi.editor.config.Configuration;
import org.zimowski.bambi.editor.config.ImageOutputConfigFacade;
import org.zimowski.bambi.editor.config.ImageOutputFormat;
import org.zimowski.bambi.editor.config.ImageOutputSettings;
import org.zimowski.bambi.editor.customui.BambiToolbar;
import org.zimowski.bambi.editor.customui.FilmPane;
import org.zimowski.bambi.editor.customui.slider.BambiSliderUI;
import org.zimowski.bambi.editor.customui.slider.SliderDecor;
import org.zimowski.bambi.editor.customui.statusbar.CheckboxCell;
import org.zimowski.bambi.editor.customui.statusbar.ImageTaskCell;
import org.zimowski.bambi.editor.customui.statusbar.ProgressBarCell;
import org.zimowski.bambi.editor.customui.statusbar.RgbCell;
import org.zimowski.bambi.editor.customui.statusbar.StatusBar;
import org.zimowski.bambi.editor.customui.statusbar.TextCell;
import org.zimowski.bambi.editor.filters.ImageFilterOps;
import org.zimowski.bambi.editor.plugins.api.ExportAbortInformer;
import org.zimowski.bambi.editor.plugins.api.ExportProgressMonitor;
import org.zimowski.bambi.editor.plugins.api.ExportStateMonitor;
import org.zimowski.bambi.editor.plugins.api.ImageExportDef;
import org.zimowski.bambi.editor.plugins.api.ImageExporter;
import org.zimowski.bambi.editor.plugins.api.TextEncrypter;
import org.zimowski.bambi.editor.studio.cam.CamFilterOps;
import org.zimowski.bambi.editor.studio.cam.CamInitializationObserver;
import org.zimowski.bambi.editor.studio.cam.CamPanel;
import org.zimowski.bambi.editor.studio.cam.FpsObserver;
import org.zimowski.bambi.editor.studio.cam.VideoDevice;
import org.zimowski.bambi.editor.studio.eventbus.EventBusManager;
import org.zimowski.bambi.editor.studio.eventbus.events.AbortFilterQueueEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.CamFilterEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.CamMirrorFilterEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.CamPictureRequestEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.CamRgbFilterEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ImageFilterEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ImageFilterQueueEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ImageLoadEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelLifecycleEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelLifecycleEvent.ModelPhase;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelRotateEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.RotateEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ScaleEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.SelectorBackgroundEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.SelectorTypeEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.SelectorVisibilityEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.SelectorVisibilityEvent.Command;
import org.zimowski.bambi.editor.studio.image.EditorImageControllers;
import org.zimowski.bambi.editor.studio.image.ImagePanel;
import org.zimowski.bambi.editor.studio.image.ImageTransformListener.RotateDirection;
import org.zimowski.bambi.editor.studio.image.SelectorObserver;
import org.zimowski.bambi.editor.studio.resources.toolbar.ToolbarIcons;
import org.zimowski.bambi.jhlabs.image.ScaleFilter;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * @author Sun Microsystems (0.1)
 * @author Adam Zimowski (mrazjava) (1.0+)
 * 
 * @version 0.01 - initial release; container for Accessible Scroll Demo 
 * 	http://www.java2s.com/Code/Java/Swing-JFC/AccessibleScrollDemo.htm
 * @version 0.1 - 2010, turned into closed source applet uploader.
 * @version 1.0 - 2012/13, major cleanup; bug fixes; added ruler, status bar, 
 * 	PNG support, image filters
 */
public class Editor extends JPanel implements 
		ItemListener, ImageOutputConfigFacade, SelectorObserver, 
		CamInitializationObserver, FpsObserver, ExportAbortInformer, 
		WindowListener {

	private static final long serialVersionUID = 628351245437307334L;
	
	private static final Logger log = LoggerFactory.getLogger(Editor.class);
	
	/**
	 * Width, in pixels, of the panel containing RGB sliders
	 */
	private static final int RGB_PANEL_WIDTH = 30;
	
	/**
	 * Width, in pixels, of the panel containing HSB sliders
	 */
	private static final int HSB_PANEL_WIDTH = 30;
	
	/**
	 * Possible viewport configurations within the editor.
	 * 
	 * @author Adam Zimowski (mrazjava)
	 */
	public enum EditorView {
		Dummy, Welcome, Picture, WebCam
	}
	
	private EditorView currentView = null;
	
	/**
	 * States that picture upload can be in.
	 * 
	 * @author Adam Zimowski (mrazjava)
	 */
	public enum ImageExportState {
		Idle, InProgress, Aborting
	}
	
	private JRadioButton pic1Radio;
	
	private JRadioButton pic2Radio;
	
	private JRadioButton pic3Radio;
	
	private JRadioButton pic4Radio;
	
	private JButton imageExportButton;
	
	private JComboBox zoomCombo;
	
	private boolean abort = false;
	
	private JComboBox jpgBgColorCombo;
	
	private StatusBar statusBar;
	
	private Configuration config;
	
	private JPanel cardPanel = new JPanel(new CardLayout());
	
	private CamPanel camPanel = new CamPanel();
	
	private ImagePanel imgPanel;
	
	private WelcomePanel welcomePanel = new WelcomePanel();
	
	private JPanel dummyPanel = new JPanel();
	
	private JToggleButton webcamButton;
	
	private ViewportMouseListener displayImageMouseListener;
	
	private ImageExportState uploadState = ImageExportState.Idle;
	
	private JButton filterAbortButton;
	
	private JToolBar imageToolbar;
	
	private JToolBar camToolbar;
	
	private JDialog loginDialog;
	
	/**
	 * handler for the {@link LoginDialog} events
	 */
	private LoginDialogAdapter loginDialogAdapter = new LoginDialogAdapter() {		
		@Override
		public void cancel() {
			super.cancel();
			statusBar.getUploadCell().exportAborted(0);
			statusBar.getUploadCell().exportFinished(new Date());
		}
	};
	
    private ItemListener camStatusBarListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				setCursorBusy();
				VideoDevice device = (VideoDevice)e.getItem();
				camPanel.stopWebCamSession();
				camPanel.startWebCamSession(device);
				log.debug("selected {}", device.getName());
			}
		}
    };
    
    private JPanel topPanel;
    
    private JPanel rgbPanel;
    
    private JPanel hscbPanel;
    
    private FilmPane filmPane;
    
    private JScrollPane filmPaneContainer; 


	public Editor() {
		config = ConfigManager.getInstance().getConfiguration();
		EventBusManager.getInstance().registerWithBus(this);
		displayImageMouseListener = new ViewportMouseListenerImpl();        
    }

	/**
	 * Builds GUI for display. Requires that configuration settings are set and 
	 * available. Parameters of a first photo are used if more than one photo 
	 * is defined. 
	 * 
	 * @throws IllegalStateException if {@link #config} is null
	 */
	void initialize() {

		if(config == null)
			throw new IllegalStateException("config is missing!");
		
		setOpaque(true);
		setLayout(new BorderLayout());

        camPanel.setFpsObserver(this);
        camPanel.setCamInitObserver(this);
        camPanel.setViewportMouseListener(displayImageMouseListener);
        
        JPanel picturePanel = new JPanel(new BorderLayout());
        picturePanel.setBorder(
    		BorderFactory.createCompoundBorder(
    			new EtchedBorder(), new EmptyBorder(2,2,2,2)
    		)
        );
        
        imgPanel = new ImagePanel(new EditorImageControllers() {
			@Override
			public SelectorObserver getSelectorObserver() {
				return Editor.this;
			}
			@Override
			public ViewportMouseListener getMouseInputListener() {
				return displayImageMouseListener;
			}
			@Override
			public ImageOutputConfigFacade getImageOutputConfigFacade() {
				return Editor.this;
			}
		});
        
        topPanel = buildTopPanel();
        
        cardPanel.add(dummyPanel, EditorView.Dummy.toString());
        cardPanel.add(welcomePanel, EditorView.Welcome.toString());
        cardPanel.add(imgPanel, EditorView.Picture.toString());
        cardPanel.add(camPanel, EditorView.WebCam.toString());
        
        rgbPanel = buildRgbPanel();
        hscbPanel = buildHsbcPanel();
        
        picturePanel.add(topPanel, BorderLayout.NORTH);
        picturePanel.add(rgbPanel, BorderLayout.WEST);
        picturePanel.add(cardPanel, BorderLayout.CENTER);
        picturePanel.add(hscbPanel, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(7, 0, 0, 0));
        
        filmPane = new FilmPane();
        filmPaneContainer = new JScrollPane(filmPane);
        filmPaneContainer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        
        bottomPanel.add(filmPaneContainer);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(statusBar = buildStatusBar());

        camToolbar = buildCamToolbar();
        imageToolbar = buildImageToolbar();
        
        JPanel mainToolbarPane = new JPanel();
        mainToolbarPane.setLayout(new BorderLayout());

        westToolbarPane.setLayout(new BoxLayout(westToolbarPane, BoxLayout.LINE_AXIS));
        eastToolbarPane.setLayout(new BoxLayout(eastToolbarPane, BoxLayout.LINE_AXIS));
        
        eastToolbarPane.add(buildCommonToolbar());

        mainToolbarPane.add(westToolbarPane, BorderLayout.CENTER);
        mainToolbarPane.add(eastToolbarPane, BorderLayout.EAST);
        
        add(mainToolbarPane, BorderLayout.NORTH);
        add(picturePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        setBorder(BorderFactory.createEmptyBorder(5,15,0,15));
        setSelectorControls(config.isSelectorVisible());
	}
	
	private JPanel eastToolbarPane = new JPanel();
	private JPanel westToolbarPane = new JPanel();

	private void setView(EditorView view) {
		CardLayout layout = (CardLayout)cardPanel.getLayout();
		layout.show(cardPanel, view.toString());
		statusBar.setView(view);
		currentView = view;
		switch(view) {
		case Picture:
			westToolbarPane.remove(camToolbar);
	        westToolbarPane.add(imageToolbar, BorderLayout.CENTER);
	        enableImageControls(printImage != null);
			filmPaneContainer.setVisible(true);
			camPanel.stopWebCamSession();
			webcamButton.setToolTipText("Start Web Cam Session");
			//setStatusbarImagePath(statusBar.getUploadCell());
			break;
		case WebCam:
			westToolbarPane.remove(imageToolbar);
			westToolbarPane.add(camToolbar, BorderLayout.CENTER);
			topPanel.setVisible(false);
			rgbPanel.setVisible(camRgbButton.isSelected());
			hscbPanel.setVisible(false);
			filmPaneContainer.setVisible(true);
			webcamButton.setToolTipText("Stop Web Cam Session");
			openCamDisplay();
			break;
		case Welcome:
			westToolbarPane.remove(camToolbar);
			westToolbarPane.add(imageToolbar, BorderLayout.CENTER);
			topPanel.setVisible(false);
			rgbPanel.setVisible(false);
			hscbPanel.setVisible(false);
			filmPaneContainer.setVisible(false);
			camPanel.stopWebCamSession();
			webcamButton.setToolTipText("Start Web Cam Session");
		case Dummy: break;
		default:
			log.error("unsupported view: {}", view.toString());
		}
		westToolbarPane.repaint();
	}
	
	private JToggleButton camRgbButton;
	
	private JToolBar buildCommonToolbar() {
		JToolBar bar = new JToolBar();
		bar.setVisible(true);
		bar.setRollover(true);
		bar.setFocusable(false);
		bar.setFloatable(false);
		bar.setLayout(new BorderLayout());
		bar.setBorder(new EtchedBorder());
		
        webcamButton = new JToggleButton(ToolbarIcons.Webcam.getIcon());
        webcamButton.addActionListener(new WebCamListener());
		
		bar.add(webcamButton);
		
		return bar;
	}
	
	private JToolBar buildCamToolbar() {

		BambiToolbar bar = new BambiToolbar();
		bar.setRollover(true);
		bar.setFocusable(false);
		bar.setFloatable(false);
		bar.setBorder(new EtchedBorder());
		bar.add(Box.createHorizontalStrut(3));
		
		ButtonGroup filterGroup = new ButtonGroup();

		JToggleButton camNoFilterButton = new JToggleButton(ToolbarIcons.Stream.getIcon());
		camNoFilterButton.setToolTipText(ToolbarIcons.Stream.getDescription());
		camNoFilterButton.setSelected(true);
		camNoFilterButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				getBus().post(new CamFilterEvent(CamFilterOps.None));
			}
		});
		filterGroup.add(camNoFilterButton);
		bar.add(camNoFilterButton);
		
		JButton camTakePictureButton = new JButton(ToolbarIcons.Camera.getIcon());
		camTakePictureButton.setToolTipText(ToolbarIcons.Camera.getDescription());
		camTakePictureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new CamPictureRequestEvent());
			}
		});
		bar.add(camTakePictureButton);
		
		camRgbButton = new JToggleButton(ToolbarIcons.Rgb.getIcon());
		camRgbButton.setToolTipText(ToolbarIcons.Rgb.getDescription());
		camRgbButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				rgbPanel.setVisible(camRgbButton.isSelected());
				getBus().post(new CamFilterEvent(CamFilterOps.Rgb));
			}
		});
		filterGroup.add(camRgbButton);
		bar.add(camRgbButton);
		
		JToggleButton camCannyEdgeButton = new JToggleButton(ToolbarIcons.Deamonize.getIcon());
		camCannyEdgeButton.setToolTipText(ToolbarIcons.Deamonize.toString());
		camCannyEdgeButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				getBus().post(new CamFilterEvent(CamFilterOps.Canny));
			}
		});
		filterGroup.add(camCannyEdgeButton);
		bar.add(camCannyEdgeButton);
		
		JToggleButton camNegativeButton = new JToggleButton(ToolbarIcons.Negative.getIcon());
		camNegativeButton.setToolTipText(ToolbarIcons.Negative.toString());
		camNegativeButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				getBus().post(new CamFilterEvent(CamFilterOps.Negative));
			}
		});
		filterGroup.add(camNegativeButton);
		bar.add(camNegativeButton);
		
		JToggleButton camSolarizeButton = new JToggleButton(ToolbarIcons.Solarize.getIcon());
		camSolarizeButton.setToolTipText(ToolbarIcons.Solarize.toString());
		camSolarizeButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				getBus().post(new CamFilterEvent(CamFilterOps.Solarize));
			}
		});
		filterGroup.add(camSolarizeButton);
		bar.add(camSolarizeButton);
		
		JToggleButton camGrayscaleButton = new JToggleButton(ToolbarIcons.Grayscale.getIcon());
		camGrayscaleButton.setToolTipText(ToolbarIcons.Grayscale.getDescription());
		camGrayscaleButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				getBus().post(new CamFilterEvent(CamFilterOps.Grayscale));
			}
		});
		filterGroup.add(camGrayscaleButton);
		bar.add(camGrayscaleButton);
		
		bar.addSeparator(new EmptyBorder(8,5,8,0));

		final JToggleButton camRedMirrorButton = new JToggleButton(ToolbarIcons.MirrorRed.getIcon());
		final JToggleButton camGreenMirrorButton = new JToggleButton(ToolbarIcons.MirrorGreen.getIcon());
		final JToggleButton camBlueMirrorButton = new JToggleButton(ToolbarIcons.MirrorBlue.getIcon());
		
		final JToggleButton camMirrorButton = new JToggleButton(ToolbarIcons.Mirror.getIcon());
		camMirrorButton.setToolTipText("Enable Pixel Mirrors");
		camMirrorButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean selected = camMirrorButton.isSelected();
				camRedMirrorButton.setEnabled(selected);
				camGreenMirrorButton.setEnabled(selected);
				camBlueMirrorButton.setEnabled(selected);
				getBus().post(new CamFilterEvent(CamFilterOps.Mirror));
				getBus().post(new CamMirrorFilterEvent(CamFilterEvent.BAND_RED, camRedMirrorButton.isSelected()));
				getBus().post(new CamMirrorFilterEvent(CamFilterEvent.BAND_GREEN, camGreenMirrorButton.isSelected()));
				getBus().post(new CamMirrorFilterEvent(CamFilterEvent.BAND_BLUE, camBlueMirrorButton.isSelected()));
			}
		});
		filterGroup.add(camMirrorButton);
		bar.add(camMirrorButton);
		
		camRedMirrorButton.setEnabled(false);
		camRedMirrorButton.setSelected(true);
		camRedMirrorButton.setToolTipText(ToolbarIcons.MirrorRed.getDescription());
		camRedMirrorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean selected = camRedMirrorButton.isSelected();
				getBus().post(new CamMirrorFilterEvent(CamFilterEvent.BAND_RED, selected));
			}
		});
		bar.add(camRedMirrorButton);

		camGreenMirrorButton.setEnabled(false);
		camGreenMirrorButton.setToolTipText(ToolbarIcons.MirrorGreen.getDescription());
		camGreenMirrorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean selected = camGreenMirrorButton.isSelected();
				getBus().post(new CamMirrorFilterEvent(CamFilterEvent.BAND_GREEN, selected));
			}
		});
		bar.add(camGreenMirrorButton);
		
		camBlueMirrorButton.setEnabled(false);
		camBlueMirrorButton.setToolTipText(ToolbarIcons.MirrorBlue.getDescription());
		camBlueMirrorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean selected = camBlueMirrorButton.isSelected();
				getBus().post(new CamMirrorFilterEvent(CamFilterEvent.BAND_BLUE, selected));
			}
		});
		bar.add(camBlueMirrorButton);		
		
		bar.add(Box.createHorizontalGlue());
		
		return bar;
	}
	
	private File lastOpenedDirectory = null;
	private List<AbstractButton> imageToolbarButtons;
	
	private JToolBar buildImageToolbar() {

		BambiToolbar bar = new BambiToolbar();

		bar.setRollover(true);
		bar.setFocusable(false);
		bar.setFloatable(false);
		bar.setBorder(new EtchedBorder());
		bar.add(Box.createHorizontalStrut(3));
		
		JButton openButton = new JButton(ToolbarIcons.Open.getIcon());
		openButton.setToolTipText(ToolbarIcons.Open.getDescription());
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Editor.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				UIManager.put("FileChooser.readOnly", Boolean.TRUE);
				JFileChooser fc = new ImageChooser();
				Window win = SwingUtilities.getWindowAncestor(Editor.this);
				if(lastOpenedDirectory == null) lastOpenedDirectory = new File(System.getProperty("user.home"));
				if(lastOpenedDirectory != null) fc.setCurrentDirectory(lastOpenedDirectory);
				int result = fc.showDialog(win, "Open");
				if(result == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					lastOpenedDirectory = file.getParentFile();
					ImageLoadEvent ev = new ImageLoadEvent(file);
					getBus().post(ev);
				}
				Editor.this.setCursor(Cursor.getDefaultCursor());
			}
		});
		bar.add(openButton);
		
		imageToolbarButtons = new LinkedList<AbstractButton>();

        JButton printButton = new JButton(ToolbarIcons.Print.getIcon());
        printButton.setToolTipText(ToolbarIcons.Print.getDescription());
        printButton.addActionListener(new ActionListener() {
        	
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						PrintDialog dialog = new PrintDialog("Print");						
						dialog.display(Editor.this.getTopLevelAncestor(), printImage);
					}
				});
			}
		});
        bar.add(printButton);
        imageToolbarButtons.add(printButton);

        bar.addSeparator(new EmptyBorder(8,1,8,3));

        if(config.isRulerToggleVisible()) {
	        JToggleButton rulerButton = new JToggleButton(ToolbarIcons.Metrics.getIcon(), config.isRulerVisible());
	        rulerButton.setActionCommand("RULER");
	        rulerButton.addItemListener(this);
	        rulerButton.setToolTipText(ToolbarIcons.Metrics.getDescription() + " " + (config.isRulerVisible() ? "ON" : "OFF"));
	        bar.add(rulerButton);
	        imageToolbarButtons.add(rulerButton);
        }

        JButton rotateLeftButton = new JButton(ToolbarIcons.RotateLeft.getIcon());
        rotateLeftButton.setToolTipText(ToolbarIcons.RotateLeft.getDescription());
        rotateLeftButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new RotateEvent(RotateDirection.LEFT));
			}
		});
        bar.add(rotateLeftButton);
        imageToolbarButtons.add(rotateLeftButton);
                
        JButton rotateRightButton = new JButton(ToolbarIcons.RotateRight.getIcon());
        rotateRightButton.setToolTipText(ToolbarIcons.RotateRight.getDescription());
        rotateRightButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new RotateEvent(RotateDirection.RIGHT));
			}
		});
        bar.add(rotateRightButton);
        imageToolbarButtons.add(rotateRightButton);
        
        imageExportButton = new JButton();
        imageExportButton.addActionListener(new ExportButtonListener());
        setUploadState(ImageExportState.Idle);
        bar.add(imageExportButton);
        imageToolbarButtons.add(imageExportButton);

        bar.addSeparator(new EmptyBorder(0,4,0,1));

		JButton posterizeButton = new JButton(ToolbarIcons.Posterize.getIcon());
        posterizeButton.setToolTipText(ToolbarIcons.Posterize.getDescription());
        posterizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new ImageFilterEvent(ImageFilterOps.Posterize));
			}
		});
        bar.add(posterizeButton);
        imageToolbarButtons.add(posterizeButton);

        JButton invertButton = new JButton(ToolbarIcons.Negative.getIcon());
        invertButton.setToolTipText(ToolbarIcons.Negative.getDescription());
        invertButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new ImageFilterEvent(ImageFilterOps.Negative));
			}
		});
        bar.add(invertButton);
        imageToolbarButtons.add(invertButton);

        JButton solarizeButton = new JButton(ToolbarIcons.Solarize.getIcon());
        solarizeButton.setToolTipText(ToolbarIcons.Solarize.getDescription());
        solarizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new ImageFilterEvent(ImageFilterOps.Solarize));
			}
		});
        bar.add(solarizeButton);
        imageToolbarButtons.add(solarizeButton);

        JButton deamonizeButton = new JButton(ToolbarIcons.Deamonize.getIcon());
        deamonizeButton.setToolTipText(ToolbarIcons.Deamonize.getDescription());
        deamonizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new ImageFilterEvent(ImageFilterOps.Daemonize));
			}
		});
        bar.add(deamonizeButton);
        imageToolbarButtons.add(deamonizeButton);

        JButton embossButton = new JButton(ToolbarIcons.Emboss.getIcon());
        embossButton.setToolTipText(ToolbarIcons.Emboss.getDescription());
        embossButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new ImageFilterEvent(ImageFilterOps.Emboss));
			}
        });
        bar.add(embossButton);
        imageToolbarButtons.add(embossButton);

        JButton kaleiButton = new JButton(ToolbarIcons.Kaleidoscope.getIcon());
        kaleiButton.setToolTipText(ToolbarIcons.Kaleidoscope.getDescription());
        kaleiButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new ImageFilterEvent(ImageFilterOps.Kaleidoscope));
			}
		});
        bar.add(kaleiButton);
        imageToolbarButtons.add(kaleiButton);

        JButton tritoneButton = new JButton(ToolbarIcons.Grayscale.getIcon());
        tritoneButton.setToolTipText(ToolbarIcons.Grayscale.getDescription());
        tritoneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new ImageFilterEvent(ImageFilterOps.Grayscale));
			}
		});
        bar.add(tritoneButton);
        imageToolbarButtons.add(tritoneButton);

        JButton sepiaButton = new JButton(ToolbarIcons.OldPhoto.getIcon());
        sepiaButton.setToolTipText(ToolbarIcons.OldPhoto.getDescription());
        sepiaButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new ImageFilterEvent(ImageFilterOps.OldPhoto));
			}
		});
        bar.add(sepiaButton);
        imageToolbarButtons.add(sepiaButton);

        JButton ditherButton = new JButton(ToolbarIcons.Chessboard.getIcon());
        ditherButton.setToolTipText(ToolbarIcons.Chessboard.getDescription());
        ditherButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new ImageFilterEvent(ImageFilterOps.Chessboard));
			}
		});
        bar.add(ditherButton);
        imageToolbarButtons.add(ditherButton);

        JButton marbleButton = new JButton(ToolbarIcons.Marble.getIcon());
        marbleButton.setToolTipText(ToolbarIcons.Marble.getDescription());
        marbleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new ImageFilterEvent(ImageFilterOps.Marble));
			}
		});
        bar.add(marbleButton);
        imageToolbarButtons.add(marbleButton);

        JButton stampButton = new JButton(ToolbarIcons.Stamp.getIcon());
        stampButton.setToolTipText(ToolbarIcons.Stamp.getDescription());
        stampButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new ImageFilterEvent(ImageFilterOps.Stamp));
			}
		});
        bar.add(stampButton);
        imageToolbarButtons.add(stampButton);

        JButton lightButton = new JButton(ToolbarIcons.Cartoonize.getIcon());
        lightButton.setToolTipText(ToolbarIcons.Cartoonize.getDescription());
        lightButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new ImageFilterEvent(ImageFilterOps.Cartoonize));
			}
		});
        bar.add(lightButton);
        imageToolbarButtons.add(lightButton);

        JButton twirlButton = new JButton(ToolbarIcons.Twirl.getIcon());
        twirlButton.setToolTipText(ToolbarIcons.Twirl.getDescription());
        twirlButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new ImageFilterEvent(ImageFilterOps.Twirl));
			}
		});
        bar.add(twirlButton);
        imageToolbarButtons.add(twirlButton);

        JButton flipButton = new JButton(ToolbarIcons.Mirror.getIcon());
        flipButton.setToolTipText(ToolbarIcons.Mirror.getDescription());
        flipButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new ImageFilterEvent(ImageFilterOps.Mirror));
			}
		});
        bar.add(flipButton);
        imageToolbarButtons.add(flipButton);
        
        bar.add(Box.createHorizontalGlue());
        enableImageControls(printImage != null);

        return bar;
	}
	
	private JPanel buildTopPanel() {
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		
		JPanel westTopPanel = new JPanel();	// left corner
		JPanel eastTopPanel = new JPanel();	// right corner

        try {
        	URL abortImageUrl = getClass().getResource(Configuration.RESOURCE_PATH + "cancel12x12.png");
        	Image abortImage = ImageIO.read(abortImageUrl);
        	filterAbortButton = new JButton(new ImageIcon(abortImage));
        	filterAbortButton.setVisible(false);
        	filterAbortButton.setToolTipText("Abort Pending Filters");
        	filterAbortButton.setPreferredSize(new Dimension(abortImage.getWidth(null)+8, abortImage.getHeight(null)+8));
        	filterAbortButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getBus().post(new AbortFilterQueueEvent());
					filterAbortButton.setEnabled(false);
				}
			});
        	westTopPanel.setBorder(new EmptyBorder(-3, -2, 0, 0));
        	westTopPanel.add(filterAbortButton);
        } catch(Exception e) {
        	log.error("{}; filter abort will be disabled", e.getMessage());
        }

		westTopPanel.setPreferredSize(new Dimension(RGB_PANEL_WIDTH, 0));
		eastTopPanel.setPreferredSize(new Dimension(HSB_PANEL_WIDTH, 0));
		
		JPanel centerTopPanel = new JPanel();
		centerTopPanel.setLayout(new BoxLayout(centerTopPanel, BoxLayout.Y_AXIS));

        JPanel outputTypeRadioPanel = buildPictureTypeRadioPanel();
        
        JPanel outputTypeZoomPanel = new JPanel(new BorderLayout());
        outputTypeZoomPanel.setBorder(new EmptyBorder(0, -5, 0, -10));
        outputTypeZoomPanel.add(outputTypeRadioPanel, BorderLayout.WEST);
        outputTypeZoomPanel.add(buildScaleViewPanel(), BorderLayout.CENTER);
        
        centerTopPanel.add(outputTypeZoomPanel);
        
        topPanel.add(westTopPanel, BorderLayout.WEST);
        topPanel.add(centerTopPanel, BorderLayout.CENTER);
        topPanel.add(eastTopPanel, BorderLayout.EAST);

        return topPanel;
	}
	
	@SuppressWarnings("restriction")
	private JDialog buildLoginDialog() {
		
		JFrame parent = (JFrame)Editor.this.getTopLevelAncestor();
		LoginDialog d = new LoginDialog(
				parent, 
				"Login Required", 
				ModalityType.APPLICATION_MODAL);
		
		d.addLoginDialogListener(loginDialogAdapter);
		d.setTitle("Login Required");
		d.setPrompt(config.getAuthenticationPrompt());
		String host = null;
		try { host = new URL(config.getRemoteHost()).getHost(); }
		catch(MalformedURLException e) {
			host = config.getBusinessNameShort();
		}
		if(StringUtils.isNotEmpty(host)) d.setServerName(host);
		d.setUndecorated(true);
		d.initialize();
		d.pack();
		d.setLocationRelativeTo(parent);
		// FIXME: once on 1.7 switch to public API
		com.sun.awt.AWTUtilities.setWindowOpacity(d, 0.85f);
		
		return d;
	}
	
	private JSlider redSlider;
	private JSlider greenSlider;
	private JSlider blueSlider;
	
	/**
	 * Checks the look and feel that is currently in use and determines if 
	 * custom UI effects are supported. Custom effects are minor changes to 
	 * certain components such as slider to personalize the Bambi experience. 
	 * This test returns true for look and feels that have been tested to work 
	 * with custom effects. A check call to this method is recommended 
	 * whenever setUI call is invoked on a Swing component (for custom Bambi 
	 * UI).
	 * 
	 * @return true if the LNF in use supports custom GUI effects; false if not
	 */
	private boolean isCustomGuiSupported() {
		// at this point there are no known incompatibility issues, but should 
		// they arise in the future, follow example below to exclude 
		// incompatibile look and feels.
		/*
		Class<?> lnf = UIManager.getLookAndFeel().getClass();
		return lnf.equals(lnf.equals(MetalLookAndFeel.class);
		*/
		return true;
	}
	
	private JPanel buildRgbPanel() {

		JPanel rgbPanel = new JPanel();
		rgbPanel.setBorder(new EmptyBorder(0,0,0,4));
		rgbPanel.setLayout(new BoxLayout(rgbPanel, BoxLayout.Y_AXIS));
		rgbPanel.setPreferredSize(new Dimension(RGB_PANEL_WIDTH, getHeight()));
		
		redSlider = buildRgbAdjustSlider();
        redSlider.setToolTipText("Adjust Red");
        redSlider.setForeground(SliderDecor.Red.getColor());
        if(isCustomGuiSupported()) {
        	redSlider.setUI(new BambiSliderUI(redSlider, SliderDecor.Red));
        }
        redSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(resetRgbSliders) return;
				if(!redSlider.getValueIsAdjusting()) {
					if(EditorView.Picture.equals(currentView))
						getBus().post(new ImageFilterEvent(ImageFilterOps.Red, redSlider.getValue()));
					else if(EditorView.WebCam.equals(currentView))
						getBus().post(new CamRgbFilterEvent(CamFilterEvent.BAND_RED, redSlider.getValue()));
				}
			}
		});
        
        greenSlider = buildRgbAdjustSlider();
        greenSlider.setToolTipText("Adjust Green");
        greenSlider.setForeground(SliderDecor.Green.getColor());
        if(isCustomGuiSupported()) {
        	greenSlider.setUI(new BambiSliderUI(greenSlider, SliderDecor.Green));
        }
        greenSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(resetRgbSliders) return;
				if(!greenSlider.getValueIsAdjusting()) {
					if(EditorView.Picture.equals(currentView))
						getBus().post(new ImageFilterEvent(ImageFilterOps.Green, greenSlider.getValue()));
					else if(EditorView.WebCam.equals(currentView))
						getBus().post(new CamRgbFilterEvent(CamFilterEvent.BAND_GREEN, greenSlider.getValue()));					
				}
			}
		});

        blueSlider = buildRgbAdjustSlider();
        blueSlider.setToolTipText("Adjust Blue");
        blueSlider.setForeground(SliderDecor.Blue.getColor());
        if(isCustomGuiSupported()) {
        	blueSlider.setUI(new BambiSliderUI(blueSlider, SliderDecor.Blue));
        }
        blueSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(resetRgbSliders) return;
				if(!blueSlider.getValueIsAdjusting()) {
					if(EditorView.Picture.equals(currentView))
						getBus().post(new ImageFilterEvent(ImageFilterOps.Blue, blueSlider.getValue()));
					else if(EditorView.WebCam.equals(currentView))
						getBus().post(new CamRgbFilterEvent(CamFilterEvent.BAND_BLUE, blueSlider.getValue()));					
				}
			}
		});

		rgbPanel.add(redSlider);
		rgbPanel.add(Box.createVerticalStrut(5));
		rgbPanel.add(greenSlider);
		rgbPanel.add(Box.createVerticalStrut(5));
		rgbPanel.add(blueSlider);
		

		return rgbPanel;
	}
	
	private JSlider hueSlider;
	private JSlider saturationSlider;
	private JSlider brightnessSlider;
	private JSlider contrastSlider;
	
	/**
	 * Builds panel with four vertical sliders to adjust hue, saturation, 
	 * contrast and brightness.
	 * 
	 * @return
	 */
	private JPanel buildHsbcPanel() {
		
		JPanel hsbPanel = new JPanel();
		hsbPanel.setLayout(new BoxLayout(hsbPanel, BoxLayout.Y_AXIS));
		hsbPanel.setPreferredSize(new Dimension(HSB_PANEL_WIDTH, getHeight()));

		hueSlider = buildHsbcSlider();
		hueSlider.setToolTipText("Adjust Hue");
		if(isCustomGuiSupported()) {
			hueSlider.setUI(new BambiSliderUI(hueSlider, SliderDecor.Hue));
		}
		hueSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(resetHsSliders) return;
				if(!hueSlider.getValueIsAdjusting()) {
					getBus().post(new ImageFilterEvent(ImageFilterOps.Hue, hueSlider.getValue()));
				}
			}
		});
        
		saturationSlider = buildHsbcSlider();
		saturationSlider.setToolTipText("Adjust Saturation");
		if(isCustomGuiSupported()) {
			saturationSlider.setUI(new BambiSliderUI(saturationSlider, SliderDecor.Saturation));
		}
		saturationSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(resetHsSliders) return;
				if(!saturationSlider.getValueIsAdjusting()) {
					getBus().post(new ImageFilterEvent(ImageFilterOps.Saturation, saturationSlider.getValue()));
				}
			}
		});

		brightnessSlider = buildHsbcSlider();
		brightnessSlider.setToolTipText("Adjust Brightness");
		if(isCustomGuiSupported()) {
			brightnessSlider.setUI(new BambiSliderUI(brightnessSlider, SliderDecor.Brigtness));
		}
		brightnessSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(resetCbSliders) return;
				if(!brightnessSlider.getValueIsAdjusting()) {
					getBus().post(new ImageFilterEvent(ImageFilterOps.Brightness, brightnessSlider.getValue()));
				}
			}
		});
		
		contrastSlider = buildHsbcSlider();
		contrastSlider.setToolTipText("Adjust Contrast");
		if(isCustomGuiSupported()) {
			contrastSlider.setUI(new BambiSliderUI(contrastSlider, SliderDecor.Contrast));
		}
		contrastSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(resetCbSliders) return;
				if(!contrastSlider.getValueIsAdjusting()) {
					getBus().post(new ImageFilterEvent(ImageFilterOps.Contrast, contrastSlider.getValue()));
				}
			}
		});

		hsbPanel.add(hueSlider);
		hsbPanel.add(Box.createVerticalStrut(5));
		hsbPanel.add(saturationSlider);
		hsbPanel.add(Box.createVerticalStrut(5));
		hsbPanel.add(brightnessSlider);
		hsbPanel.add(Box.createVerticalStrut(5));
		hsbPanel.add(contrastSlider);

		return hsbPanel;
	}
	
	private JPanel buildScaleViewPanel() {
		
		JPanel scaleViewPanel = new JPanel(
				new FlowLayout(FlowLayout.RIGHT, 10, 0)
		);

        jpgBgColorCombo = buildColorCombo();
        scaleViewPanel.add(jpgBgColorCombo);
        
        zoomCombo = buildScaleCombo();
        scaleViewPanel.add(zoomCombo);
        
        return scaleViewPanel;
	}
	
	private JSlider buildRgbAdjustSlider() {
		JSlider slider = new JSlider(JSlider.VERTICAL);
		slider.setMajorTickSpacing(2);
		slider.setMinorTickSpacing(1);
		slider.setSnapToTicks(true);
		slider.setMinimum(-10);
		slider.setMaximum(10);
		slider.setValue(0);
		return slider;
	}
	
	private JSlider buildHsbcSlider() {
		JSlider slider = new JSlider(JSlider.VERTICAL);
		slider.setMajorTickSpacing(2);
		slider.setMinorTickSpacing(1);
		slider.setSnapToTicks(true);
		slider.setMinimum(-10);
		slider.setMaximum(10);
		slider.setValue(0);
		slider.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		return slider;		
	}
/*
	private JPanel buildCopyrightPanel() {
		
        JPanel copyright = new JPanel();
        copyright.setLayout(new BoxLayout(copyright, BoxLayout.Y_AXIS));
        copyright.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        copyright.add(new JLabel("\u00a9 2010-2013 Adam Zimowski, zimowski74@yahoo.com"));
        JLabel lblCopy2 = new JLabel("Licensed to " + config.getBusinessName());
        lblCopy2.setForeground(Color.GRAY);
        copyright.add(lblCopy2);

        return copyright;
	}
*/
	private File currentImageFile;
	
	private void setStatusbarImagePath(ImageTaskCell cell) {
		String file = currentImageFile == null ? "no image" : currentImageFile.getAbsolutePath();
		cell.setText(file);
		cell.setDefaultText(file);
		cell.repaint();
	}
	
	@Subscribe
	public void onImageOpen(ImageLoadEvent ev) {
		currentImageFile = ev.getImageFile();
		setStatusbarImagePath(statusBar.getUploadCell());
		if(!ev.isAutoLoaded()) {
			resetRgbSliders = resetHsSliders = resetCbSliders = true;
			handleModelReset();
		}
		if(currentImageFile == null) {
			setView(EditorView.Welcome);
			repaint();
		}
		else if(!EditorView.Picture.equals(currentView)) {
			setView(EditorView.Picture);
			repaint();
		}
		setSelectorVisbilityState(showSelectorCheckbox.isSelected());
	}

	private StatusBar buildStatusBar() {
		
		StatusBar statusBar = new StatusBar();
		
		TextCell outputFormatCell = statusBar.getOutputFormatCell();
		TextCell angleCell = statusBar.getAngleCell();
		CheckboxCell selectorPositionCell = statusBar.getSelectorPositionCell();
		CheckboxCell selectorSizeCell = statusBar.getSelectorSizeCell();
		RgbCell rgbCell = statusBar.getRgbCell();
		CheckboxCell fpsCell = statusBar.getFpsCell();
		
		outputFormatCell.setToolTipText("Output Format");
		angleCell.setToolTipText("Rotation Angle");
		selectorPositionCell.setToolTipText("Selector Position on Canvas");
		selectorSizeCell.setToolTipText("Selector Size (width x height)");
		rgbCell.getPixelCell().setToolTipText("Pixel Color/Position");
		rgbCell.getRedCell().setToolTipText("Pixel's Red Intensity");
		rgbCell.getGreenCell().setToolTipText("Pixel's Green Intensity");
		rgbCell.getBlueCell().setToolTipText("Pixel's Blue Intensity");
		fpsCell.setToolTipText("Frames Per Second");
		
		outputFormatCell.setText(getImageOutputFormat().toString());
		angleCell.setText("0°");
		
		final JCheckBox posCheckbox = selectorPositionCell.getCheckbox();
		final JCheckBox sizeCheckbox = selectorSizeCell.getCheckbox();
		final JCheckBox fpsCheckbox = fpsCell.getCheckbox();
		// TODO: initialize checkboxes and related features with configurable 
		// settings
		posCheckbox.setSelected(false);
		sizeCheckbox.setSelected(false);
		fpsCheckbox.setSelected(false);

		posCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new SelectorVisibilityEvent(Command.ShowPosition, posCheckbox.isSelected()));
			}
		});
		
		sizeCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getBus().post(new SelectorVisibilityEvent(Command.ShowSize, sizeCheckbox.isSelected()));			}
		});
		
		fpsCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				camPanel.setShowFpsStats(fpsCheckbox.isSelected());
			}
		});
		
		return statusBar;
	}
	
	/**
	 * Scale view combo box element. 
	 * 
	 * @author Adam Zimowski (mrazjava)
	 */
	class ScaleItem {
		private int value;
		public ScaleItem(int value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return value + " %";
		}
	}
	
	private JComboBox buildScaleCombo() {
		
        JComboBox combo = new JComboBox();
        
        combo.setToolTipText("Scale Image");
        combo.addItem(new ScaleItem(100));
        combo.addItem(new ScaleItem(90));
        combo.addItem(new ScaleItem(80));
        combo.addItem(new ScaleItem(70));
        combo.addItem(new ScaleItem(60));
        combo.addItem(new ScaleItem(50));
        combo.addItem(new ScaleItem(40));
        combo.addItem(new ScaleItem(30));
        combo.addItem(new ScaleItem(20));
        combo.addItem(new ScaleItem(10));
        
        combo.setActionCommand("SCALE");
        combo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					ScaleItem scale = (ScaleItem)e.getItem();
					getBus().post(new ScaleEvent(scale.value));
				}
			}
		});
        
        return combo;
	}
	
	private JComboBox buildColorCombo() {
		
		JComboBox combo = new JComboBox();
		
		combo.setToolTipText("Round corner background color");
		combo.setPreferredSize(ComboBoxColorCellRenderer.PREFERRED_SIZE);
		
		combo.addItem(Color.BLUE);
		combo.addItem(Color.CYAN);
		combo.addItem(Color.DARK_GRAY);
		combo.addItem(Color.GRAY);
		combo.addItem(Color.GREEN);
		combo.addItem(SliderDecor.Green.getColor());
		combo.addItem(Color.LIGHT_GRAY);
		combo.addItem(Color.MAGENTA);
		combo.addItem(Color.ORANGE);
		combo.addItem(Color.PINK);
		combo.addItem(Color.RED);
		combo.addItem(Color.WHITE);
		combo.addItem(Color.YELLOW);
		combo.addItem(Color.BLACK);
		
		combo.setRenderer(new ComboBoxColorCellRenderer());
		combo.setSelectedItem(Color.WHITE);
		combo.setVisible(getSuggestedColorComboBoxVisibility());
		
		combo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					Color color = (Color)e.getItem();
					getBus().post(new SelectorBackgroundEvent(getCurrentSelector(), color));
				}
			}
		});
		
		return combo;
	}
	
	JCheckBox showSelectorCheckbox;
	
	/**
	 * Sets up edit panel controls related to selector operation. Depending on 
	 * selector visibility these controls may be enabled, disabled, visible or 
	 * hidden.
	 * 
	 * @param visible true if selector is visible; false if hidden
	 */
	private void setSelectorVisbilityState(boolean visible) {

		getBus().post(new SelectorVisibilityEvent(Command.ShowSelector, visible));
		setSelectorControls(visible);
	}
	
	private void setSelectorControls(boolean visible) {
		pic1Radio.setEnabled(visible);
		pic2Radio.setEnabled(visible);
		pic3Radio.setEnabled(visible);
		pic4Radio.setEnabled(visible);
		statusBar.getSelectorPositionCell().setVisible(visible);
		statusBar.getSelectorSizeCell().setVisible(visible);
		imageExportButton.setEnabled(visible && EditorView.Picture.equals(currentView));
	}
	
	private JPanel buildPictureTypeRadioPanel() {
		
		final JLabel selectorLabel = new JLabel(config.getRadioOutputTypeLabel());
		selectorLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				showSelectorCheckbox.doClick();
			}
		});
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        ButtonGroup radioGroup = new ButtonGroup();
        
        showSelectorCheckbox = new JCheckBox();
        showSelectorCheckbox.setToolTipText("Show/Hide Selector");
        showSelectorCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean isSelected = showSelectorCheckbox.isSelected();
				setSelectorVisbilityState(isSelected);
			}
		});
        showSelectorCheckbox.setSelected(config.isSelectorVisible());
        
        radioPanel.add(showSelectorCheckbox);
        radioPanel.add(selectorLabel);
        radioPanel.add(Box.createHorizontalStrut(5));
        
        ActionListener listener = new SelectorChangeListener();
        
        ImageOutputSettings io1 = config.getPicSettings(1); // never null
        pic1Radio = new JRadioButton(io1.getRadioLabel());
        pic1Radio.setActionCommand("1");
        pic1Radio.addActionListener(listener);
        radioGroup.add(pic1Radio);
        radioPanel.add(pic1Radio);
        pic1Radio.setSelected(true);
        pic1Radio.setEnabled((config.getNumberOfPics() > 1));
        
        pic2Radio = new JRadioButton();
        ImageOutputSettings io2 = config.getPicSettings(2);
        if(io2 != null) {
        	pic2Radio.setActionCommand("2");
        	String label = io2.getRadioLabel();
	        pic2Radio.setText(label);
	        pic2Radio.addActionListener(listener);
	        radioGroup.add(pic2Radio);
	        radioPanel.add(pic2Radio);
        }

        pic3Radio = new JRadioButton();
        ImageOutputSettings io3 = config.getPicSettings(3);
        if(io3 != null) {
        	pic3Radio.setActionCommand("3");
        	String label = io3.getRadioLabel();
	        pic3Radio.setText(label);
	        pic3Radio.addActionListener(listener);
	        radioGroup.add(pic3Radio);
	        radioPanel.add(pic3Radio);
        }
        
        pic4Radio = new JRadioButton();
        ImageOutputSettings io4 = config.getPicSettings(4);
        if(io4 != null) {
        	pic4Radio.setActionCommand("4");
        	String label = io4.getRadioLabel();
	        pic4Radio.setText(label);
	        pic4Radio.addActionListener(listener);
	        radioGroup.add(pic4Radio);
	        radioPanel.add(pic4Radio);
        }
        
        radioPanel.setVisible(true);
        
        return radioPanel;
	}
	
    public void itemStateChanged(ItemEvent e) {
    	boolean isSelected = (e.getStateChange() == ItemEvent.SELECTED);
    	JToggleButton btn = (JToggleButton)e.getSource();
    	String cmd = btn.getActionCommand();
    	if("RULER".equals(cmd)) {
    		imgPanel.setRulerVisible(isSelected);
    		btn.setToolTipText("Ruler " + (isSelected ? "ON" : "OFF"));
    	}
    	else {
    		log.warn("invalid action command {}", cmd);
    	}
    }
    
    private int getCurrentSelector() {
    	
    	if(pic1Radio.isSelected()) return 1;
    	if(pic2Radio.isSelected()) return 2;
    	if(pic3Radio.isSelected()) return 3;
    	if(pic4Radio.isSelected()) return 4;
    	
    	log.warn("could not determine selector state");
    	
    	return -1;
    }
    
    /**
     * Handles radio button click for image output type selection. This 
     * implementation redraws selector in its starting position with a default 
     * starting size.
     * 
     * @author Adam Zimowski
     */
    class SelectorChangeListener implements ActionListener {
    	
    	@Override
    	public void actionPerformed(ActionEvent event) {
    		
    		int selector = getCurrentSelector();
    		log.debug("selector: {}", selector);
    		
    		getBus().post(new SelectorTypeEvent(selector));
			jpgBgColorCombo.setVisible(Editor.this.getSuggestedColorComboBoxVisibility());
			
			TextCell cell = statusBar.getOutputFormatCell();
			cell.setText(getImageOutputFormat().toString());
			cell.repaint();
    	}
    }
    
    /**
     * Additional GUI handling for certain upload outcomes.
     * 
     * @author Adam Zimowski (mrazjava)
     */
    class EditorUploadStateMonitor implements ExportStateMonitor {
		
		@Override
		public void exportSuccess(long bytesReceived) {
		}

		@Override
		public void exportError(final Exception e) {
			if (!ImageExportState.Aborting.equals(uploadState)) {
				// legit error
				log.error(e.getMessage());
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(Editor.this, e.getMessage(),
								"Upload Error", JOptionPane.ERROR_MESSAGE);
					}
				});
			}
		}

		@Override
		public void exportAborted(long bytesWritten) {
		}

		@Override
		public void exportFinished(Date when) {
			setUploadState(ImageExportState.Idle);
		}

		@Override
		public void exportStarted(Date when) {
		}
    }
    
    /**
     * Extracts selected image and uploads it to a remote location. The upload 
     * process is done off a EDT so gui is not blocked.
     * 
     * @author Adam Zimowski
     */
    class ExportButtonListener implements ActionListener {
    	
    	/**
    	 * Handles Upload/Abort action which is assigned to the same button. 
    	 * The button changes appearance and function depending on the context 
    	 * of the transfer. If there is no upload, the button presents an 
    	 * option to begin the transfer (upload). If upload is in progress the 
    	 * button presents an option to abort the transfer. When transfer is 
    	 * completed (successfully or not) the button revers to presenting an 
    	 * option to begin the transfer.
    	 */
		@Override
		public void actionPerformed(ActionEvent event) {
			if(ImageExportState.InProgress.equals(uploadState)) {
				uploadState = ImageExportState.Aborting;
				abort = true;
			}
			else {
				BufferedImage clippedImage = getClippedImage();
				if(clippedImage != null) {
					String loginId = null;
					String password = null;
					if(config.isAuthenticationRequired()) {
						if(loginDialog == null) {
							loginDialog = buildLoginDialog();
						}
						loginDialog.setVisible(true);
						loginId = loginDialogAdapter.getLoginId();
						password = loginDialogAdapter.getPassword();
					}
					if(!loginDialogAdapter.isCancelled()) {
						exportImage(loginId, password, clippedImage);
					}
				}
			}
		}
		
		/**
		 * Initiate the export process. Prepare all the necessary data along 
		 * with listeners and let the plugin roll.
		 * 
		 * @param loginId authentication login if required; null otherwise
		 * @param password authentication password if required; null otherwise
		 * @param image the image - already clipped - to be exported; may be 
		 * 	null if image could not be obtained
		 */
		void exportImage(String loginId, String password, BufferedImage image) {
			
			if(image == null) {
				abort = true;
				return;
			}
			
			abort = false;
			BufferedImage scaledImage = null;
			
			if(getTargetShape() != Configuration.TARGET_SHAPE_FULL && isRatioPreserved()) {
				// we need to do final rescale to match exact target dimensions
				int scaleWidth = getTargetWidth();
				int scaleHeight = getTargetHeight();
				ScaleFilter filter = new ScaleFilter(scaleWidth, scaleHeight);
				scaledImage = filter.filter(image, null);
			}
			else {
				// no need to rescale since full view image is already scaled
				scaledImage = image;
			}

			final ImageTaskCell cell = statusBar.getUploadCell();
			final JProgressBar progressBar = cell.getProgressBar();
			
			String encLoginId = null;	// encrypted if necessary
			String encPassword = null;	// encrypted if necessary
			
			if(config.isAuthenticationRequired()) {
				TextEncrypter loginIdEncrypter = config.getLoginIdEncrypter();
				loginIdEncrypter.initialize(config.getLoginIdEncrypterConfig());
				encLoginId = loginIdEncrypter.encrypt(loginId);
				TextEncrypter passwordEncrypter = config.getPasswordEncrypter();
				passwordEncrypter.initialize(config.getPasswordEncrypterConfig());
				encPassword = passwordEncrypter.encrypt(password);
			}
			else {
				encLoginId = loginId;
				encPassword = password;
			}
			
			List<ExportStateMonitor> stateMonitors = new LinkedList<ExportStateMonitor>();
			stateMonitors.add(new EditorUploadStateMonitor());
			stateMonitors.add(statusBar.getUploadCell());
			
			try {
				ImageOutputFormat outputFormat = getImageOutputFormat();
				final byte[] scaledImageBytes = ImageUtil.bufferedImageToByteArray(scaledImage, outputFormat.toString());

				progressBar.setMaximum(scaledImageBytes.length);
				progressBar.setString(null);
				progressBar.setValue(0);

				ImageExportDef imgExportDef = new ImageExportDef();
				imgExportDef.setImageBytes(scaledImageBytes);
				imgExportDef.setFormat(outputFormat);
				imgExportDef.setUrl(config.getRemoteHost());
				imgExportDef.setLoginId(encLoginId);
				imgExportDef.setPassword(encPassword);
				imgExportDef.setAbortAgent(Editor.this);
				imgExportDef.setProgressMonitor(new EditorUploadProgressMonitor());
				imgExportDef.setStateMonitors(stateMonitors);
				
				ImageExporter imgExporter = config.getImageExporter();

				imgExporter.setSelectorId(getSelectorId());
				imgExporter.export(imgExportDef, Editor.this.getTopLevelAncestor());

				setUploadState(ImageExportState.InProgress);
			}
			catch(Exception e) {
				// should not happen but plugins, like kids, could mis-behave
				JOptionPane.showMessageDialog(
						Editor.this, 
						e.getMessage(), 
						"Unexpected Error",
						JOptionPane.ERROR_MESSAGE
				);
			}			
		}
    }

    private BufferedImage getClippedImage() {
		BufferedImage image = null;
		try {
			image = imgPanel.getSelectedImage();
		}
		catch(RasterFormatException rfe) {
			JOptionPane.showMessageDialog(Editor.this,
				    "Clipping area is out of bounds.",
				    "Clipping Error",
				    JOptionPane.ERROR_MESSAGE);
		}
		return image;
    }
    
    private void setUploadState(ImageExportState state) {
    	uploadState = state;
    	ImageExporter imageExporter = config.getImageExporter();
    	if(ImageExportState.Idle.equals(state)) {
    		imageExportButton.setToolTipText(imageExporter.getTooltip(Locale.ENGLISH));
    		imageExportButton.setIcon(ToolbarIcons.Export.getIcon());
    	}
    	else if(ImageExportState.InProgress.equals(state)) {
    		imageExportButton.setToolTipText(ToolbarIcons.Cancel.getDescription());
    		imageExportButton.setIcon(ToolbarIcons.Cancel.getIcon());
    	}
    	else
    		log.warn("Invalid upload state: {}", state);
    }

    /**
     * Informs a browser that a URL request should be processed. This could be 
     * http/https request to render a new page, or a javascript call. If this 
     * is a javascript call it must be prefixed with a javascript: URI as this 
     * method tests if the URL begins with such URI because instristics of 
     * such call are different than a typical URL. The second argument is only 
     * relevant in the context of standard URL request. If a javascript call 
     * the second argument is ignored. This method should only be called if in 
     * applet mode. If {@link ConfigurationImpl#getAppletContext} returns null this 
     * method has no effect.
     * 
     * @param aUrl
     * @param aPopup true if page should be rendered in a new window; false 
     * 	if it should be rendered in the existing window
     * @deprecated Use {@link #showUrl(String)}. This method was used 
     * 	when code was conceived as an applet in the initial version. Since 
     * 	then it has been converted to a full blown java app, and is meant for 
     *  deployment under Java Web Start if web integration is desired. The 
     *  codebase has grown too large to be efficiently handled by the applet 
     *  technology. As such, this method provides no use outside of the Applet 
     *  context.
     */
    void showUrlPageFromApplet(String aUrl, boolean aPopup) {
    	
    	final AppletContext ctx = config.getAppletContext();
    	
    	if(ctx == null) {
    		log.warn("wrong invocation; applet mode required");
    		return;
    	}
    	
    	if(StringUtils.isEmpty(aUrl)) {
    		log.warn("url param empty; aborting!");
    		return;
    	}
    	
    	boolean jsCall = aUrl.startsWith("javascript:");

        try  {
        	URL url = null;
        	if(!jsCall) {
        		url = new URL(aUrl); // standard http(s) request
        	}
        	else {
        		final URLStreamHandler streamHanlder = new URLStreamHandler() {
					@Override
					protected URLConnection openConnection(URL u) throws IOException {
						return null;
					}
				};
        		url = new URL(null, aUrl, streamHanlder);
        	}
        	String target = aPopup ? "_blank" : "_self";
        	ctx.showDocument(url, target);
        } 
        catch (MalformedURLException e)  {
        	log.error(e.getMessage());
        }        
    }
    
    /**
     * @author Adam Zimowski (mrazjava)
     */
    class HelpButtonListener implements ActionListener {
    	
    	@Override
    	public void actionPerformed(ActionEvent event) {
    		
			try {
				URL url = new URL(config.getHelpPageUrl());
				Desktop.getDesktop().browse(url.toURI());
			} catch (MalformedURLException e) {
				log.warn(e.getMessage());
			} catch (IOException e) {
				log.error(e.getMessage());
			} catch (URISyntaxException e) {
				log.error(e.getMessage());
			}
    	}
    }

    /**
     * @author Adam Zimowski (mrazjava)
     */
    class WebCamListener implements ActionListener {
    	
		@Override
		public void actionPerformed(ActionEvent e) {

			JToggleButton camButton = (JToggleButton)e.getSource();

			if(camButton.isSelected()) {
				setView(EditorView.WebCam);
				//openCamDisplay();
			}
			else {
				//camPanel.stopWebCamSession();
				setView(filmPane.getThumbCount() > 0 ? 
						EditorView.Picture : EditorView.Welcome);
				//setStatusbarImagePath(statusBar.getUploadCell());
			}
		}
    }
    
	/**
	 * Computes visibility for color combo box component based on the state 
	 * of other related controls.
	 * 
	 * @return true if combo box should be made visible; false if it should 
	 * 	be hidden.
	 */
    public boolean getSuggestedColorComboBoxVisibility() {
		
		ImageOutputFormat fmt = getImageOutputFormat();
		int shape = getTargetShape();
		
		boolean show = 
				ImageOutputFormat.jpg.equals(fmt) && 
				shape == Configuration.TARGET_SHAPE_ELIPSE;

		return show;
    }
    
	@Override
	public boolean isExportAborted() {
		return abort;
	}
	
	/**
	 * Helper to quickly get image ouptut settings.
	 * 
	 * @param picNo
	 * @return
	 */
	private ImageOutputConfigFacade getIo(int picNo) {
		return config.getPicSettings(picNo);
	}
	
	/**
	 * @return id of the selector currently in use
	 * @throws NullPointerException radio buttons may be null if called from 
	 * 	initialization routine
	 */
	private int getSelectorId() {
		
		if(pic1Radio.isSelected()) return 1;
		if(pic2Radio.isSelected()) return 2;
		if(pic3Radio.isSelected()) return 3;
		if(pic4Radio.isSelected()) return 4;
		
		return 1;
	}

	@Override
	public int getTargetWidth() {
		
		try { return getIo(getSelectorId()).getTargetWidth(); }
		catch(NullPointerException npe) {}

		return getIo(1).getTargetWidth();
	}
	
	@Override
	public boolean isRatioPreserved() {

		try { return getIo(getSelectorId()).isRatioPreserved(); }
		catch(NullPointerException npe) {}

		return getIo(1).isRatioPreserved();
	}

	@Override
	public int getTargetHeight() {

		try { return getIo(getSelectorId()).getTargetHeight(); }
		catch(NullPointerException npe) {}
		
		return getIo(1).getTargetHeight();
	}

	@Override
	public int getTargetShape() {

		try { return getIo(getSelectorId()).getTargetShape(); }
		catch(NullPointerException npe) {}
		
		return getIo(1).getTargetShape();
	}

	@Override
	public float getSelectorFactor() {

		try { return getIo(getSelectorId()).getSelectorFactor(); }
		catch(NullPointerException npe) {}
		
		return getIo(1).getSelectorFactor();
	}

	@Override
	public String getSubmitUrl() {

		try { return getIo(getSelectorId()).getSubmitUrl(); }
		catch(NullPointerException npe) {}
		
		return getIo(1).getSubmitUrl();
	}

	@Override
	public ImageOutputFormat getImageOutputFormat() {

		try { return getIo(getSelectorId()).getImageOutputFormat(); }
		catch(NullPointerException npe) {}
		
		return getIo(1).getImageOutputFormat();
	}

	@Override
	public void selectorMoved(int x, int y) {
		CheckboxCell cell = statusBar.getSelectorPositionCell();
		cell.setText(String.format("x: %d, y: %d", x, y));
		cell.repaint();
	}

	@Override
	public void selectorResized(int width, int height) {
		CheckboxCell cell = statusBar.getSelectorSizeCell();
		cell.setText(String.format("%d x %d", width, height));
		cell.repaint();
	}

	@Override
	public void selectorClosed() {
		showSelectorCheckbox.doClick();
	}
	
	private BufferedImage printImage;
	
	@Subscribe
	public void onModelLifecycle(ModelLifecycleEvent ev) {
		ModelPhase phase = ev.getPhase();
		printImage = ev.getImage();
		// this flag is needed to preventing sliders firing an event on reset
		resetRgbSliders = ev.isRgbReset();
		resetHsSliders = ev.isHsReset();
		resetCbSliders = ev.isCbReset();
		switch(phase) {
		case BeforeChange:
			setCursorBusy();
			break;
		case AfterChange:
			setCursorDefault();
			if(ev.isRgbReset()) resetRgbSliders();
			if(ev.isHsReset()) resetHsSliders();
			if(ev.isCbReset()) resetCbSliders();
			enableImageControls(printImage != null);
			break;
		case Reset:
			handleModelReset();
			break;
		default:
		}
		if(resetRgbSliders) resetRgbSliders = false;
		if(resetHsSliders) resetHsSliders = false;
		if(resetCbSliders) resetCbSliders = false;
	}
	
	/**
	 * @param enabled true to enable; false to disable
	 */
	void enableImageControls(boolean enabled) {
		for(AbstractButton btn : imageToolbarButtons) btn.setEnabled(enabled);
		rgbPanel.setVisible(enabled);
		hscbPanel.setVisible(enabled);
		topPanel.setVisible(enabled);
	}
	
	/**
	 * Thread safe delegate for {@link #setCursorBusy()} which takes EDT 
	 * state into consideration. In particular, it considers app initialization 
	 * state scenarios such as beginning with busy cursor on startup when EDT 
	 * may not have fully initialized all components, such as ancestor window, 
	 * which under unsafe invokation could result in NPE. This delegate 
	 * checks for null state of parent window, and assumes the null state of a 
	 * window be that specific initialization scenario.
	 *
	private void setCursorBusyEDTSafe() {
		Window parent = SwingUtilities.getWindowAncestor(Editor.this);
		if(parent == null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setCursorBusy();
				}
			});
		}
		else
			setCursorBusy();
	}*/
	
	private void setCursorBusy() {
		Window parent = SwingUtilities.getWindowAncestor(Editor.this);
		if(parent == null) {
			return;
		}
		Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		parent.setCursor(waitCursor);
	}
	
	private void setCursorDefault() {
		Window parent = SwingUtilities.getWindowAncestor(this);
		Cursor defaultCursor = Cursor.getDefaultCursor();
		parent.setCursor(defaultCursor);		
	}
	
	/**
	 * indicates if red, green & blue color adjust sliders are being reset
	 */
	private boolean resetRgbSliders = false;
	
	/**
	 * indicates if hue & saturation sliders are being reset
	 */
	private boolean resetHsSliders = false;
	
	/**
	 * indicates if contrast and brightness sliders are being reset
	 */
	private boolean resetCbSliders = false;
	
	private void resetRgbSliders() {
		redSlider.setValue(0);
		greenSlider.setValue(0);
		blueSlider.setValue(0);
	}
	
	private void resetHsSliders() {
		hueSlider.setValue(0);
		saturationSlider.setValue(0);
	}
	
	private void resetCbSliders() {
		brightnessSlider.setValue(0);
		contrastSlider.setValue(0);		
	}

	private void handleModelReset() {
		zoomCombo.setSelectedIndex(0);
		resetRgbSliders();
		resetHsSliders();
		resetCbSliders();
		modelRotated(0,0);
	}

	@Subscribe
	public void onModelRotated(ModelRotateEvent ev) {
		modelRotated(ev.getAngleOfRotation(), ev.getCumulativeAngle());
	}
	
	private void modelRotated(int angleOfRotation, int cumulativeAngle) {
		TextCell angleCell = statusBar.getAngleCell();
		Integer angle = cumulativeAngle;
		StringBuffer txt = new StringBuffer();
		txt.append(angle);
		txt.append("°");
		angleCell.setText(txt.toString());
		angleCell.repaint();
	}

	@Override
	public void camReady() {
		setCursorDefault();
	}
	
	@Override
	public void camFailed() {
		setCursorDefault();
	}

	@Override
	public void camScanComplete(List<VideoDevice> devices, ActionListener listener) {
		JComboBox combo = statusBar.getVideoDeviceCell().getDropdown();
		if(combo.getItemCount() == 0) {
			for(VideoDevice device : devices) combo.addItem(device);
			combo.addItemListener(camStatusBarListener);
		}
	}

	/**
	 * Begins the web cam session.
	 */
	private void openCamDisplay() {
		// this routine can be called anywhere so make sure button state is in sync
		if(!webcamButton.isSelected()) webcamButton.setSelected(true);
		//setCursorBusyEDTSafe();
		setCursorBusy();
		
		VideoDevice camDevice = null;
		JComboBox camDeviceCombo = statusBar.getVideoDeviceCell().getDropdown();
		if(camDeviceCombo.getItemCount() > 0) {
			camDevice = (VideoDevice)camDeviceCombo.getSelectedItem();
		}
		camPanel.startWebCamSession(camDevice);
	}

	/**
	 * @author Adam Zimowski (mrazjava)
	 */
	class ViewportMouseListenerImpl implements ViewportMouseListener {

		@Override
		public void mouseMoved(MouseEvent e, int pixelColor) {
			if(pixelColor != 0) updateStatusBar(e, pixelColor);
		}

		@Override
		public void mouseDragged(MouseEvent e, int pixelColor) {
			if(pixelColor != 0) updateStatusBar(e, pixelColor);
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
		}

		private void updateStatusBar(MouseEvent e, int pixelColor) {
			//log.trace("pixelColor: {}, img? {}", pixelColor, imagePixel);
			RgbCell cell = statusBar.getRgbCell();
			cell.setPixelColor(pixelColor);
			cell.getPixelCell().setText(e.getX() + "," + e.getY());
			cell.repaint();
		}
	}

	/**
	 * Used to display video frame stats on the status bar
	 */
	private static final DecimalFormat df = new DecimalFormat("#.#####");
	
	@Override
	public void fpsComputed(double fpsLive, double fpsAverage) {
		CheckboxCell cell = statusBar.getFpsCell();
		cell.setText(df.format(fpsAverage));
		cell.repaint();
	}
	
	/**
	 * Updates status progress bar cell as post reports transfer progress.
	 * 
	 * @author Adam Zimowski (mrazjava)
	 */
	class EditorUploadProgressMonitor implements ExportProgressMonitor {
		@Override
		public void bytesTransferred(final int bytes) {
        	ProgressBarCell cell = statusBar.getUploadCell();
        	JProgressBar bar = cell.getProgressBar();
        		float pct = ((bytes/(float)bar.getMaximum())*100);
        		int percent = Math.round(pct);
            	bar.setValue(bytes);
            	cell.setText((percent > 100 ? 100 : percent) + " %");
            	cell.repaint();
		}
	}
	
	@Subscribe
	public void onFilterEventQueue(ImageFilterQueueEvent ev) {
		List<ToolbarIcons> icons = ev.getIcons();
		filterAbortButton.setVisible(icons.size() > 1);
		if(!ev.isAborted()) filterAbortButton.setEnabled(true);
	}
	
	private EventBus eventBus = null;
	private EventBus getBus() {
		if(eventBus == null) eventBus = EventBusManager.getInstance().getBus();
		return eventBus;
	}

	@Override
	public void windowOpened(WindowEvent e) {
		if(currentView == null) setView(EditorView.Welcome);
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}