package org.nisshiee.passwordManager.gui.listener;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.tree.DefaultMutableTreeNode;

import org.nisshiee.passwordManager.Logic;
import org.nisshiee.passwordManager.primitive.Setting;
import org.nisshiee.passwordManager.xml.Def;

public class MarkListener implements MouseListener, ActionListener {
	private JList markList;
	private Logic logic;
	// JList serviceList;

	private JPopupMenu popup;
	private JMenuItem addItem;
	private JMenuItem deleteItem;
	private JMenuItem updateItem;

	private JTable wordTable;
	private JList fileList;

	public static final String ADD_MARK = "addMark";
	public static final String DELETE_MARK = "deleteMark";
	public static final String UPDATE_MARK_NAME = "updateMarkName";

	public MarkListener(Logic logic, JList markList, JTable wordTable,
			JList fileList) {
		this.logic = logic;
		this.markList = markList;

		this.wordTable = wordTable;
		this.fileList = fileList;

		this.addItem = new JMenuItem("マーク追加");
		this.addItem.setActionCommand(ADD_MARK);
		this.addItem.addActionListener(this);
		this.updateItem = new JMenuItem("マーク名変更");
		this.updateItem.setActionCommand(UPDATE_MARK_NAME);
		this.updateItem.addActionListener(this);
		this.deleteItem = new JMenuItem("マーク削除");
		this.deleteItem.setActionCommand(DELETE_MARK);
		this.deleteItem.addActionListener(this);
		this.popup = new JPopupMenu();
		popup.add(this.addItem);
		popup.add(this.updateItem);
		popup.add(this.deleteItem);
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
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		reselect(e);
		DefaultMutableTreeNode node = this.logic.getSelectedTreeNode();
		if (e.isPopupTrigger() && node != null && !node.getAllowsChildren()) {
			this.setMenuEnable();
			this.popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	private void reselect(MouseEvent e) {
		Object obj = e.getSource();
		if (obj instanceof JList) {
			this.wordTable.clearSelection();
			this.fileList.clearSelection();
			JList list = (JList) obj;
			if (list.getLastVisibleIndex() >= 0) {
				int index = list.getLastVisibleIndex();
				Rectangle r = list.getCellBounds(index, index);
				if (r.getY() + r.getHeight() > e.getY()) {
					index = list.locationToIndex(e.getPoint());
					this.markList.setSelectedIndex(index);
				}
			}
		}
	}

	private void setMenuEnable() {
		boolean enable = false;
		if (markList.getSelectedValue() != null) {
			enable = true;
		}
		this.deleteItem.setEnabled(enable);
		this.updateItem.setEnabled(enable);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals(ADD_MARK)) {
			this.logic.addMark(this.logic.getSelectedService());
			this.logic.saveFile(Setting.USE_CIPHER);
		} else if (command.equals(DELETE_MARK)) {
			this.logic.deleteMark((Def) this.markList.getSelectedValue());
			this.logic.saveFile(Setting.USE_CIPHER);
		} else if (command.equals(UPDATE_MARK_NAME)) {
			this.logic.updateMarkName((Def) markList.getSelectedValue());
			this.logic.saveFile(Setting.USE_CIPHER);
		}
	}

}
