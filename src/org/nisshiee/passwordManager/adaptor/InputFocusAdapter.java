package org.nisshiee.passwordManager.adaptor;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;

public class InputFocusAdapter<E extends JComponent> extends WindowAdapter {
	E element;

	public InputFocusAdapter(E element) {
		this.element = element;
	}

	@Override
	public void windowOpened(WindowEvent e) {
		super.windowOpened(e);
		this.element.setFocusable(true);
		this.element.requestFocusInWindow();
	}

}
