package org.zimowski.bambi.editor.filters;

/**
 * @author Adam Zimowski (mrazjava)
 */
public interface FilterListener {
	
	public void filterInitialize();

	public void filterStart(int totalPixels);
	
	public void filterProgress(int percentComplete);
	
	public void filterDone();
}
