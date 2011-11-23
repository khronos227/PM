package org.nisshiee.passwordManager.gui.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;

import org.nisshiee.passwordManager.Logic;

public class SearchListener implements ActionListener {
	private JTextField textField;
	private Logic logic;

	public static final String SEARCH_LIST = "seach";

	public SearchListener(JTextField textField, Logic logic) {
		super();
		this.textField = textField;
		this.logic = logic;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(SEARCH_LIST)) {
			textField.requestFocus();
		} else {
			this.search();
		}
	}

	private void search() {
		String string = this.textField.getText();
		if (string.isEmpty()) {
			this.logic.cancelSearch();
		} else {
			this.logic.search(string);
		}
	}

}
