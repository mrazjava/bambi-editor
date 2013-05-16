package org.zimowski.bambi.controls.dialog.print;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.RequestingUserName;
import javax.print.attribute.standard.Sides;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.controls.dialog.print.PrintPreview.PrintOutputConfig;
import org.zimowski.bambi.controls.resources.dialog.print.PrintDialogIcon;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class PrintDialog extends JDialog {
	
	public static enum Orientation { Portrait, Landscape }

	private static final long serialVersionUID = 2459464590245586831L;
	
	private static final Logger log = LoggerFactory.getLogger(PrintDialog.class);

	private JPanel topPanel;
	
	private JPanel bottomPanel;
	
	private Icon cropButtonIcon;

	private Icon printButtonIcon;

	public PrintDialog() {
		super();
	}
	
	public PrintDialog(String title) {
		super();
		setTitle(title);
	}

	public PrintDialog(Window owner) {
		super(owner);
	}

	public PrintDialog(Window owner, ModalityType modalityType) {
		super(owner, modalityType);
	}

	public PrintDialog(Window owner, String title) {
		super(owner, title);
	}

	public PrintDialog(Window owner, String title, ModalityType modalityType) {
		super(owner, title, modalityType);
	}

	public PrintDialog(Frame owner, boolean modal) {
		super(owner, modal);
	}

	public PrintDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
	}

	public PrintDialog(Frame owner, String title) {
		super(owner, title);
	}

	public PrintDialog(Frame owner) {
		super(owner);
	}
	
	/**
	 * Sets custom icon for the crop button. Highly recommending 16x16 
	 * resolution.
	 * 
	 * @param cropButton
	 */
	public void setCropButton(JButton cropButton) {
		this.cropButton = cropButton;
	}

	/**
	 * Sets custom icon for the print button. Highly recommending 16x16 
	 * resolution.
	 * 
	 * @param printButtonIcon
	 */
	public void setPrintButtonIcon(ImageIcon printButtonIcon) {
		this.printButtonIcon = printButtonIcon;
	}

	@Override
	protected void dialogInit() {
		super.dialogInit();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		topPanel = new JPanel();
		topPanel.setBorder(BorderFactory.createCompoundBorder(
				new EmptyBorder(5,5,5,5), 
				BorderFactory.createCompoundBorder(
						new EtchedBorder(), 
						new EmptyBorder(10,20,10,20)
				)
			)
		);
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(topPanel, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		getContentPane().add(mainPanel);
		initGui();
	}
	
	private JComboBox printerCombo;
	
	private JPanel buildPrinterList() {
		JPanel printerListPanel = new JPanel();
		printerListPanel.setLayout(new BoxLayout(printerListPanel, BoxLayout.X_AXIS));
		printerCombo = new JComboBox();
		DocFlavor myFormat = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
		PrintService[] services =PrintServiceLookup.lookupPrintServices(myFormat, aset);
		for(PrintService service : services) printerCombo.addItem(new Printer(service));
		printerCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					Printer printer = (Printer)e.getItem();
					preview.setSelectedPrinter(printer.getService());
					initializeMediaCombos();
					reInitializePreview();
				}
			}
		});
		printerListPanel.add(new JLabel("Printer:"));
		printerListPanel.add(Box.createHorizontalStrut(15));
		printerListPanel.add(printerCombo);
		return printerListPanel;
	}
	
	private void reInitializePreview() {
		preview.clearCrop();
		centerHorizontalCheckbox.setSelected(false);
		centerVerticalCheckbox.setSelected(false);
		fullPageCheckbox.setSelected(false);
		cropButton.setEnabled(false);
		try {
			preview.initialize();
			preview.setRenderScale(1d);
			printSizeSlider.setValue(100);
		}
		catch(IllegalStateException e) { return; }
		preview.setShowCroppingTool(fullPageCheckbox.isEnabled() && fullPageCheckbox.isSelected());
		preview.repaint();
	}
	
	private JRadioButton portraitRadio;
	private JRadioButton landscapeRadio;
	private JSlider printSizeSlider; 
	
	private JPanel buildRadioPanel() {

		JPanel radioPanel = new JPanel(new BorderLayout());
		
		JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		//JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		ButtonGroup orientGroup = new ButtonGroup();
		portraitRadio = new JRadioButton("Portrait");
		landscapeRadio = new JRadioButton("Landscape");
		landscapeRadio.setSelected(true);
		orientGroup.add(portraitRadio);
		orientGroup.add(landscapeRadio);
		westPanel.add(portraitRadio);
		westPanel.add(landscapeRadio);
		
		portraitRadio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preview.setOrientation(Orientation.Portrait);
				reInitializePreview();
			}
		});
		
		landscapeRadio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preview.setOrientation(Orientation.Landscape);
				reInitializePreview();
			}
		});

		printSizeSlider = new JSlider();
		printSizeSlider.setToolTipText("Adjust Print Size");
		printSizeSlider.setMaximum(100);
		printSizeSlider.setMinimum(10);
		printSizeSlider.setMajorTickSpacing(10);
		printSizeSlider.setMinorTickSpacing(1);
		printSizeSlider.setValue(100);
		printSizeSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				log.trace("slider: {}", printSizeSlider.getValue());
				preview.clearCrop();
				centerHorizontalCheckbox.setSelected(false);
				centerVerticalCheckbox.setSelected(false);
				fullPageCheckbox.setSelected(false);
				cropButton.setSelected(false);
				preview.setRenderScale(new Double(printSizeSlider.getValue()/100d));
				preview.repaint();
				previewInfo.repaint();
			}
		});
		printSizeSlider.addMouseListener(new MouseListener() {			
			@Override
			public void mouseReleased(MouseEvent e) {
				preview.setScaleAdjusting(false);
			}			
			@Override
			public void mousePressed(MouseEvent e) {
				preview.setScaleAdjusting(true);
			}			
			@Override
			public void mouseExited(MouseEvent e) {
			}			
			@Override
			public void mouseEntered(MouseEvent e) {
			}			
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		mediaFormatCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					MediaSizeName mediaFormat = (MediaSizeName)e.getItem();
					preview.setMediaFormat(mediaFormat);
					previewInfo.mediaSize = MediaSize.getMediaSizeForName(mediaFormat);
					reInitializePreview();
				}
			}
		});

		radioPanel.add(printSizeSlider, BorderLayout.CENTER);
		radioPanel.add(westPanel, BorderLayout.EAST);

		return radioPanel;
	}
	
	/**
	 * Defines initial orientation settings.
	 * 
	 * @param orientation
	 */
	public void setOrientation(Orientation orientation) {
		portraitRadio.setSelected(Orientation.Portrait.equals(orientation));
		landscapeRadio.setSelected(Orientation.Landscape.equals(orientation));
		preview.setOrientation(orientation);
	}
	
	public void setFormat(MediaSizeName format) {
		preview.setMediaFormat(format);
	}
	
	private JComboBox mediaFormatCombo;
	private JComboBox mediaTrayCombo;
	
	/**
	 * Populates supported media (paper, envelope, etc) formats for a given 
	 * printer. Assumes that {@link #mediaFormatCombo} has been instantiated.
	 */
	private void initializeMediaCombos() {
		mediaFormatCombo.removeAllItems();
		mediaTrayCombo.removeAllItems();
		Printer printer = (Printer)printerCombo.getSelectedItem();
		PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
		attr.add(MediaTray.MAIN);
		Media[] media = (Media[])printer.service.getSupportedAttributeValues(Media.class, DocFlavor.SERVICE_FORMATTED.PRINTABLE, attr);
		Set<String> mediaSet = new HashSet<String>();
		for (int x = 0; x < media.length; ++x) {
			if(media[x] instanceof MediaSizeName) {
				final String mediaName = media[x].toString();
				if(mediaSet.contains(mediaName)) continue;
				if("CUSTOM".equalsIgnoreCase(mediaName)) continue;
				mediaSet.add(mediaName);
				if(!isMediaSizeSane((MediaSizeName)media[x])) continue;
				mediaFormatCombo.addItem(media[x]);
				if(MediaSizeName.NA_LETTER.equals(media[x])) {
					mediaFormatCombo.setSelectedItem(media[x]);
				}
			}
			else {
				mediaTrayCombo.addItem(media[x]);
				//log.debug(media[x].getClass().getName());
			}
		}
		mediaTrayCombo.setVisible(mediaTrayCombo.getItemCount() > 0);
	}
	
	/**
	 * Performs sanity checks on parameters reported by the driver for a 
	 * particular media, such that they can be interpreted and worked with. 
	 * Sometimes driver will produce parameters which absolutely make no 
	 * sense, such as reporting printable area greater than the actual 
	 * media area. This happens with japanes media formats, one such example 
	 * jis-b7 under Linux for Canon i850. Note, this does not check the 
	 * actual exact validity of parameters to match the manufactures actual 
	 * specs, we merely check to make sure that these parameters make sense.
	 * 
	 * @return true if media parameters are sane, false if not
	 */
	private boolean isMediaSizeSane(MediaSizeName media) {
		MediaPrintableArea mediaArea = preview.getMediaPrintableArea(media);
		MediaSize mediaSize = MediaSize.getMediaSizeForName(media);
		if(mediaArea == null || mediaSize == null) return false;
		double mediaAreaWidth = mediaArea.getWidth(MediaPrintableArea.INCH) - mediaArea.getX(MediaPrintableArea.INCH);
		double mediaAreaHeight = mediaArea.getHeight(MediaPrintableArea.INCH) - mediaArea.getY(MediaPrintableArea.INCH);
		double mediaSizeWidth = mediaSize.getX(MediaSize.INCH);
		double mediaSizeHeight = mediaSize.getY(MediaSize.INCH);
		return mediaSizeWidth >= mediaAreaWidth && mediaSizeHeight >= mediaAreaHeight;
	}
	
	private JPanel previewPanel;
	private PrintPreview preview;
	private PreviewInfo previewInfo;
	private JCheckBox fullPageCheckbox;
	private JCheckBox centerHorizontalCheckbox;
	private JCheckBox centerVerticalCheckbox;
	private JButton cropButton;
	
	private void initGui() {
		topPanel.add(Box.createVerticalStrut(5));
		topPanel.add(buildPrinterList());
		topPanel.add(Box.createVerticalStrut(25));
		previewInfo = new PreviewInfo();
		preview = new PrintPreview(previewInfo);
		preview.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
			}
			@Override
			public void componentResized(ComponentEvent e) {
				reInitializePreview();
			}
			@Override
			public void componentMoved(ComponentEvent e) {
			}			
			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setPreferredSize(new Dimension(275,0));
		tabbedPane.setBorder(new EmptyBorder(0, 5, 0, 0));

		JPanel previewInfoPanel = new JPanel();
		JPanel controlLayoutPanel = new JPanel(new BorderLayout());
		previewInfoPanel.setLayout(new BoxLayout(previewInfoPanel, BoxLayout.Y_AXIS));
		previewInfoPanel.setBorder(new EmptyBorder(5, 5, 5, 0));
		
		JPanel controlPanel = new JPanel(new GridLayout(10,1));
		controlPanel.setBorder(new EmptyBorder(0, 5, 0, 5));

		tabbedPane.addTab("Info", null, previewInfoPanel);
		tabbedPane.addTab("Controls", null, controlLayoutPanel);
		
		final JCheckBox colorMarginCheckbox = new JCheckBox("Show hardware margins");
		colorMarginCheckbox.setToolTipText("Margins built into this printer");
		colorMarginCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//log.debug("colorMarginCheckbox.isSelected() {}", colorMarginCheckbox.isSelected());
				preview.setDrawMargins(colorMarginCheckbox.isSelected());
				preview.repaint();
			}
		});
		colorMarginCheckbox.doClick();
		
		centerHorizontalCheckbox = new JCheckBox("Center Horizontally");
		centerHorizontalCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preview.centerHorizontal(centerHorizontalCheckbox.isSelected());
				preview.repaint();
			}
		});
		
		centerVerticalCheckbox = new JCheckBox("Center Vertically");
		centerVerticalCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preview.centerVertical(centerVerticalCheckbox.isSelected());
				preview.repaint();
			}
		});
		cropButton = new JButton("Crop");
		if(cropButtonIcon == null) {
			cropButtonIcon = PrintDialogIcon.Crop.getIcon();
		}
		cropButton.setIcon(cropButtonIcon);
		cropButton.setEnabled(false);
		cropButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preview.executeCrop(true);
				cropButton.setEnabled(false);
				preview.repaint();
			}
		});
		
		fullPageCheckbox = new JCheckBox("Full Page");
		fullPageCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cropButton.setEnabled(fullPageCheckbox.isSelected());
				if(!fullPageCheckbox.isSelected()) {
					preview.executeCrop(false);
				}
				centerHorizontalCheckbox.setSelected(false);
				centerVerticalCheckbox.setSelected(false);
				preview.setShowCroppingTool(fullPageCheckbox.isSelected());
				preview.repaint();
			}
		});
		
		final JCheckBox relativePrevewSizes = new JCheckBox("Relate Preview Size");
		relativePrevewSizes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preview.useSizeDiffRation = relativePrevewSizes.isSelected();
				reInitializePreview();
			}
		});
		relativePrevewSizes.setToolTipText("check if different preview media should show with different sizes relative one to another");
		
		previewPanel = new JPanel(new BorderLayout());
		previewInfoPanel.add(previewInfo);
		
		controlPanel.add(colorMarginCheckbox);
		controlPanel.add(centerHorizontalCheckbox);
		controlPanel.add(centerVerticalCheckbox);
		JPanel fullPagePanel = new JPanel();
		fullPagePanel.setLayout(new BoxLayout(fullPagePanel, BoxLayout.X_AXIS));
		fullPagePanel.add(fullPageCheckbox);
		fullPagePanel.add(Box.createHorizontalGlue());
		fullPagePanel.add(cropButton);
		controlPanel.add(fullPagePanel);
		controlPanel.add(relativePrevewSizes);

		mediaFormatCombo = new JComboBox();
		mediaFormatCombo.setPreferredSize(new Dimension(200, 20));
		mediaTrayCombo = new JComboBox();
		
		controlPanel.add(mediaFormatCombo);
		controlPanel.add(mediaTrayCombo);
		
		controlLayoutPanel.add(controlPanel, BorderLayout.NORTH);
		controlLayoutPanel.add(Box.createVerticalGlue(), BorderLayout.CENTER);

		previewInfo.setPreferredSize(new Dimension(175, 0));
		previewPanel.add(tabbedPane, BorderLayout.WEST);
		previewPanel.add(preview, BorderLayout.CENTER);
		previewPanel.add(buildRadioPanel(), BorderLayout.SOUTH);
		
		topPanel.add(previewPanel);
		JButton printButton = new JButton("Print");
		if(printButtonIcon == null) {
			printButtonIcon = PrintDialogIcon.Print.getIcon();
		}
		printButton.setIcon(printButtonIcon);
		printButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PrintOutputConfig config = preview.getOutputConfiguration();
				executePrint(config);
				PrintDialog.this.dispose();
			}
		});
		bottomPanel.add(printButton);
		
		if(printerCombo.getItemCount() > 0) {
			Printer p = (Printer)printerCombo.getItemAt(0);
			preview.setSelectedPrinter(p.getService());
			initializeMediaCombos();
		}
	}

	private void setPrintImage(BufferedImage printImage) {
		preview.setPreviewImage(printImage);
	}
	
	/**
	 * @author Adam Zimowski (mrazjava)
	 */
	class Printer {
		private PrintService service;
		Printer(PrintService service) {
			this.service = service;
		}
		PrintService getService() {
			return service;
		}
		@Override
		public String toString() {
			return service.getName();
		}
	}
	
	private void executePrint(PrintOutputConfig pc) {
		
		Printer printer = (Printer)printerCombo.getSelectedItem();
		PrintService ps = printer.service;
		log.info("printing to: {}", ps.getName());

		PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();
		printAttributes.add(Chromaticity.COLOR);
		printAttributes.add(PrintQuality.HIGH);
		printAttributes.add(new RequestingUserName(System.getProperty("user.name"), Locale.US));
		printAttributes.add(new Copies(1));
		printAttributes.add(pc.mediaFormat);
		printAttributes.add(Sides.ONE_SIDED);
		if(mediaTrayCombo.getItemCount() > 0) {
			MediaTray mediaTray = (MediaTray)mediaTrayCombo.getSelectedItem();
			log.info("media feed from: {}", mediaTray);
			printAttributes.add(mediaTray);
		}
		
		PrinterJob pj = PrinterJob.getPrinterJob();
		try {
			pj.setPrintService(ps);
		} catch (PrinterException e) {
			log.error("print error: {}", e.getMessage());
			return;
		}
		PageFormat pf = pj.defaultPage();
		
		Paper paper = new Paper();
		MediaSize ms = MediaSize.getMediaSizeForName(pc.mediaFormat);
		paper.setSize(ms.getX(MediaSize.INCH)*72d, ms.getY(MediaSize.INCH)*72d);
		paper.setImageableArea(
				pc.printerSpecs.x, 
				pc.printerSpecs.y, 
				pc.printerSpecs.width - pc.printerSpecs.x, 
				pc.printerSpecs.height - pc.printerSpecs.y);

		pf.setPaper(paper);
		int orientation = portraitRadio.isSelected() ? PageFormat.PORTRAIT : PageFormat.LANDSCAPE;
		log.info("orientation: {}", orientation);
		pf.setOrientation(orientation);
		
		Book book = new Book();
		book.append(new BambiPrint(pc), pf);
		pj.setPageable(book);
		//pj.setPrintable(new BambiPrint(pc), pf);
		
		try {
			pj.print(printAttributes);
		} catch (PrinterException e) {
			log.error("unexpected print error: {}", e.getMessage());
		}
	}
	
	/**
	 * Tester, used for development.
	 * 
	 * @param parent owner window of this dialog
	 */
	private void display() {
		try {
			//String path = "/home/zima/Pictures/SarkaInPoland/attachments1_2009_11_12/021.JPG";
			//String path = "/home/zima/Pictures/SarkaInPoland/attachments_2009_11_05/370.JPG";
			//String path = "/home/zima/Pictures/SarkaInPoland/attachments_2009_11_05/266.JPG";
			//String path = "/home/zima/hubble_eye.jpg";
			String path = "/home/zima/mercedes_benz_500k-t2.jpg";
			//String path = "/home/zima/me2.JPG";
			//String path = "/home/zima/dark.jpg";
			//String path = "/home/zima/julia.jpeg";
			//String path = "/home/zima/Pictures/iPhone/2010-08-23/IMG_0145.JPG";
			//String path = "/home/zima/cyc.jpg";
			//String path = "/home/zima/print.png";
			//String path = "/home/zima/camera.png";
			//String path = "/home/zima/x/2012-02-11/2OKBK.jpg";
			//String path = "/home/zima/raptor/home/xOther/raptor/053/SEX-2cTN1UT-zET-.jpg";
			BufferedImage printImage = ImageIO.read(new File(path));
			display(null, printImage);
		}
		catch(IOException e) {
			log.error(e.getMessage());
		}		
	}
	
	/**
	 * Displays print dialog. If parent is provided, dialog is centered 
	 * relative to the parent. This is a convenience method because dialog 
	 * display can be achieved manually (and customized) by calling various 
	 * setters and {@link #setVisible(boolean)} in the end.
	 * 
	 * @param parent owner window of this dialog; can be null in which case 
	 * 	dialog is centered relative to active screen
	 * @param printImage image to be printed
	 */
	public void display(Component parent, BufferedImage printImage) {
		boolean portrait = printImage.getHeight() > printImage.getWidth();
		setPrintImage(printImage);
		setOrientation(portrait ? Orientation.Portrait : Orientation.Landscape);
		setFormat(MediaSizeName.NA_LETTER);
		setMinimumSize(new Dimension(750, 480));
		setModal(true);
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				PrintDialog dialog = new PrintDialog("Print");
				dialog.display();
			}
		});
	}
}