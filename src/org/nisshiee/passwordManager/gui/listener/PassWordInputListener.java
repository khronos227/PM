package org.nisshiee.passwordManager.gui.listener;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PassWordInputListener implements KeyListener {
	JOptionPane pane;
	JTextField field;
	Component okButton;

	public PassWordInputListener(JOptionPane pane, JTextField field) {
		this.pane = pane;
		this.field = field;
		int type = pane.getOptionType();
		if (type == JOptionPane.OK_CANCEL_OPTION) {
			this.okButton = ((JPanel) pane.getComponent(pane
					.getComponentCount() - 1)).getComponent(0);
		}
		setSubmittable();
	}

	private void setSubmittable() {
		if (this.okButton != null) {
			String text = this.field.getText();
			int length = text.length();
			boolean enabled = true;
			if (length < 5 || length > 128) {
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
