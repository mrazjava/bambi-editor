package org.zimowski.bambi.controls.dialog.login;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All purpose and very simple implementation of the login dialog handler. 
 * More complex handlers may either directly re-implement {@link LoginDialogListener} 
 * or derive from this base.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class LoginDialogAdapter implements LoginDialogListener {

	private static final Logger log = LoggerFactory.getLogger(LoginDialogAdapter.class);
	
	private boolean cancelled;
	
	private String loginId;
	
	private String password;
	
	private boolean remember;


	@Override
	public void okay(String loginId, String password, boolean remember) {
		log.debug("ok - {} | {}", loginId, password + " ? " + remember);
		cancelled = false;
		this.loginId = loginId;
		this.password = password;
		this.remember = remember;
	}

	@Override
	public void cancel() {
		cancelled = true;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public String getLoginId() {
		return loginId;
	}

	public String getPassword() {
		return password;
	}

	public boolean isRemember() {
		return remember;
	}
}