package org.zimowski.bambi.controls.dialog.login;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.controls.resources.dialog.login.LoginDialogIcon;

/**
 * @author Adam Zimowski (mrazjava)
 */
public class LoginDialog extends JDialog {

	private static final long serialVersionUID = 5804943075801676917L;
	
	private static final Logger log = LoggerFactory.getLogger(LoginDialog.class);
	
	private List<LoginDialogListener> loginDialogListeners = 
			new LinkedList<LoginDialogListener>();
	
	private String prompt;
	
	private String serverName;
	
	private static final String DEFAULT_PROMPT = "<html><h2>Web Login</h2></html>";
	
	private static final String DEFAULT_SHORT_MSG = 
			"<html><i>%s requires authentication</i></html>";


	public LoginDialog() {
		super();
	}

	public LoginDialog(Dialog owner, boolean modal) {
		super(owner, modal);
	}

	public LoginDialog(Dialog owner, String title, boolean modal,
			GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
	}

	public LoginDialog(Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
	}

	public LoginDialog(Dialog owner, String title) {
		super(owner, title);
	}

	public LoginDialog(Dialog owner) {
		super(owner);
	}

	public LoginDialog(Frame owner, boolean modal) {
		super(owner, modal);
	}

	public LoginDialog(Frame owner, String title, boolean modal,
			GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
	}

	public LoginDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
	}

	public LoginDialog(Frame owner, String title) {
		super(owner, title);
	}

	public LoginDialog(Frame owner) {
		super(owner);
	}

	public LoginDialog(Window owner, ModalityType modalityType) {
		super(owner, modalityType);
	}

	public LoginDialog(Window owner, String title, ModalityType modalityType,
			GraphicsConfiguration gc) {
		super(owner, title, modalityType, gc);
	}

	public LoginDialog(Window owner, String title, ModalityType modalityType) {
		super(owner, title, modalityType);
	}

	public LoginDialog(Window owner, String title) {
		super(owner, title);
	}

	public LoginDialog(Window owner) {
		super(owner);
	}
	
	public void initialize() {
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(350, 240));

		SpringLayout layout = new SpringLayout();
		JPanel contentPane = new JPanel(layout);
		contentPane.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5, 5, 5, 5), new EtchedBorder()));
		
		getContentPane().add(contentPane);

		String promptMsg = prompt == null ? DEFAULT_PROMPT : prompt;
		String destName = serverName == null ? "Server" : "<b>" + serverName + "</b>";
		JLabel promptLabel = new JLabel(promptMsg);
		promptLabel.setPreferredSize(new Dimension(200, 50));
		JLabel iconLabel = new JLabel(LoginDialogIcon.Password.getIcon());
		JLabel loginIdLabel = new JLabel("Login ID:");
		JLabel passwordLabel = new JLabel("Password:");
		
		final JTextField loginIdTxt = new JTextField(17);
		final JPasswordField passwordTxt = new JPasswordField(17);
		
		contentPane.add(promptLabel);
		contentPane.add(iconLabel);
		contentPane.add(loginIdLabel);
		contentPane.add(loginIdTxt);
		contentPane.add(passwordLabel);
		contentPane.add(passwordTxt);
		
		layout.putConstraint(SpringLayout.WEST, promptLabel, 25, SpringLayout.EAST, loginIdLabel);
		layout.putConstraint(SpringLayout.NORTH, promptLabel, 10, SpringLayout.NORTH, contentPane);
		
		layout.putConstraint(SpringLayout.WEST, loginIdLabel, 25, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, loginIdLabel, 70, SpringLayout.NORTH, contentPane);
		
		layout.putConstraint(SpringLayout.WEST, loginIdTxt, 25, SpringLayout.EAST, loginIdLabel);
		layout.putConstraint(SpringLayout.NORTH, loginIdTxt, 70, SpringLayout.NORTH, contentPane);
		
		layout.putConstraint(SpringLayout.WEST, passwordLabel, 25, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, passwordLabel, 30, SpringLayout.NORTH, loginIdLabel);
		
		layout.putConstraint(SpringLayout.WEST, passwordTxt, 25, SpringLayout.EAST, loginIdLabel);
		layout.putConstraint(SpringLayout.NORTH, passwordTxt, 30, SpringLayout.NORTH, loginIdTxt);
		
		layout.putConstraint(SpringLayout.WEST, iconLabel, 25, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, iconLabel, 15, SpringLayout.NORTH, contentPane);
		
		final JCheckBox remember = new JCheckBox("Remember next time");
		contentPane.add(remember);
		
		layout.putConstraint(SpringLayout.WEST, remember, 0, SpringLayout.WEST, passwordTxt);
		layout.putConstraint(SpringLayout.NORTH, remember, 30, SpringLayout.NORTH, passwordTxt);
		
		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(loginIdTxt.getText().trim().length() == 0) {
					JOptionPane.showMessageDialog(LoginDialog.this,
						    "Login ID can't be blank",
						    "Input Error",
						    JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if(passwordTxt.getPassword().length == 0) {
					JOptionPane.showMessageDialog(LoginDialog.this,
						    "Password can't be blank",
						    "Input Error",
						    JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(loginDialogListeners.size() > 0) {
					for(LoginDialogListener listener : loginDialogListeners) {
						listener.okay(
								loginIdTxt.getText(), 
								new String(passwordTxt.getPassword()), 
								remember.isSelected());
					}
				}
				
				dispose();
			}
		});
		
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(loginDialogListeners.size() > 0) {
					for(LoginDialogListener listener : loginDialogListeners) {
						listener.cancel();
					}
				}
				dispose();
			}
		});
		
		okButton.setPreferredSize(new Dimension(60, 27));
		cancelButton.setPreferredSize(new Dimension(90, 27));
		
		JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
		separator.setPreferredSize(new Dimension(315, 10));
		contentPane.add(separator);
		
		layout.putConstraint(SpringLayout.WEST, separator, 10, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, separator, 30, SpringLayout.SOUTH, remember);		
		
		JLabel shortMsgLbl = new JLabel(String.format(DEFAULT_SHORT_MSG, destName));
		
		contentPane.add(shortMsgLbl);
		
		layout.putConstraint(SpringLayout.WEST, shortMsgLbl, 0, SpringLayout.WEST, separator);
		layout.putConstraint(SpringLayout.SOUTH, shortMsgLbl, -3, SpringLayout.NORTH, separator);
		
		contentPane.add(okButton);
		contentPane.add(cancelButton);
		
		layout.putConstraint(SpringLayout.WEST, okButton, 0, SpringLayout.WEST, passwordTxt);
		layout.putConstraint(SpringLayout.NORTH, okButton, 3, SpringLayout.SOUTH, separator);

		layout.putConstraint(SpringLayout.EAST, cancelButton, 0, SpringLayout.EAST, passwordTxt);
		layout.putConstraint(SpringLayout.NORTH, cancelButton, 3, SpringLayout.SOUTH, separator);
	}

	public void addLoginDialogListener(LoginDialogListener loginDialogListener) {
		loginDialogListeners.add(loginDialogListener);
	}
	
	public void removeLoginDialogListener(LoginDialogListener loginDialogListener) {
		loginDialogListeners.remove(loginDialogListener);
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}
	
	public void setServerName(String serverName) {
		if(serverName == null) return;
		String name;
		if(serverName.length() > 20) {
			log.warn("server name too long; trimming");
			name = serverName.substring(0, 19);
		}
		else {
			name = serverName;
		}
		this.serverName = name;
	}

	public static void main(String[] args) {
		LoginDialog d = new LoginDialog();
		d.setTitle("Login Required");
		d.setUndecorated(true);
		d.initialize();
		d.setVisible(true);
		d.pack();
		d.setLocationRelativeTo(null);
		d.setModal(true);
	}
}