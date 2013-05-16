package org.zimowski.bambi.editor.studio.cam;

import org.apache.commons.lang3.StringUtils;
import org.openimaj.video.capture.Device;

/**
 * Simple struct to represent video streaming device inside a dropdown.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class VideoDevice {

	private int index;

	private String name;
	
	
	/**
	 * @param index zero-based index in the device order list returned by the 
	 * 	host sytem
	 * @param device
	 * @throws IllegalArgumentException if id is negative or device is null
	 */
	public VideoDevice(int index, Device device) {
		if(index < 0)
			throw new IllegalArgumentException("negative id!");
		if(device == null) 
			throw new IllegalArgumentException("null device!");
		
		this.index = index;
		this.name = getDeviceInfo(device);
	}
	
	/**
	 * @param index zero-based index in the device order list returned by the 
	 * 	host sytem
	 * @param name display name for the device
	 * @throws IllegalArgumentException if id is negative
	 */
	public VideoDevice(int index, String name) {
		if(index < 0)
			throw new IllegalArgumentException("negative id!");

		this.index = index;
		this.name = name;
	}
	
	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	/**
	 * @param device
	 * @return single string friendly name identifief for a device
	 */
	private String getDeviceInfo(Device device) {
		StringBuilder sb = new StringBuilder();
		if(device != null) {
			sb.append(device.getNameStr());
			String identifier = device.getIdentifierStr();
			if(StringUtils.isNotBlank(identifier)) {
				sb.append(" @ " + identifier);
			}
		}
		else {
			sb.append("Cam not available");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return getName();
	}
}