package org.nisshiee.passwordManager.gui.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.nisshiee.passwordManager.Logic;
import org.nisshiee.passwordManager.primitive.Setting;

/**
 * サイドメニューのサービスを操作するためのリスナークラス
 * 
 * @author 太田 健文
 * 
 */
public class TreeNodeHandleListener implements MouseListener, ActionListener {

	public static final String DELETE = "serviceDelete";
	public static final String NEW_SERVICE = "newService";
	public static final String UPDATE_SERVICE_NAME = "updateServiceName";

	public static final String NEW_DIRECTORY = "newDirectory";
	public static final String UPDATE_DIRECTORY_NAME = "updateDirectoryName";

	JPopupMenu popup;
	JMenuItem deleteItem;
	JMenuItem newItem;
	JMenuItem updateItem;

	JMenuItem newDirItem;
	JMenuItem updateDirItem;

	JTree serviceTree;
	Logic logic;

	public TreeNodeHandleListener(Logic logic, JTree serviceTree) {
		super();
		this.logic = logic;
		this.serviceTree = serviceTree;

		this.popup = new JPopupMenu();

		this.newItem = new JMenuItem("新規サービス");
		this.newItem.setActionCommand(NEW_SERVICE);
		this.newItem.addActionListener(this);

		this.updateItem = new JMenuItem("サービス名変更");
		this.updateItem.setActionCommand(UPDATE_SERVICE_NAME);
		this.updateItem.addActionListener(this);

		this.newDirItem = new JMenuItem("新規ディレクトリ");
		this.newDirItem.setActionCommand(NEW_DIRECTORY);
		this.newDirItem.addActionListener(this);

		this.updateDirItem = new JMenuItem("ディレクトリ名変更");
		this.updateDirItem.setActionCommand(UPDATE_DIRECTORY_NAME);
		this.updateDirItem.addActionListener(this);

		this.deleteItem = new JMenuItem("削除");
		// this.deleteItem.setAccelerator(KeyStroke.getKeyStroke(
		// KeyEvent.VK_DELETE, 0));
		this.deleteItem.setActionCommand(DELETE);
		this.deleteItem.addActionListener(this);

		this.popup.add(this.newItem);
		this.popup.add(this.updateItem);

		this.popup.add(this.newDirItem);
		this.popup.add(this.updateDirItem);

		this.popup.add(this.deleteItem);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		reselectTreeNode(e);
		if (e.isPopupTrigger()) {
			setMenuVisible();
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		reselectTreeNode(e);
		if (e.isPopupTrigger()) {
			setMenuVisible();
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	private void reselectTreeNode(MouseEvent e) {
		Object obj = e.getSource();
		if (obj instanceof JTree) {
			JTree tree = (JTree) obj;
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			if (path != null) {
				tree.setSelectionPath(path);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals(DELETE)) {
			this.logic.deleteService();
			this.logic.saveFile(Setting.USE_CIPHER);
		} else if (command.equals(UPDATE_SERVICE_NAME)) {
			this.logic.updateServiceName();
			this.logic.saveFile(Setting.USE_CIPHER);
		} else if (command.equals(NEW_SERVICE)) {
			this.logic.addService();
			this.logic.saveFile(Setting.USE_CIPHER);
		} else if (command.equals(UPDATE_DIRECTORY_NAME)) {
			this.logic.updateDirectoryName();
			this.logic.saveFile(Setting.USE_CIPHER);
		} else if (command.equals(NEW_DIRECTORY)) {
			this.logic.addDirectory();
			this.logic.saveFile(Setting.USE_CIPHER);
		}
	}

	private void setMenuVisible() {
		TreePath path = this.serviceTree.getSelectionPath();
		// Service selectedService = (Service)
		// this.serviceList.getSelectedValue();
		boolean del = true;
		boolean serviceUpdatable = true;
		boolean directoryUpdatable = true;
		if (path == null) {
			del = false;
			serviceUpdatable = false;
			directoryUpdatable = false;
		} else {
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path
					.getLastPathComponent();
			if (selectedNode.isRoot()) {
				del = false;
				serviceUpdatable = false;
				directoryUpdatable = false;
			} else {
				if (selectedNode.getAllowsChildren()) {
					serviceUpdatable = false;
				} else {
					directoryUpdatable = false;
				}
			}
		}
		// if (selectedService == null) {
		// enable = false;
		// }
		this.deleteItem.setEnabled(del);
		this.updateItem.setEnabled(serviceUpdatable);
		this.updateDirItem.setEnabled(directoryUpdatable);
	}
}
