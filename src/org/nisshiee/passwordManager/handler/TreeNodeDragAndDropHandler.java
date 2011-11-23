package org.nisshiee.passwordManager.handler;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.nisshiee.passwordManager.Logic;
import org.nisshiee.passwordManager.xml.Directory;

public class TreeNodeDragAndDropHandler extends TransferHandler {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 4349973234640907347L;

	private final DataFlavor localObjectFlavor;

	private Logic logic;

	public TreeNodeDragAndDropHandler(Logic logic) {
		super();
		this.localObjectFlavor = new ActivationDataFlavor(TreePath.class,
				DataFlavor.javaJVMLocalObjectMimeType, "node");
		this.logic = logic;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.MOVE;
	}

	@Override
	/**
	 * 選択されたノードへのパスを送信データとして整形する．
	 */
	protected Transferable createTransferable(JComponent c) {
	//	System.out.println("debug createTransterable");
		Transferable res = null;
		JTree tree = (JTree) c;
		TreePath path = tree.getSelectionPath();
		if (path != null) {
			res = new DataHandler(path, this.localObjectFlavor.getMimeType());
		}
		return res;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		// JTree.DropLocation location = (JTree.DropLocation) support
		// .getDropLocation();
		// TreePath path = location.getPath();
		// DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
		// .getLastPathComponent();
		//
		// System.out.println("debug canImport " + !node.isRoot() + "," + node
		// + "->" + (!node.isRoot() || node.getUserObject() == null));
		// return !(node.isRoot() && node != null);
		JTree.DropLocation location = (JTree.DropLocation) support
				.getDropLocation();

		TreePath path = location.getPath();// 挿入対象へのルート
		int childIndex = location.getChildIndex();// 挿入場所
		DefaultMutableTreeNode destNode = (DefaultMutableTreeNode) path
				.getLastPathComponent();// 挿入対象物
		DefaultMutableTreeNode node = null;// 挿入物
		try {
			node = (DefaultMutableTreeNode) ((TreePath) support
					.getTransferable().getTransferData(this.localObjectFlavor))
					.getLastPathComponent();
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		// System.out.println("修正---------");
		// System.out.println(childIndex);
		if (node.isRoot()) {
			return false;
		} else if (node.equals(destNode)) {
			return false;
		}
		if (childIndex < 0) {// 要素上にカーソルがあっているとき
			if (!destNode.getAllowsChildren()) {
				return false;
			} else if (((DefaultMutableTreeNode) node.getParent())
					.equals(destNode)) {
				return false;
			} else if (node.isNodeDescendant(destNode)) {
				return false;
			}
			// if (node.getParent().equals(destNode.getParent())) {//
			// 同じ場所での移動を行う場合
			// System.out.println("同じ場所ですよ");
			// if (node.getAllowsChildren()) {
			// if (destNode.getAllowsChildren()) {
			//
			// } else {
			// System.out.println("選択させません");
			// return false;
			// }
			// } else {
			// if (destNode.getAllowsChildren()) {
			//
			// } else {
			// System.out.println("選択させません");
			// return false;
			// }
			// }
			// } else {// 異なるディレクトリ間の移動を行う場合
			// if (node.getAllowsChildren()) {// 挿入物がディレクトリ
			// if (destNode.getAllowsChildren()) {// 挿入対象がディレクトリ
			//
			// } else {
			// System.out.println("選択させません");
			// return false;
			// }
			// } else {
			// if (destNode.getAllowsChildren()) {// 挿入対象がディレクトリ
			//
			// } else {
			// System.out.println("選択させません");
			// return false;
			// }
			// }
			// }
		} else {// 要素間にカーソルがあっているとき
			// if (node.getParent().equals(destNode)) {// 同じ場所の移動を行う場合
			// System.out.println("同じ場所ですよ");
			int size = 0;
			if (destNode.isRoot()) {
				size = this.logic.getData().getService().size();
			} else {
				size = ((Directory) destNode.getUserObject()).getService()
						.size();
			}
			if (node.getAllowsChildren()) {// 挿入物がディレクトリ
				if (childIndex < size) {
					return false;
				}
			} else {
				if (childIndex > size) {
					return false;
				}
			}
			// }
		}
		// System.out.println("-----------");
		// System.out.println(childIndex);
		return true;
	}

	@Override
	public boolean importData(TransferSupport support) {
		// System.out.println("debug importData");
		if (support.isDrop()) {
			if (support.getDropAction() == TransferHandler.MOVE) {
				Transferable t = support.getTransferable();
				try {
					// System.out.println("debug "
					// + ((TreePath) t
					// .getTransferData(this.localObjectFlavor))
					// .getLastPathComponent());
					// 移動対象ノード
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) ((TreePath) t
							.getTransferData(this.localObjectFlavor))
							.getLastPathComponent();
					JTree.DropLocation location = (JTree.DropLocation) support
							.getDropLocation();
					// System.out.println(location.getDropPoint());
					TreePath path = location.getPath();
					int childIndex = location.getChildIndex();
					this.logic.moveTreeNodeFirstStep(node, path, childIndex);
					// this.logic.moveTreeNode(node, location.getDropPoint());
					return true;
				} catch (Exception e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		if (action == TransferHandler.MOVE) {
			try {
				TreePath path = (TreePath) data
						.getTransferData(this.localObjectFlavor);
				DefaultMutableTreeNode node = ((DefaultMutableTreeNode) path
						.getLastPathComponent());
				this.logic.moveTreeNodeSecondStep(node);
			} catch (Exception e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}

		}
		// System.out.println("debug exportDone");
		// System.out.println("debug " + action);
		// TODO 自動生成されたメソッド・スタブ
		super.exportDone(source, data, action);
	}
}
