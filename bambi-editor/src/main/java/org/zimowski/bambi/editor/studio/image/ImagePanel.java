package org.zimowski.bambi.editor.studio.image;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.config.ConfigManager;
import org.zimowski.bambi.editor.config.Configuration;
import org.zimowski.bambi.editor.studio.eventbus.EventBusManager;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelLifecycleEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelLifecycleEvent.ModelPhase;

import com.google.common.eventbus.Subscribe;

/**
 * Image container with scrolling and ruler support.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class ImagePanel extends JScrollPane {

	private static final long serialVersionUID = 3076945493645903684L;
	
	private static final Logger log = LoggerFactory.getLogger(ImagePanel.class);
	
	private static final String METRIC_TOGGLE_TIP = "Metric system is ";

	private ImageContainer imgContainer;
	
	private JPanel buttonCorner;
	
	private Ruler rulerColumnView;
    
	private Ruler rulerRowView;
	
	private JToggleButton isMetricButton;
	
	private Configuration config = 
			ConfigManager.getInstance().getConfiguration();
	
	
	public ImagePanel(EditorImageControllers controllers) {
		
		super();
		EventBusManager.getInstance().registerWithBus(this);
		imgContainer = new ImageContainer(controllers.getImageOutputConfigFacade());
		imgContainer.setMouseInputListener(controllers.getMouseInputListener());
		imgContainer.setSelectorObserver(controllers.getSelectorObserver());

		setViewportView(imgContainer);
		
        JScrollBar vScrollBar = getVerticalScrollBar();
        JScrollBar hScrollBar = getHorizontalScrollBar();
        
        vScrollBar.addAdjustmentListener(imgContainer);
        hScrollBar.addAdjustmentListener(imgContainer);
	}

	@Subscribe
	public void onModelInit(ModelLifecycleEvent ev) {
		if(ModelPhase.Initialized.equals(ev.getPhase())) initializeRuler();
	}
	
	public void setRulerVisible(boolean visible) {
		if(visible) {
	        setColumnHeaderView(rulerColumnView);
	        setRowHeaderView(rulerRowView);

	        setCorner(JScrollPane.UPPER_LEADING_CORNER, buttonCorner);
	        setCorner(JScrollPane.LOWER_LEADING_CORNER, new RulerCorner());
	        setCorner(JScrollPane.UPPER_TRAILING_CORNER, new RulerCorner());			
		}
		else {
	        setColumnHeaderView(null);
	        setRowHeaderView(null);

	        setCorner(JScrollPane.UPPER_LEADING_CORNER, null);
	        setCorner(JScrollPane.LOWER_LEADING_CORNER, null);
	        setCorner(JScrollPane.UPPER_TRAILING_CORNER, null);
		}
	}
	
	public BufferedImage getSelectedImage() {
		return imgContainer.getClippedImage();
	}
	
	private void initializeRuler() {
		
		if(imgContainer == null) {
			log.debug("null container; aborting");
			return;
		}
		
        //Create the corners.
        buttonCorner = new JPanel(); //use FlowLayout
        isMetricButton = new JToggleButton("in", false);
        isMetricButton.setToolTipText("Metric system is OFF");
        isMetricButton.setFont(new Font("SansSerif", Font.PLAIN, 10));
        isMetricButton.setMargin(new Insets(2,2,3,2));
        isMetricButton.setPreferredSize(new Dimension(25,25));
        isMetricButton.setActionCommand("RULUNIT");
        isMetricButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
		    	boolean isSelected = (e.getStateChange() == ItemEvent.SELECTED);
		    	isMetricButton.setText(isSelected ? "cm" : "in");
		    	isMetricButton.setToolTipText(METRIC_TOGGLE_TIP + (isSelected ? "ON" : "OFF"));
		    	changeRuleMetric(isSelected);
			}
		});
        buttonCorner.add(isMetricButton); 
		
        //Create the row and column headers.
        rulerColumnView = new Ruler(Ruler.ORIENT_HORIZONTAL, true);
        rulerRowView = new Ruler(Ruler.ORIENT_VERTICAL, true);
        
        imgContainer.setMaxUnitIncrement(rulerColumnView.getIncrement());
        
        BufferedImage image = imgContainer.getImage();
        int w, h, rulerLength;
        
        if (image != null) {
        	w = image.getWidth();
        	h = image.getHeight();
        	
        } else {
        	w = ImageContainer.DEFAULT_CANVAS_WIDTH;
        	h = ImageContainer.DEFAULT_CANVAS_HEIGHT;
        }

        rulerLength = w > h ? w : h;
    	rulerColumnView.setLength(rulerLength);
    	rulerRowView.setLength(rulerLength);

    	setRulerVisible(config.isRulerVisible());
        changeRuleMetric(isMetricButton.isSelected());   
        
        log.debug("ruler length: {}", rulerLength);
	}
	
    /**
     * @param aSelected true if metric; false if inches
     */
    public void changeRuleMetric(boolean aSelected) {
        rulerRowView.setIsMetric(aSelected);
        rulerColumnView.setIsMetric(aSelected);    	
    }
}