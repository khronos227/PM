package org.nisshiee.passwordManager.gui.listener;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.tree.DefaultMutableTreeNode;

import org.nisshiee.passwordManager.Logic;
import org.nisshiee.passwordManager.gui.MainFrame;
import org.nisshiee.passwordManager.primitive.Setting;
import org.nisshiee.passwordManager.xml.PMFile;

public class PMFileListener implements MouseListener, ActionListener {
	private JList markList;
	private MainFrame mainFrame;
	private Logic logic;

	private JPopupMenu popup;
	private JMenuItem addItem;
	private JMenuItem deleteItem;
	private JMenuItem outputItem;

	private JTable wordTable;
	private JList fileList;

	public static final String ADD_FILE = "addFile";
	public static final String DELETE_FILE = "deleteFile";
	public static final String OUTPUT_FILE = "outputFile";

	public PMFileListener(MainFrame mainFrame, Logic logic, JList fileList,
			JTable wordTable, JList markList) {
		this.mainFrame = mainFrame;
		this.logic = logic;
		this.fileList = fileList;
		this.wordTable = wordTable;
		this.markList = markList;

		this.addItem = new JMenuItem("ファイル登録");
		this.addItem.setActionCommand(ADD_FILE);
		this.addItem.addActionListener(this);
		this.deleteItem = new JMenuItem("ファイル削除");
		this.deleteItem.setActionCommand(DELETE_FILE);
		this.deleteItem.addActionListener(this);
		this.outputItem = new JMenuItem("ファイル出力");
		this.outputItem.setActionCommand(OUTPUT_FILE);
		this.outputItem.addActionListener(this);
		this.popup = new JPopupMenu();
		popup.add(this.addItem);
		popup.add(this.deleteItem);
		popup.add(this.outputItem);
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
			this.markList.clearSelection();
			JList list = (JList) obj;
			if (list.getLastVisibleIndex() >= 0) {
				int index = list.getLastVisibleIndex();
				Rectangle r = list.getCellBounds(index, index);
				if (r.getY() + r.getHeight() > e.getY()) {
					index = list.locationToIndex(e.getPoint());
					this.fileList.setSelectedIndex(index);
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals(ADD_FILE)) {
			JFileChooser chooser = new JFileChooser(new File("./"));
			int state = chooser.showOpenDialog(this.mainFrame);
			File file = chooser.getSelectedFile();
			if (state == JFileChooser.APPROVE_OPTION && file != null
					&& file.exists() && file.isFile()) {
				this.logic.registPMFile(this.logic.getSelectedService(), file);
				this.logic.saveFile(Setting.USE_CIPHER);
			}
		} else if (command.equals(DELETE_FILE)) {
			this.logic.deletePMFile((PMFile) this.fileList.getSelectedValue());
			this.logic.saveFile(Setting.USE_CIPHER);
		} else if (command.equals(OUTPUT_FILE)) {
			JFileChooser chooser = new JFileChooser(new File("./"));
			int state = chooser.showSaveDialog(this.mainFrame);
			File file = chooser.getSelectedFile();
			PMFile pmf = (PMFile) this.fileList.getSelectedValue();
			if (state == JFileChooser.APPROVE_OPTION && file != null
					&& pmf != null) {
				this.logic.outputPMFile(pmf, file);
				this.logic.saveFile(Setting.USE_CIPHER);
				JOptionPane.showMessageDialog(this.mainFrame, "出力が終了しました．");
			}
		}
	}

	private void setMenuEnable() {
		boolean enabled = false;
		if (this.fileList.getSelectedValue() != null) {
			enabled = true;
		}
		this.deleteItem.setEnabled(enabled);
		this.outputItem.setEnabled(enabled);
	}
}
