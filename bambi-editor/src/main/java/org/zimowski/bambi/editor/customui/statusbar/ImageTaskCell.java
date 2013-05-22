package org.zimowski.bambi.editor.customui.statusbar;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.config.Configuration;
import org.zimowski.bambi.editor.plugins.api.ExportStateMonitor;
import org.zimowski.bambi.editor.studio.eventbus.EventBusManager;
import org.zimowski.bambi.editor.studio.eventbus.events.ImageFilterMonitorEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ImageFilterQueueEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelLifecycleEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelLifecycleEvent.ModelPhase;

import com.google.common.eventbus.Subscribe;

/**
 * A {@link JProgressBar} backed cell used to display realtime progress of 
 * a lengthy task such as upload to remote server or image filter operation. 
 * Also used to display location where original image was loaded from when no 
 * upload is being conducted (idle time). 
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class ImageTaskCell extends ProgressBarCell implements ExportStateMonitor {

	private static final long serialVersionUID = 4456415422381527492L;
	
	private static final Logger log = LoggerFactory.getLogger(ImageTaskCell.class);
	
	private BufferedImage uploadFailedIcon;
	
	private BufferedImage uploadOkIcon;
	
	private BufferedImage closeIcon;
	
	private ExportHistory lastExport;
	
	private String defaultText;
	
	private boolean showingHistory = false;
	
	private boolean exportInProgress = false;
	
	/**
	 * Used for the blinking effect
	 */
	private boolean showIcon = false;
	
	/**
	 * Simple struct keeping track of recent export operations.
	 * 
	 * @author Adam Zimowski (mrazjava)
	 */
	class ExportHistory {
		private DateFormat df = new SimpleDateFormat("HH:mm:ss");
		Date timeFinished;
		boolean error;
		int barValue;
		String barMessage;
		
		public ExportHistory(boolean e, int v, String m) {
			error = e;
			barValue = v;
			barMessage = m;
		}
		
		String getBarMessageForDisplay() {
			return df.format(timeFinished) + ": " + barMessage;
		}
	}
	
	/**
	 * @author Adam Zimowski (mrazjava)
	 */
	class HideUploadInfo implements Runnable {
		
		private boolean running = false;
		
		@Override
		public void run() {
			running = true;
			try {
				for(int x=0; x<8; x++) {
					Thread.sleep(500); // blink every half second
					showIcon = !showIcon;
					ImageTaskCell.this.repaint();
				}
			} catch(Exception e) {}
			setText(defaultText);
			progressBar.setValue(progressBar.getMinimum());
			showIcon = true;
			ImageTaskCell.this.repaint();
			running = false;
		}
		
		public boolean isRunning() {
			return running;
		}		
	}
	
	private HideUploadInfo redisplayDefaultTextTask;
	
	private Thread redisplayDefaultTextThread;
	
	enum ToolTipType {
		Regular, Trimmed, Icon
	};

	private MouseAdapter mouseHandler = new MouseAdapter() {
	
		private ToolTipType tipType = null;
		
		@Override
		public void mouseMoved(MouseEvent e) {
			if(!redisplayDefaultTextTask.isRunning()) {
				ImageTaskCell cell = ImageTaskCell.this;
				if(isOverIcon(e.getX(), e.getY())) {
					if(!ToolTipType.Icon.equals(tipType)) {
						StringBuffer tip = new StringBuffer(showingHistory ? "Close" : "Show");
						tip.append(" results of last operation");
						cell.setToolTipText(tip.toString(), false);
						cell.setCursor(new Cursor(Cursor.HAND_CURSOR));
						tipType = ToolTipType.Icon;
					}
				}
				else {
					if(isTextOnDisplayTrimmed()) {
						if(!ToolTipType.Trimmed.equals(tipType)) {
							cell.setToolTipText(getTooltipTextWhenTrimmed(), false);
							cell.setCursor(Cursor.getDefaultCursor());
							tipType = ToolTipType.Trimmed;
						}
					}
					else {
						if(!ToolTipType.Regular.equals(tipType)) {
							cell.setToolTipText(toolTipText, false);
							cell.setCursor(Cursor.getDefaultCursor());
							tipType = ToolTipType.Regular;
						}
					}
				}
			}
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if(!redisplayDefaultTextTask.isRunning()) {
				if(isOverIcon(e.getX(), e.getY())) {
					showingHistory = !showingHistory;
					handleLastUploadDisplay();
				}
			}			
		}

		private boolean isOverIcon(int x, int y) {
			boolean result = false;			
			BufferedImage icon = getUploadDisplayIcon();
			if(icon != null) {
				Point p = getDisplayIconPosition(icon);
				int w = icon.getWidth();
				int h = icon.getHeight();
				result = (x >= p.x && y >= p.y) && (x <= p.x+w && y <= p.y+h);
			}
			return result;
		}
	};

	
	public ImageTaskCell() {
		EventBusManager.getInstance().registerWithBus(this);
		redisplayDefaultTextTask = new HideUploadInfo();
		addMouseMotionListener(mouseHandler);
		addMouseListener(mouseHandler);
		try {
			uploadFailedIcon = ImageIO.read(getClass().getResource(Configuration.RESOURCE_PATH + "upload_red12x12.png"));
			uploadOkIcon = ImageIO.read(getClass().getResource(Configuration.RESOURCE_PATH + "upload_green12x12.png"));
			closeIcon = ImageIO.read(getClass().getResource(Configuration.RESOURCE_PATH + "close12x12.png"));
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void handleLastUploadDisplay() {
		if(lastExport != null) {
			if(showingHistory) {
				setText(lastExport.getBarMessageForDisplay());
				progressBar.setValue(lastExport.barValue);
			}
			else {
				setText(defaultText);
				progressBar.setValue(progressBar.getMinimum());
			}
			repaint();
		}
	}
	
	@Override
	protected int getTrimOffset() {
		int iconOffset = 0;
		Image displayIcon = getUploadDisplayIcon();
		if(isUploadIconDisplayRequired() && displayIcon != null) {
			iconOffset += displayIcon.getWidth(null) + 3;
		}
		return super.getTrimOffset() + iconOffset;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(isUploadIconDisplayRequired()) {
			BufferedImage icon = getUploadDisplayIcon();
			Point p = getDisplayIconPosition(icon);
			g.drawImage(icon, p.x, p.y, null);
		}
	}

	@Override
	public void exportStarted(Date when) {
		if(redisplayDefaultTextTask != null && redisplayDefaultTextTask.isRunning()) {
			log.debug("interrupting running thread..");
			redisplayDefaultTextThread.interrupt();
			redisplayDefaultTextThread = null;
		}
		exportInProgress = true;
		showingHistory = false;
	}

	@Override
	public void exportFinished(Date when) {
		if(lastExport != null) lastExport.timeFinished = when;
		exportInProgress = false;
		redisplayDefaultTextThread = new Thread(redisplayDefaultTextTask);
		redisplayDefaultTextThread.start();
	}

	@Override
	public void exportSuccess(long bytesReceived) {
		String msg = String.format("Transfer complete! %d bytes uploaded.", bytesReceived);
		setText(msg);
		repaint();
		lastExport = new ExportHistory(false, progressBar.getValue(), msg);
	}

	@Override
	public void exportError(Exception e) {
		String msg = String.format("Transfer failed! %s", e.getMessage());
		setText(msg);
		repaint();
		lastExport = new ExportHistory(true, progressBar.getValue(), msg);
	}

	@Override
	public void exportAborted(long bytesWritten) {
		String msg = "Transfer aborted at user's request.";
		setText(msg);
		repaint();
		lastExport = new ExportHistory(true, progressBar.getValue(), msg);
	}

	public void setDefaultText(String defaultText) {
		this.defaultText = defaultText;
	}
	
	private BufferedImage getUploadDisplayIcon() {
		if(lastExport == null) return null;
		if(!showingHistory)
			return lastExport.error ? uploadFailedIcon : uploadOkIcon;
		else
			return closeIcon;
	}
	
	private Point getDisplayIconPosition(BufferedImage icon) {
		Point leftUpperCorner = new Point();
		leftUpperCorner.x = getWidth() - icon.getWidth(null) - 5;
		leftUpperCorner.y = 4;
		return leftUpperCorner;
	}
	
	/**
	 * Determines if upload info icon is currently being displayed in the cell.  
	 * Does not take blinking into account.
	 * 
	 * @return true if icon is currently showing; false if it is hidden
	 */
	private boolean isUploadIconDisplayRequired() {
		return !exportInProgress && lastExport != null && showIcon;
	}

	@Override
	public String getTooltipTextWhenTrimmed() {
		if(StringUtils.isNotEmpty(text)) return text;
		if(StringUtils.isNotEmpty(defaultText)) return defaultText;
		return null;
	}

	@Subscribe
	public void onFilter(ImageFilterMonitorEvent ev) {
		int phase = ev.getPhase();
		JProgressBar bar = getProgressBar();
		log.trace("on EDT ? {}", SwingUtilities.isEventDispatchThread());
		if(phase == ImageFilterMonitorEvent.PHASE_PROGRESS) {
			bar.setValue(ev.getPctComplete());
		}
		else if(phase == ImageFilterMonitorEvent.PHASE_START) {
			if(redisplayDefaultTextTask != null && redisplayDefaultTextTask.isRunning()) {
				redisplayDefaultTextThread.interrupt();
			}
			bar.setMaximum(100);
			bar.setString(ev.getStatusFormatted());
			bar.setStringPainted(true);
			setText(null);
		}
		else if(phase == ImageFilterMonitorEvent.PHASE_FINALIZE) {
			bar.setValue(ev.getPctComplete());
			bar.setString(ev.getStatusFormatted());
		}
		bar.repaint();
	}

	@Subscribe
	public void onFilterEventQueue(ImageFilterQueueEvent ev) {
		if(ev.getIcons() == null || ev.getIcons().size() == 0) {
			JProgressBar bar = getProgressBar();
			bar.setString(null);
			bar.setStringPainted(false);
			setText(defaultText);
			repaint();
		}
	}
	
	@Subscribe
	public void onModelChanged(ModelLifecycleEvent ev) {
		if(ModelPhase.AfterChange.equals(ev.getPhase()) || 
			ModelPhase.Initialized.equals(ev.getPhase())
			) {
			JProgressBar bar = getProgressBar();
			bar.setValue(0);
			bar.repaint();
		}
	}
}