package org.zimowski.bambi.controls.dialog.login;

/**
 * Listener hook for clients wishing be notified of login outcome from the 
 * {@link LoginDialog}.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public interface LoginDialogListener {

	/**
	 * Invoked when user clicked okay.
	 * 
	 * @param loginId
	 * @param password
	 * @param remember
	 */
	public void okay(String loginId, String password, boolean remember);
	
	/**
	 * Invoked when user clicked cancel.
	 */
	public void cancel();
}