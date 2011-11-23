package org.nisshiee.passwordManager.gui.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.nisshiee.passwordManager.gui.MainFrame;

public class VersionListener implements ActionListener {
	private MainFrame mainFrame;
	private String version = "5.0.2";

	public static final String VERSION_INFORMANTION = "version_infomation";

	public VersionListener(MainFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(VERSION_INFORMANTION)) {
			String name = "PasswordManager";
			String about = "  The Application manage your important data.";
			String ver = "Version : " + version;
			String author = "Developed by Takefumi";
			String title = "ヴァージョン情報";
			Object[] message = { name, about, ver, author };
			JOptionPane optionPane = new JOptionPane(message,
					JOptionPane.INFORMATION_MESSAGE);
			JDialog dialog = optionPane.createDialog(this.mainFrame, title);
			dialog.setVisible(true);
		}
	}

}
