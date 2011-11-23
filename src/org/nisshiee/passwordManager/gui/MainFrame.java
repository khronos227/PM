package org.nisshiee.passwordManager.gui;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.nisshiee.passwordManager.Logic;
import org.nisshiee.passwordManager.gui.listener.CenterCanvasListener;
import org.nisshiee.passwordManager.gui.listener.FileMenuListener;
import org.nisshiee.passwordManager.gui.listener.GeneratePasswordSettingListener;
import org.nisshiee.passwordManager.gui.listener.MarkListener;
import org.nisshiee.passwordManager.gui.listener.PMFileListener;
import org.nisshiee.passwordManager.gui.listener.SearchListener;
import org.nisshiee.passwordManager.gui.listener.SelectTreeNodeListener;
import org.nisshiee.passwordManager.gui.listener.SynchroListener;
import org.nisshiee.passwordManager.gui.listener.TreeNodeHandleListener;
import org.nisshiee.passwordManager.gui.listener.VersionListener;
import org.nisshiee.passwordManager.handler.TreeNodeDragAndDropHandler;
import org.nisshiee.passwordManager.primitive.Setting;
import org.nisshiee.passwordManager.xml.Data;
import org.nisshiee.passwordManager.xml.Def;
import org.nisshiee.passwordManager.xml.Directory;
import org.nisshiee.passwordManager.xml.Mark;
import org.nisshiee.passwordManager.xml.MarkDefs;
import org.nisshiee.passwordManager.xml.PMFile;
import org.nisshiee.passwordManager.xml.Service;
import org.nisshiee.passwordManager.xml.Word;

public class MainFrame extends JFrame {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 2084797458663243098L;
	private Logic logic;
	private JMenuBar menuBar = new JMenuBar();
	private JMenuItem overwriteItem;
	private JMenuItem overwriteWithNewPassItem;

	public MainFrame() {
		super("Password Manager");
		this.logic = new Logic(this);
		this.createMenuBar();
		this.createCanvas();

		this.setVersionMenu();

		// URL url = getClass().getResource(Setting.APPLICATION_ICON_FILE_NAME);
		// if (url != null) {
		// // ImageIcon img = new ImageIcon(url);
		// Image img = new ImageIcon(url).getImage();
		// this.setIconImage(img);
		// }
		List<Image> iList = new ArrayList<Image>();
		for (int i = 0; i < 5; i++) {
			String fileName = Setting.APPLICATION_ICON_FILE_NAME + (i + 1)
					+ Setting.APPLICATION_ICON_FILE_EXT;
			URL url = getClass().getResource(fileName);
			if (url != null) {
				Image img = Toolkit.getDefaultToolkit().getImage(url);
				iList.add(img);
			}
			// else {
			// try {
			// Image img = ImageIO.read(new File("." + fileName));
			// if (img != null) {
			// iList.add(img);
			// }
			// } catch (IOException e) {
			// }
			// }
		}
		this.setIconImages(iList);

		this.logic.openFile(new File("password.xml"));
	}

	private void createMenuBar() {
		JMenu menu = new JMenu("ファイル( F )");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		FileMenuListener fileMenuListener = new FileMenuListener(this.logic,
				this);

		JMenuItem menuItem;

		menuItem = new JMenuItem("./password.xmlを開く", KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				InputEvent.CTRL_DOWN_MASK));
		menuItem.setActionCommand(FileMenuListener.OPEN_DEFAULT_FILE);
		menuItem.addActionListener(fileMenuListener);
		menu.add(menuItem);

		menuItem = new JMenuItem("保存", KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_DOWN_MASK));
		menuItem.setActionCommand(FileMenuListener.SAVE_OVERWRITE);
		menuItem.addActionListener(fileMenuListener);
		menu.add(menuItem);
		this.overwriteItem = menuItem;
		if (Setting.USE_CIPHER) {
			menuItem = new JMenuItem("暗号化パスワードを変更して保存");
			menuItem.setActionCommand(FileMenuListener.SAVE_WITH_NEW_PASS);
			menuItem.addActionListener(fileMenuListener);
			menu.add(menuItem);
			this.overwriteWithNewPassItem = menuItem;
		}

		JMenuItem getItem = new JMenuItem(SynchroListener.GET);
		getItem.setActionCommand(SynchroListener.GET);
		JMenuItem putItem = new JMenuItem(SynchroListener.PUT);
		putItem.setActionCommand(SynchroListener.PUT);
		JMenuItem mergeItem = new JMenuItem(SynchroListener.MERGE);
		mergeItem.setActionCommand(SynchroListener.MERGE);
		JMenuItem createItem = new JMenuItem(SynchroListener.CREATE);
		createItem.setActionCommand(SynchroListener.CREATE);
		SynchroListener syncListener = new SynchroListener(this.logic, this,
				getItem, putItem, mergeItem, createItem);
		JMenu syncMenu = new JMenu("機能");
		getItem.addActionListener(syncListener);
		syncMenu.add(getItem);
		putItem.addActionListener(syncListener);
		syncMenu.add(putItem);
		mergeItem.addActionListener(syncListener);
		syncMenu.add(mergeItem);
		createItem.addActionListener(syncListener);
		syncMenu.add(createItem);
		menu.add(syncMenu);

		menu.addSeparator();

		menuItem = new JMenuItem("ファイルを指定して開く");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
		menuItem.setActionCommand(FileMenuListener.OPEN_SELECTED_FILE);
		menuItem.addActionListener(fileMenuListener);
		menu.add(menuItem);

		menuItem = new JMenuItem("名前を指定して保存");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		menuItem.setActionCommand(FileMenuListener.SAVE_NEW_FILE);
		menuItem.addActionListener(fileMenuListener);
		menu.add(menuItem);
		if (Setting.USE_CIPHER) {
			menuItem = new JMenuItem("エクスポート");
			menuItem.setActionCommand(FileMenuListener.SAVE_WITHOUT_CIPHER);
			menuItem.addActionListener(fileMenuListener);
			menu.add(menuItem);
		}
		this.setJMenuBar(menuBar);
	}

	private void createCanvas() {
		JPanel westPanel = new JPanel();
		JPanel centerPanel = new JPanel();

		Container container = this.getContentPane();
		container.setPreferredSize(new Dimension(600, 500));
		container.setLayout(new BorderLayout());
		container.add(westPanel, BorderLayout.WEST);
		container.add(centerPanel, BorderLayout.CENTER);

		this.createWestCanvas(westPanel);
		this.createConfigMenu();
		this.createCenterCanvas(centerPanel);
	}

	private void createConfigMenu() {
		JMenu menu = new JMenu("ウィンドウ( W )");
		menu.setMnemonic(KeyEvent.VK_W);
		JCheckBoxMenuItem box = new JCheckBoxMenuItem(
				GeneratePasswordSettingListener.MAKE_PASS,
				this.logic.isMakePass());
		JMenu subMenu = new JMenu("パスワード生成設定");
		JCheckBox lowerBox = new JCheckBox(
				GeneratePasswordSettingListener.USE_LOWER,
				this.logic.isUseLowerCase());
		JCheckBox upperBox = new JCheckBox(
				GeneratePasswordSettingListener.USE_UPPER,
				this.logic.isUseUpperCase());
		JCheckBox numberBox = new JCheckBox(
				GeneratePasswordSettingListener.USE_NUMBER,
				this.logic.isUseNumber());
		subMenu.setEnabled(this.logic.isMakePass());
		JRadioButton rb1 = new JRadioButton(
				GeneratePasswordSettingListener.LENGTH_SHORT);
		JRadioButton rb2 = new JRadioButton(
				GeneratePasswordSettingListener.LENGTH_NORMAL, true);
		JRadioButton rb3 = new JRadioButton(
				GeneratePasswordSettingListener.LENGTH_LONG);
		GeneratePasswordSettingListener gpsl = new GeneratePasswordSettingListener(
				this.logic, box, lowerBox, upperBox, numberBox, subMenu);
		box.addActionListener(gpsl);
		lowerBox.addActionListener(gpsl);
		upperBox.addActionListener(gpsl);
		numberBox.addActionListener(gpsl);
		menu.add(box);
		subMenu.add(lowerBox);
		subMenu.add(upperBox);
		subMenu.add(numberBox);
		menu.add(rb1);
		menu.add(rb2);
		menu.add(rb3);
		rb1.addChangeListener(gpsl);
		rb2.addChangeListener(gpsl);
		rb3.addChangeListener(gpsl);
		ButtonGroup bg = new ButtonGroup();
		bg.add(rb1);
		bg.add(rb2);
		bg.add(rb3);
		JMenu lengthMenu = new JMenu("パスワード長");
		lengthMenu.add(rb1);
		lengthMenu.add(rb2);
		lengthMenu.add(rb3);
		subMenu.add(lengthMenu);
		menu.add(subMenu);
		JMenuItem menuItem = new JMenuItem(
				GeneratePasswordSettingListener.CREATE_PASS);
		// menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
		// InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
		menuItem.setActionCommand(GeneratePasswordSettingListener.CREATE_PASS);
		menuItem.addActionListener(gpsl);
		menu.add(menuItem);
		menuBar.add(menu);
		this.setJMenuBar(menuBar);
	}

	// サービスの新規追加，削除をWestCanvasから行うためのリスナー
	// private ServiceHandleListener serviceListener;
	private TreeNodeHandleListener treeNodeHandleListener;

	private void createWestCanvas(JPanel panel) {
		JPanel searchBoxPanel = new JPanel();
		JTree tree = new JTree(new DefaultMutableTreeNode());
		DefaultTreeCellRenderer renderer = new PasswordManagerTreeCellRenderer();
		tree.setCellRenderer(renderer);
		tree.setRootVisible(true);// rootを表示したくない場合はfalseにする
		DefaultTreeSelectionModel dtsm = new DefaultTreeSelectionModel();
		dtsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setSelectionModel(dtsm);
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.ON_OR_INSERT);
		tree.setTransferHandler(new TreeNodeDragAndDropHandler(this.logic));
		JScrollPane pane = new JScrollPane(tree);
		pane.setPreferredSize(new Dimension(200, 400));
		pane.setMinimumSize(new Dimension(200, 400));
		this.serviceTree = tree;
		SelectTreeNodeListener selectTreeNodeListener = new SelectTreeNodeListener(
				tree, this.logic, this);
		tree.addTreeSelectionListener(selectTreeNodeListener);
		this.treeNodeHandleListener = new TreeNodeHandleListener(this.logic,
				this.serviceTree);
		tree.addMouseListener(this.treeNodeHandleListener);
		// サービスの新規追加，削除コマンドをメニューバーに登録
		JMenu menu = this.menuBar.getMenu(0);
		JMenuItem menuItem;
		JMenu createMenu = new JMenu("新規");
		menuItem = new JMenuItem("サービス");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				InputEvent.CTRL_DOWN_MASK));
		menuItem.setActionCommand(TreeNodeHandleListener.NEW_SERVICE);
		menuItem.addActionListener(this.treeNodeHandleListener);
		// menuItem.setActionCommand(ServiceHandleListener.NEW_SERVICE);
		// menuItem.addActionListener(serviceListener);
		menu.addSeparator();
		createMenu.add(menuItem);
		menuItem = new JMenuItem("ディレクトリ");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		menuItem.setActionCommand(TreeNodeHandleListener.NEW_DIRECTORY);
		menuItem.addActionListener(this.treeNodeHandleListener);
		createMenu.add(menuItem);
		menu.add(createMenu);
		// menu.add(menuItem);

		panel.setLayout(new BorderLayout());
		panel.add(searchBoxPanel, BorderLayout.NORTH);
		panel.add(pane, BorderLayout.CENTER);

		this.createSearchBox(searchBoxPanel);
	}

	private void createSearchBox(JPanel panel) {

		JTextField textField = new JTextField();
		SearchListener searchListener = new SearchListener(textField,
				this.logic);
		textField.addActionListener(searchListener);
		Set<AWTKeyStroke> strokeSet = new HashSet<AWTKeyStroke>();
		strokeSet.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_F,
				InputEvent.CTRL_DOWN_MASK));
		textField.setFocusAccelerator('d');
		JButton button = new JButton("検索");
		button.addActionListener(searchListener);
		panel.setLayout(new BorderLayout());
		panel.add(textField, BorderLayout.CENTER);
		panel.add(button, BorderLayout.EAST);

		JMenu menu = new JMenu("編集( E )");
		menu.setMnemonic(KeyEvent.VK_E);
		JMenuItem menuItem;
		menuItem = new JMenuItem("検索");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
				InputEvent.CTRL_DOWN_MASK));
		menuItem.setActionCommand(SearchListener.SEARCH_LIST);
		menuItem.addActionListener(searchListener);
		menu.add(menuItem);

		menu.addSeparator();

		this.deleteItem = new JMenuItem("削除");
		this.deleteItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_DELETE, 0));
		this.deleteItem.setActionCommand(TreeNodeHandleListener.DELETE);
		this.deleteItem.addActionListener(this.treeNodeHandleListener);
		// this.deleteItem.setActionCommand(ServiceHandleListener.SERVICE_DELETE);
		// this.deleteItem.addActionListener(this.serviceListener);
		this.deleteItem.setEnabled(false);
		menu.add(this.deleteItem);

		menuBar.add(menu);
		this.setJMenuBar(menuBar);
	}

	private JMenuItem deleteItem;

	public JMenuItem getDeleteItem() {
		return this.deleteItem;
	}

	private void createCenterCanvas(JPanel panel) {
		JTable table = new JTable();
		table.setColumnSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane wordPane = new JScrollPane(table);
		JList markList = new JList();
		markList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		markList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane markListPane = new JScrollPane(markList);
		markListPane.setPreferredSize(new Dimension(400, 100));
		markListPane.setMinimumSize(new Dimension(400, 100));
		markListPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

		this.wordTable = table;
		this.markList = markList;

		JList fileList = new JList();
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fileList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane fileListPane = new JScrollPane(fileList);
		fileListPane.setPreferredSize(new Dimension(400, 100));
		fileListPane.setMinimumSize(new Dimension(400, 100));
		fileListPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
		this.fileList = fileList;
		PasswordManagerListCellRenderer listCellTenderer = new PasswordManagerListCellRenderer();
		this.fileList.setCellRenderer(listCellTenderer);

		panel.setLayout(new BorderLayout());
		panel.add(wordPane, BorderLayout.CENTER);// GUIに表示するよう設定
		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(fileListPane, BorderLayout.CENTER);
		panel2.add(markListPane, BorderLayout.SOUTH);
		panel.add(panel2, BorderLayout.SOUTH);

		// 操作用のポップアップメニュー表示用
		CenterCanvasListener centerCanvasListener = new CenterCanvasListener(
				this.logic, this.wordTable, this.markList, this.fileList);
		wordPane.addMouseListener(centerCanvasListener);
		this.wordTable.addMouseListener(centerCanvasListener);

		PMFileListener fileListener = new PMFileListener(this, this.logic,
				this.fileList, this.wordTable, this.markList);
		fileListPane.addMouseListener(fileListener);
		this.fileList.addMouseListener(fileListener);

		// TODO マーク操作のポップアップメニュー表示用
		MarkListener markListener = new MarkListener(this.logic, this.markList,
				this.wordTable, this.fileList);
		markListPane.addMouseListener(markListener);
		this.markList.addMouseListener(markListener);
		// MarkListener markListener = new MarkListener(this.logic,
		// this.markList,
		// this.serviceList);
		// markListPane.addMouseListener(markListener);
		// this.markList.addMouseListener(markListener);
	}

	private JTree serviceTree;
	// private JList serviceList;
	private JTable wordTable;
	private JList markList;
	private JList fileList;

	public JList getMarkList() {
		return this.markList;
	}

	public JTree getServiceTree() {
		return this.serviceTree;
	}

	// public JList getServiceList() {
	// return this.serviceList;
	// }

	public void setServiceList(Data data) {
		// System.out
		// .println("debug ----------------------初期データ---------------------");
		DefaultMutableTreeNode root = resetTree();
		setServiceList(data.getService(), root);
		setDirectories(data.getDirectory(), root);
		expandRoot(this.serviceTree);
		// System.out
		// .println("debug ---------------------------------------------------");
	}

	public void setServiceList() {
		resetTree();
		expandRoot(this.serviceTree);
	}

	private DefaultMutableTreeNode resetTree() {
		DefaultMutableTreeNode res = ((DefaultMutableTreeNode) this.serviceTree
				.getModel().getRoot());
		// System.out.println(res.isRoot());
		res.removeAllChildren();
		// System.out.println(res.getChildCount());
		return res;
	}

	public void setDirectories(List<Directory> directoryList,
			DefaultMutableTreeNode parent) {
		for (Directory directory : directoryList) {
			// System.out
			// .println("debug sub ----------------ディレクトリ構造生成----------------");
			// System.out.println(directory.getName());
			DefaultMutableTreeNode dir = new DefaultMutableTreeNode(directory);
			parent.add(dir);
			dir.setAllowsChildren(true);
			setServiceList(directory.getService(), dir);
			setDirectories(directory.getDirectory(), dir);
			// System.out
			// .println("debug sub -----------------------------------------------");
		}
	}

	private void setServiceList(List<Service> services,
			DefaultMutableTreeNode parent) {
		for (Service service : services) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(service);
			// System.out.println(node.getUserObject());
			node.setAllowsChildren(false);
			parent.add(node);
		}
	}

	/**
	 * ディレクトリに格納されていないサービスをTreeに追加する．(root直下)<br>
	 * search結果を表示するために使用します．
	 */
	public void setServiceList(List<Service> services) {
		DefaultMutableTreeNode root = this.resetTree();
		for (Service service : services) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(service);
			node.setAllowsChildren(false);
			root.add(node);
		}
		// System.out.println(this.serviceTree.getModel().getClass());
	}

	public void setDataList(List<Service> services, List<Directory> directory) {
		DefaultMutableTreeNode root = this.resetTree();
		for (Service service : services) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(service);
			node.setAllowsChildren(false);
			root.add(node);
		}
		this.setDirectories(directory, root);
	}

	/**
	 * JTreeのRootを展開した状態にする．
	 * 
	 * @param tree
	 *            Rootを展開したいJTree
	 */
	private void expandRoot(JTree tree) {
		DefaultTreeModel model = (DefaultTreeModel) this.serviceTree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		TreePath rootPath = new TreePath(model.getPathToRoot(root));
		tree.expandPath(rootPath);
	}

	// public void setServiceList(List<Service> services) {
	// DefaultListModel model = (DefaultListModel) this.serviceList.getModel();
	// model.clear();
	// for (Service s : services) {
	// model.addElement(s);
	// }
	// }

	public void setService(Service service, MarkDefs markDefs) {
		// ワードセット
		List<Word> words = service.getWord();
		DefaultTableModel model = new DefaultTableModel(new String[] { "name",
				"value" }, words.size()) {
			/**
			 * serialVersionUID
			 */
			private static final long serialVersionUID = -6727411618280446557L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		for (int i = 0; i < words.size(); i++) {
			Word word = words.get(i);
			model.setValueAt(word.getName(), i, 0);
			model.setValueAt(word.getValue(), i, 1);
		}
		this.wordTable.setModel(model);

		List<PMFile> files = service.getFile();
		this.fileList.setListData(files.toArray());

		// マークセット
		List<Mark> marks = service.getMark();
		List<Def> defs = new ArrayList<Def>();
		Map<BigInteger, Def> map = new HashMap<BigInteger, Def>();
		if (markDefs != null) {
			for (Def def : markDefs.getDef()) {
				map.put(def.getId(), def);
			}
			for (Mark mark : marks) {
				BigInteger id = mark.getId();
				Def def = map.get(id);
				if (def != null) {
					defs.add(def);
				}
			}
		}
		this.markList.setListData(defs.toArray());
	}

	private void setVersionMenu() {
		VersionListener versionListener = new VersionListener(this);

		JMenu menu = new JMenu("ヘルプ( H )");
		menu.setMnemonic(KeyEvent.VK_H);
		JMenuItem menuItem;
		menuItem = new JMenuItem("ヴァージョン情報");
		// menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
		// InputEvent.CTRL_DOWN_MASK));
		menuItem.setActionCommand(VersionListener.VERSION_INFORMANTION);
		menuItem.addActionListener(versionListener);
		menu.add(menuItem);

		menuBar.add(menu);
		this.setJMenuBar(menuBar);

	}

	public static void createAndShowGui() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final MainFrame frame = new MainFrame();
				frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						frame.preperationExitSystem();
						frame.dispose();
					}

					@Override
					public void windowClosed(WindowEvent e) {
						System.exit(0); // webstart
					}
				});
				frame.pack();
				frame.setVisible(true);
			}
		});
	}

	public void preperationExitSystem() {
		if (this.logic.isEditted()) {
			this.logic.saveFile(Setting.USE_CIPHER);
		}
	}

	public JMenuItem getOverwriteItem() {
		return this.overwriteItem;
	}

	public JMenuItem getOverwriteWithNewPassItem() {
		return this.overwriteWithNewPassItem;
	}

	public DefaultMutableTreeNode getRoot() {
		return (DefaultMutableTreeNode) this.serviceTree.getModel().getRoot();
	}

	/**
	 * treeに変更があったことを通知する．
	 */
	public void reload() {
		((DefaultTreeModel) this.serviceTree.getModel()).reload();
	}

	/**
	 * treeに変更があったことを通知する．
	 */
	public void reload(DefaultMutableTreeNode node) {
		((DefaultTreeModel) this.serviceTree.getModel()).reload(node);
	}

	// // TODO テストメソッド要削除
	// public static void main(String[] args) {
	// createAndShowGui();
	// }
}
