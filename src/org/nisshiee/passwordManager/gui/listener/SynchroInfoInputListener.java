package org.nisshiee.passwordManager.gui.listener;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SynchroInfoInputListener implements KeyListener {
	JOptionPane pane;
	JTextField host;
	JTextField user;
	Component okButton;

	public SynchroInfoInputListener(JOptionPane pane, JTextField host,
			JTextField user) {
		this.pane = pane;
		this.host = host;
		this.user = user;
		int type = pane.getOptionType();
		if (type == JOptionPane.OK_CANCEL_OPTION) {
			this.okButton = ((JPanel) pane.getComponent(pane
					.getComponentCount() - 1)).getComponent(0);
		}
		setSubmittable();
	}

	private void setSubmittable() {
		if (this.okButton != null) {
			String h = this.host.getText();
			String u = this.user.getText();
			int hLength = h.length();
			int uLength = u.length();
			boolean enabled = true;
			if (hLength <= 0 || uLength <= 0) {
				enabled = false;
			}
			this.okButton.setEnabled(enabled);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		setSubmittable();
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

}
