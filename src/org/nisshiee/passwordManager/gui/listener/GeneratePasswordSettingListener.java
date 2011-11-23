package org.nisshiee.passwordManager.gui.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nisshiee.passwordManager.Logic;

public class GeneratePasswordSettingListener implements ActionListener,
		ChangeListener {
	public static final String MAKE_PASS = "新規サービス作成時，初期パスワードを設定";
	public static final String USE_LOWER = "小文字を使用する";
	public static final String USE_UPPER = "大文字を使用する";
	public static final String USE_NUMBER = "数字を使用する";
	public static final String CREATE_PASS = "create password";

	public static final String LENGTH_SHORT = "6";
	public static final String LENGTH_NORMAL = "8";
	public static final String LENGTH_LONG = "12";

	private Logic logic;
	private JCheckBoxMenuItem makePassItem;
	private JCheckBox lowerItem, upperItem, numberItem;
	private JMenu menu;

	public GeneratePasswordSettingListener(Logic logic,
			JCheckBoxMenuItem makePassItem, JCheckBox lowerItem,
			JCheckBox upperItem, JCheckBox numberItem, JMenu menu) {
		super();
		this.logic = logic;
		this.makePassItem = makePassItem;
		this.lowerItem = lowerItem;
		this.upperItem = upperItem;
		this.numberItem = numberItem;
		this.menu = menu;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		boolean enabled = false;
		if (command.equals(MAKE_PASS)) {
			if (this.makePassItem.isSelected()) {
				enabled = true;
			}
			this.menu.setEnabled(enabled);
			this.logic.setMakePass(enabled);
		} else if (command.equals(USE_LOWER)) {
			if (this.lowerItem.isSelected()) {
				enabled = true;
				System.out.println(menu.isPopupMenuVisible());
				System.out.println(menu.isSelected());
			}
			this.logic.setUseLowerCase(enabled);
		} else if (command.equals(USE_UPPER)) {
			if (this.upperItem.isSelected()) {
				enabled = true;
			}
			this.logic.setUseUpperCase(enabled);
		} else if (command.equals(USE_NUMBER)) {
			if (this.numberItem.isSelected()) {
				enabled = true;
			}
			this.logic.setUseNumber(enabled);
		} else if (command.equals(CREATE_PASS)) {
			this.logic.createAndShowRandomPass();
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object obj = e.getSource();
		if (obj instanceof JRadioButton) {
			JRadioButton rb = (JRadioButton) obj;
			if (rb.isSelected()) {
				int length = new Integer(rb.getText());
				this.logic.setPassLength(length);
			}
		}
	}
}
