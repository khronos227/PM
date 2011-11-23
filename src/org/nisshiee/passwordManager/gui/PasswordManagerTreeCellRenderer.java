package org.nisshiee.passwordManager.gui;

import java.awt.Component;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.nisshiee.passwordManager.primitive.Setting;

public class PasswordManagerTreeCellRenderer extends DefaultTreeCellRenderer {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1542462933916194062L;

	DefaultMutableTreeNode node = null;
	ImageIcon serviceIcon = null;

	public PasswordManagerTreeCellRenderer() {
		super();
		URL url = getClass().getResource(
				Setting.SERVICE_ICON_FILE_NAME);
//		System.out.println("url is not null ? ::" + (url != null));
		if (url != null) {
			this.serviceIcon = new ImageIcon(url);
		}
		// this.serviceIcon = new ImageIcon("service.gif");
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		this.node = (DefaultMutableTreeNode) value;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		if (node.getAllowsChildren()) {
			leaf = false;
		}
		Component c = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus);
		if (leaf && this.serviceIcon != null) {
			((JLabel) c).setIcon(this.serviceIcon);
		}
		return c;
	}
}
