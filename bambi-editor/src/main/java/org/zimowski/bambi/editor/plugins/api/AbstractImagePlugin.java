package org.zimowski.bambi.editor.plugins.api;


/**
 * Image plugin base with selector support.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public abstract class AbstractImagePlugin extends AbstractPlugin implements ImagePlugin {

	protected int selectorId;
	
	@Override
	public void setSelectorId(int selectorId) {
		this.selectorId = selectorId;
	}
}