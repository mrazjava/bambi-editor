package org.zimowski.bambi.editor.studio.eventbus.events;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class SelectorVisibilityEvent extends ImageEvent {

	public enum Command {
		ShowSelector, ShowPosition, ShowSize
	};
	
	private Command command;
	
	private boolean visibility;
	
	public SelectorVisibilityEvent(Command cmd, boolean visibility) {
		this.command = cmd;
		this.visibility = visibility;
	}

	public Command getCommand() {
		return command;
	}
	
	public boolean getVisibility() {
		return visibility;
	}
}