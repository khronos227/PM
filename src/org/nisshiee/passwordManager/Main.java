package org.nisshiee.passwordManager;

import javax.swing.UIManager;

import org.nisshiee.passwordManager.gui.MainFrame;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		MainFrame.createAndShowGui();
	}

}
