package org.zimowski.bambi.editor.studio.cam;

import java.awt.event.ActionListener;
import java.util.List;

/**
 * @author Adam Zimowski (mrazjava)
 */
public interface CamInitializationObserver {

	/**
	 * Invoked when web cam device has been fully and successfully 
	 * initialized and has just began operating.
	 */
	public void camReady();
	
	/**
	 * Invoked when web cam device failed to initialize. This could be due to 
	 * several reasons, most common being no device is available on a host 
	 * system.
	 */
	public void camFailed();
	
	/**
	 * Fired when a scan of all available video capture devices has completed. 
	 * The list argument is null safe, it will be empty if no devices were 
	 * found. The second listener argument, is a swing component event listener 
	 * which wants to respond when a coponent (if any) initialized as a result 
	 * of this event, fires an action. For example, a combo box could be 
	 * initialized with a list of devices from this call. In such case, the 
	 * action listener would be one to add to that combo box.
	 * 
	 * @param devices list of devices found on the host system
	 * @param listener the listener object that wishes to be informed of gui 
	 * 	action invoked as a result of this event; null if none
	 */
	public void camScanComplete(List<VideoDevice> devices, ActionListener listener);
}
