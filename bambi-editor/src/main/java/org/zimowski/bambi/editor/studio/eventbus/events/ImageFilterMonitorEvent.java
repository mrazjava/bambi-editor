package org.zimowski.bambi.editor.studio.eventbus.events;

import org.zimowski.bambi.editor.filters.ImageFilterOps;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class ImageFilterMonitorEvent extends ImageFilterEvent {

	public static final int PHASE_UNDEFINED = -1;
	
	public static final int PHASE_START = 0;
	
	public static final int PHASE_PROGRESS = 1;
	
	public static final int PHASE_FINALIZE = 2;
	
	private int phase = ImageFilterMonitorEvent.PHASE_UNDEFINED;
	
	private int pctComplete;

	private String status;
	
	public boolean showFilterNameOnOutput = true;

	
	public ImageFilterMonitorEvent(ImageFilterOps filter, int phase) {
		super(filter);
		this.phase = phase;
	}
	
	public int getPctComplete() {
		return pctComplete;
	}

	public void setPctComplete(int pctComplete) {
		this.pctComplete = pctComplete;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getPhase() {
		return phase;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}
	
	public String getStatusFormatted() {
		ImageFilterOps filter = getFilter();
		if(filter != null && showFilterNameOnOutput) {
			StringBuffer desc = new StringBuffer(getFilter().toString());
			if(status != null) {
				desc.append(": ");
				desc.append(status);
			}
			return desc.toString();
		}
		else
			return status;
	}
}