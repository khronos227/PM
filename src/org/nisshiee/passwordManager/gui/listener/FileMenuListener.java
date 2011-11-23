package org.nisshiee.passwordManager.gui.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.nisshiee.passwordManager.Logic;
import org.nisshiee.passwordManager.gui.MainFrame;
import org.nisshiee.passwordManager.primitive.Setting;

public class FileMenuListener implements ActionListener {
	private Logic logic;
	private MainFrame mainFrame;

	public static final String OPEN_DEFAULT_FILE = "openDefaultFile";
	public static final String OPEN_SELECTED_FILE = "openSelectedFile";
	public static final String SAVE_OVERWRITE = "overwrite";
	public static final String SAVE_NEW_FILE = "saveNewFile";
	public static final String SAVE_WITH_NEW_PASS = "saveWithNewPass";
	public static final String SAVE_WITHOUT_CIPHER = "saveWithoutCipher";

	private static final String EXPORT_DIR_NAME = "pmExport";

	public FileMenuListener(Logic logic, MainFrame mainFrame) {
		super();
		this.logic = logic;
		this.mainFrame = mainFrame;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(OPEN_DEFAULT_FILE)) {
			this.logic.openFile(new File("password.xml"));
			this.mainFrame.reload();
		} else if (e.getActionCommand().equals(OPEN_SELECTED_FILE)) {
			JFileChooser chooser = new JFileChooser(new File("./"));
			chooser.showOpenDialog(this.mainFrame);
			File file = chooser.getSelectedFile();
			if (file != null) {
				reloadFile(file);
			}
		} else if (e.getActionCommand().equals(SAVE_OVERWRITE)) {
			this.logic.saveFile(Setting.USE_CIPHER);
		} else if (e.getActionCommand().equals(SAVE_NEW_FILE)) {
			JFileChooser chooser = new JFileChooser(new File("./"));
			chooser.showSaveDialog(this.mainFrame);
			File file = chooser.getSelectedFile();
			if (file != null && file.getName().length() > 0) {
				this.logic.saveFile(file, Setting.USE_CIPHER);
			}
		} else if (e.getActionCommand().equals(SAVE_WITH_NEW_PASS)) {
			this.logic.saveFileWithNewPass();
		} else if (e.getActionCommand().equals(SAVE_WITHOUT_CIPHER)) {
			JFileChooser chooser = new JFileChooser(new File("./"));
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int select = chooser.showOpenDialog(this.mainFrame);
			if (select == JFileChooser.APPROVE_OPTION) {
				String separator = System.getProperty("file.separator");
				File file = new File(chooser.getSelectedFile().getPath()
						+ separator + EXPORT_DIR_NAME);

				if (file != null) {
					this.logic.doExport(file);
				}
			}
			// JFileChooser chooser = new JFileChooser(new File("./"));
			// chooser.showSaveDialog(this.mainFrame);
			// File file = chooser.getSelectedFile();
			// if (file != null && file.getName().length() > 0) {
			// this.logic.saveFileWithCipher(file, false, false);
			// }
		}
	}

	private void reloadFile(File file) {
		if (file.exists()) {
			this.logic.openFile(file);
		} else {
			JOptionPane.showMessageDialog(this.mainFrame, file
					.getAbsoluteFile()
					+ "は存在しません.", "エラー", JOptionPane.WARNING_MESSAGE);
		}
	}
}
