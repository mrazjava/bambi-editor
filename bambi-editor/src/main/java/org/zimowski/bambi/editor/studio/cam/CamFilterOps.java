package org.zimowski.bambi.editor.studio.cam;

/**
 * @author Adam Zimowski (mrazjava)
 */
public enum CamFilterOps {

	None("No Filters, Original Feed"), 
	Rgb("Adjust Red, Green & Blue"), 
	Canny, 
	Negative,
	Solarize,
	Grayscale,
	Mirror;
	
	private String description = null;
	
	private CamFilterOps() {
	}
	
	private CamFilterOps(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		if(description == null)
			return super.toString();
		else
			return description;
	}
}