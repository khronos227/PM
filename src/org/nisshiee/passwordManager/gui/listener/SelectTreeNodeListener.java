package org.nisshiee.passwordManager.gui.listener;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.nisshiee.passwordManager.Logic;
import org.nisshiee.passwordManager.gui.MainFrame;
import org.nisshiee.passwordManager.xml.Service;

public class SelectTreeNodeListener implements TreeSelectionListener {
	// private JTree serviceTree;
	private Logic logic;
	private MainFrame mainFrame;

	public SelectTreeNodeListener(JTree serviceTree, Logic logic,
			MainFrame mainFrame) {
		super();
		// this.serviceTree = serviceTree;
		this.logic = logic;
		this.mainFrame = mainFrame;
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		try {
			DefaultMutableTreeNode selectedNode = this.logic
					.getSelectedTreeNode();
			if (selectedNode != null) {
				boolean enable = true;
				if (!selectedNode.getAllowsChildren()) {
					Service service = (Service) selectedNode.getUserObject();
					this.logic.selectService(service);
				} else {
					if (selectedNode.isRoot()) {
						enable = false;
					}
					this.logic.selectService(new Service());
				}
				this.logic.setMenuItemEnabled(this.mainFrame.getDeleteItem(),
						enable);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
