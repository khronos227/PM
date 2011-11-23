package org.nisshiee.passwordManager.gui;

import java.awt.Component;
import java.net.URL;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;

import org.nisshiee.passwordManager.primitive.Setting;
import org.nisshiee.passwordManager.xml.PMFile;

public class PasswordManagerListCellRenderer extends DefaultListCellRenderer {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 7084680069415201435L;

	private ImageIcon fileIcon;

	public PasswordManagerListCellRenderer() {
		super();
		fileIcon = null;
		URL url = getClass().getResource(Setting.FILE_ICON_FILE_NAME);
		// System.out.println("url is not null ? ::" + (url != null));
		if (url != null) {
			this.fileIcon = new ImageIcon(url);
		}
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		Component c = super.getListCellRendererComponent(list, value, index,
				isSelected, cellHasFocus);
		if (value instanceof PMFile) {
			((JLabel) c).setIcon(this.fileIcon);
		}
		return c;
	}
}
