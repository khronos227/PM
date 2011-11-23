package org.nisshiee.passwordManager.gui.listener;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.nisshiee.passwordManager.Logic;
import org.nisshiee.passwordManager.primitive.Setting;

public class CenterCanvasListener implements MouseListener, ActionListener {
	private JTable wordTable;
	private Logic logic;

	private JPopupMenu popup;
	private JMenuItem copyItem;
	private JMenuItem updateItem;
	private JMenuItem addItem;
	private JMenuItem deleteItem;

	private JList markList;
	private JList fileList;

	public static final String COPY_COMMAND = "copySelectedWord";
	public static final String UPDATE_WORD_COMMAND = "updateSelectedWord";
	public static final String ADD_ELEMENT = "newElement";
	public static final String DELETE_ELEMENT = "deleteElement";

	/**
	 * コンストラクタ
	 * 
	 * @param logic
	 *            操作中継器
	 * @param wordTable
	 */
	public CenterCanvasListener(Logic logic, JTable wordTable, JList markList,
			JList fileList) {
		super();
		this.logic = logic;
		this.wordTable = wordTable;

		this.markList = markList;
		this.fileList = fileList;

		popup = new JPopupMenu();
		this.copyItem = new JMenuItem("コピー");
		copyItem.setActionCommand(COPY_COMMAND);
		copyItem.addActionListener(this);

		this.updateItem = new JMenuItem("変更");
		updateItem.setActionCommand(UPDATE_WORD_COMMAND);
		updateItem.addActionListener(this);

		this.addItem = new JMenuItem("要素追加");
		addItem.setActionCommand(ADD_ELEMENT);
		addItem.addActionListener(this);

		this.deleteItem = new JMenuItem("要素削除");
		this.deleteItem.setActionCommand(DELETE_ELEMENT);
		this.deleteItem.addActionListener(this);

		this.popup.add(copyItem);
		this.popup.add(updateItem);
		this.popup.add(addItem);
		this.popup.add(deleteItem);
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
		this.markList.clearSelection();
		this.fileList.clearSelection();
		reselect(e);
		if (e.isPopupTrigger() && this.logic.getSelectedService() != null) {
			setMenuEnable();
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	private void reselect(MouseEvent e) {
		Point p = e.getPoint();
		int column = this.wordTable.columnAtPoint(p);
		int row = this.wordTable.rowAtPoint(p);
		this.wordTable.changeSelection(row, column, false, false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int row = this.wordTable.getSelectedRow();
		String command = e.getActionCommand();
		if (command.equals(COPY_COMMAND)) {
			this.logic.copyValue(row);
		} else if (command.equals(UPDATE_WORD_COMMAND)) {
			this.logic.updateValue(row, this.wordTable);
			this.logic.saveFile(Setting.USE_CIPHER);
		} else if (command.equals(ADD_ELEMENT)) {
			this.logic.addWordElement2(this.wordTable);
			this.logic.saveFile(Setting.USE_CIPHER);
		} else if (command.equals(DELETE_ELEMENT)) {
			this.logic.deleteWordElement(wordTable.getSelectedRow());
			this.logic.saveFile(Setting.USE_CIPHER);
		}
	}

	private void setMenuEnable() {
		boolean enable = true;
		if (!this.wordTable.isColumnSelected(1)) {
			enable = false;
		}
		this.copyItem.setEnabled(enable);
		this.updateItem.setEnabled(enable);
		enable = true;
		if (this.wordTable.getModel().getRowCount() == 0
				|| this.wordTable.getSelectedRow() == -1) {
			enable = false;
		}
		this.deleteItem.setEnabled(enable);
	}
}
