package org.nisshiee.passwordManager;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.nisshiee.passwordManager.adaptor.InputFocusAdapter;
import org.nisshiee.passwordManager.gui.MainFrame;
import org.nisshiee.passwordManager.gui.listener.PassWordInputListener;
import org.nisshiee.passwordManager.primitive.PMCipher;
import org.nisshiee.passwordManager.primitive.PMCipherException;
import org.nisshiee.passwordManager.primitive.PasswordGenerator;
import org.nisshiee.passwordManager.primitive.Setting;
import org.nisshiee.passwordManager.xml.Config;
import org.nisshiee.passwordManager.xml.Data;
import org.nisshiee.passwordManager.xml.Db;
import org.nisshiee.passwordManager.xml.Def;
import org.nisshiee.passwordManager.xml.Directory;
import org.nisshiee.passwordManager.xml.Mark;
import org.nisshiee.passwordManager.xml.MarkDefs;
import org.nisshiee.passwordManager.xml.ObjectFactory;
import org.nisshiee.passwordManager.xml.PMFile;
import org.nisshiee.passwordManager.xml.PassSetting;
import org.nisshiee.passwordManager.xml.Service;
import org.nisshiee.passwordManager.xml.Word;
import org.nisshiee.passwordManager.xml.WordSetting;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Logic {
	private MainFrame mainFrame;
	private Db db;
	private String fileName;
	private Map<BigInteger, Integer> markCountMap;
	private String password;

	private boolean edited = false;

	private boolean hidePass = false;
	private boolean makePass = true;
	private int passLength = 8;
	private boolean useLowerCase = true;
	private boolean useUpperCase = false;
	private boolean useNumber = true;

	public Logic(MainFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
		this.fileName = null;
		this.markCountMap = new HashMap<BigInteger, Integer>();
		this.password = null;
	}

	@SuppressWarnings("unchecked")
	public JAXBElement<Db> createXMLObjectForMerge(String data) {
		JAXBElement<Db> res = null;
		if (data == null) {
			return null;
		}
		String data2 = data.trim();
		try {
			if (!data2.startsWith("<?xml")) {
				String message = "ファイルを復号するためのパスワードを入力してください.";
				JPasswordField passField = new JPasswordField();
				Object[] objs = { message, passField };
				JOptionPane optionPane = new JOptionPane(objs,
						JOptionPane.QUESTION_MESSAGE,
						JOptionPane.OK_CANCEL_OPTION);
				passField.addKeyListener(new PassWordInputListener(optionPane,
						passField));
				JDialog dialog = optionPane.createDialog(this.mainFrame,
						"復号パスワード入力");
				dialog.addWindowListener(new InputFocusAdapter<JTextField>(
						passField));
				dialog.setVisible(true);
				if (optionPane.getValue() == null
						|| ((Integer) optionPane.getValue()) == JOptionPane.CANCEL_OPTION) {
					return null;
				}
				String password = new String(passField.getPassword());
				data = new String(PMCipher.decrypt(data, password,
						Setting.CIPHER_SCHEME));
				this.password = password;
			}
			// xmlのxsd versionに下位互換性を持たせる．
			data = validateXMLVersion(data);

			JAXBContext jc = JAXBContext.newInstance(Setting.XML_CONTEXT_PATH);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			res = (JAXBElement<Db>) unmarshaller.unmarshal(new StringReader(
					data));
		} catch (JAXBException e) {
			res = null;
		} catch (PMCipherException e) {
			String message = "復号に失敗しました.";
			JOptionPane.showMessageDialog(this.mainFrame, message, "復号エラー",
					JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			String message = "予期せぬ事態でシステムは停止します．";
			JOptionPane.showMessageDialog(this.mainFrame, message, "予期せぬエラー",
					JOptionPane.ERROR_MESSAGE);
		}
		return res;
	}

	public void merge(String data) throws Exception {
			Db db = this.createXMLObjectForMerge(data).getValue();
			// まずはserivceに対するマージ
			this.mergeService(this.db.getData().getService(), db.getData()
					.getService());
			// 次に各ディレクトリについて
			// そもそもディレクトリが存在しなければそれを追加
			// 存在すればディレクトリ内を確認
			this.mergeDirectory(this.db.getData().getDirectory(), db.getData()
					.getDirectory());
			this.mainFrame.setServiceList(this.db.getData());
			this.selectService(new Service());
	}

	private void mergeService(List<Service> toList, List<Service> fromList) {
		if (toList != null && fromList != null) {
			List<Service> serviceList = new ArrayList<Service>(fromList);
			for (Service s : toList) {
				serviceList.remove(s);
			}
			toList.addAll(serviceList);
		}
	}

	private void mergeDirectory(List<Directory> toList, List<Directory> fromList) {
		if (toList != null && fromList != null) {
			// ディレクトリ操作による影響を受けないように現状のListを生成
			List<Directory> directoryList = new ArrayList<Directory>(toList);
			for (Directory d : fromList) {
				if (directoryList.contains(d)) {// ディレクトリ内走査
					Directory dir = toList.get(toList.indexOf(d));
					// service
					this.mergeService(dir.getService(), d.getService());
					// さらに深部のディレクトリについて
					this.mergeDirectory(dir.getDirectory(), d.getDirectory());
				} else {
					toList.add(d);
				}
			}
		}
	}

	private Service containService(Service s1, List<Service> serviceList) {
		Service res = null;
		String name = s1.getName();
		for (Service service : serviceList) {
			if (service.getName().equals(name)) {
				res = service;
			}
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	private JAXBElement<Db> createXMLObject(File file) {
		if (!file.exists()) {
			return null;
		}
		JAXBElement<Db> res = null;
		String data = readFile(file);
		if (data == null) {
			return null;
		}
		String data2 = data.trim();
		try {
			if (!data2.startsWith("<?xml")) {
				String message = "ファイルを復号するためのパスワードを入力してください.";
				JPasswordField passField = new JPasswordField();
				Object[] objs = { message, passField };
				JOptionPane optionPane = new JOptionPane(objs,
						JOptionPane.QUESTION_MESSAGE,
						JOptionPane.OK_CANCEL_OPTION);
				passField.addKeyListener(new PassWordInputListener(optionPane,
						passField));
				JDialog dialog = optionPane.createDialog(this.mainFrame,
						"復号パスワード入力");
				dialog.addWindowListener(new InputFocusAdapter<JTextField>(
						passField));
				dialog.setVisible(true);
				if (optionPane.getValue() == null
						|| ((Integer) optionPane.getValue()) == JOptionPane.CANCEL_OPTION) {
					JOptionPane.showMessageDialog(this.mainFrame,
							"システムを終了します．", "終了報告",
							JOptionPane.INFORMATION_MESSAGE);
					System.exit(0);
				}
				String password = new String(passField.getPassword());
				data = new String(PMCipher.decrypt(data, password,
						Setting.CIPHER_SCHEME));
				this.password = password;
			}
			// xmlのxsd versionに下位互換性を持たせる．
			data = validateXMLVersion(data);

			JAXBContext jc = JAXBContext.newInstance(Setting.XML_CONTEXT_PATH);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			res = (JAXBElement<Db>) unmarshaller.unmarshal(new StringReader(
					data));
		} catch (JAXBException e) {
			e.printStackTrace();
			res = null;
			this.password = null;
		} catch (PMCipherException e) {
			String message = file.getName() + "の復号に失敗しました.\n" + "システムを停止します.";
			JOptionPane.showMessageDialog(this.mainFrame, message, "復号エラー",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			String message = "予期せぬ事態でシステムは停止します．";
			JOptionPane.showMessageDialog(this.mainFrame, message, "予期せぬエラー",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		return res;
	}

	private String validateXMLVersion(String data) throws Exception {
		// string to document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		StringReader sr = new StringReader(data);
		InputSource is = new InputSource(sr);
		Document doc = builder.parse(is);
		Element root = doc.getDocumentElement();

		// check xml version
		root.setAttribute("xmlns", ObjectFactory.getXSDVersion());

		// document to string
		StringWriter sw = new StringWriter();
		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer transformer = tfactory.newTransformer();
		transformer.transform(new DOMSource(doc), new StreamResult(sw));
		return sw.toString();
	}

	private String readFile(File file) {
		String res = "";
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "utf-8");
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (res.length() != 0) {
					res += "\n";
				}
				res += line;
			}
			br.close();
			isr.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			res = null;
		} catch (IOException e) {
			e.printStackTrace();
			res = null;
		}
		return res;
	}

	private void readConfig() {
		if (this.db.getConfig() != null) {
			Config config = this.db.getConfig();
			readWordSetting(config);
		} else {
			this.db.setConfig(this.createConfig());
		}
	}

	private Config createConfig() {
		Config res = new Config();
		res.setWordSetting(this.createWordSetting());
		return res;
	}

	private void readWordSetting(Config config) {
		if (config.getWordSetting() != null) {
			WordSetting wSetting = config.getWordSetting();
			if (wSetting.isHidePass() != null) {
				this.hidePass = wSetting.isHidePass();
			}
			readPassSetting(wSetting);
		} else {
			config.setWordSetting(this.createWordSetting());
		}
	}

	private WordSetting createWordSetting() {
		WordSetting res = new WordSetting();
		res.setPassSetting(this.createPassSetting());
		return res;
	}

	private void readPassSetting(WordSetting wSetting) {
		if (wSetting.getPassSetting() != null) {
			PassSetting pSetting = wSetting.getPassSetting();
			if (pSetting.isMakePass() != null) {
				this.makePass = pSetting.isMakePass();
			}
			if (pSetting.getPassLength() != null) {
				this.passLength = pSetting.getPassLength();
			}
			if (pSetting.isUseLowerCase() != null) {
				this.useLowerCase = pSetting.isUseLowerCase();
			}
			if (pSetting.isUseUpperCase() != null) {
				this.useUpperCase = pSetting.isUseUpperCase();
			}
			if (pSetting.isUseNumber() != null) {
				this.useNumber = pSetting.isUseNumber();
			}
		} else {
			wSetting.setPassSetting(this.createPassSetting());
		}
	}

	private PassSetting createPassSetting() {
		PassSetting res = new PassSetting();
		return res;
	}

	public boolean isHidePass() {
		return hidePass;
	}

	public void setHidePass(boolean hidePass) {
		this.hidePass = hidePass;
		this.db.getConfig().getWordSetting().setHidePass(hidePass);
	}

	public boolean isMakePass() {
		return makePass;
	}

	public void setMakePass(boolean makePass) {
		this.makePass = makePass;
		this.db.getConfig().getWordSetting().getPassSetting()
				.setMakePass(makePass);
	}

	public int getPassLength() {
		return passLength;
	}

	public void setPassLength(int passLength) {
		this.passLength = passLength;
		this.db.getConfig().getWordSetting().getPassSetting()
				.setPassLength(passLength);
	}

	public boolean isUseLowerCase() {
		return useLowerCase;
	}

	public void setUseLowerCase(boolean useLowerCase) {
		this.useLowerCase = useLowerCase;
		this.db.getConfig().getWordSetting().getPassSetting()
				.setUseLowerCase(useLowerCase);
	}

	public boolean isUseUpperCase() {
		return useUpperCase;
	}

	public void setUseUpperCase(boolean useUpperCase) {
		this.useUpperCase = useUpperCase;
		this.db.getConfig().getWordSetting().getPassSetting()
				.setUseUpperCase(useUpperCase);
	}

	public boolean isUseNumber() {
		return useNumber;
	}

	public void setUseNumber(boolean useNumber) {
		this.useNumber = useNumber;
		this.db.getConfig().getWordSetting().getPassSetting()
				.setUseNumber(useNumber);
	}

	public void openFile(File file) {
		boolean enable = true;
		this.edited = false;
		this.hidePass = false;
		this.makePass = true;
		this.passLength = 8;
		this.useLowerCase = true;
		this.useUpperCase = false;
		this.useNumber = true;
		JAXBElement<Db> je = createXMLObject(file);
		if (je != null) {
			Db db = je.getValue();
			if (!checkValidation(db)) {
				String msg = file.getAbsoluteFile() + "に整合性のエラーを検出しました．\n"
						+ "\"了解\"を押すと整合性の取れた状態で表示を行います．\n"
						+ "この状態では保存はされていません．\n";
				JOptionPane.showMessageDialog(this.mainFrame, msg,
						"ファイルの整合性エラー", JOptionPane.WARNING_MESSAGE);
			}
			this.db = db;
			this.mainFrame.setServiceList(this.db.getData());
			this.selectService(new Service());
			this.fileName = file.getName();

			// dbから設定を読み込む(あれば)
			this.readConfig();
		} else {
			this.db = new Db();
			this.db.setData(new Data());
			this.db.setMarkDefs(new MarkDefs());
			this.db.setConfig(this.createConfig());
			if (this.fileName == null) {
				enable = false;
			}
			JOptionPane.showMessageDialog(this.mainFrame,
					file.getAbsoluteFile() + "のオープンに失敗しました.\n"
							+ "ファイルを新規作成します.", "エラー",
					JOptionPane.WARNING_MESSAGE);
			this.fileName = "password.xml";
		}
		// 登録されている全マークを取得
		if (this.db.getMarkDefs() != null) {
			for (Def def : this.db.getMarkDefs().getDef()) {
				this.markCountMap.put(def.getId(), 0);
			}
			// 各マークの参照数をカウント
			for (Service service : this.db.getData().getService()) {
				for (Mark mark : service.getMark()) {
					markCountMap.put(mark.getId(),
							markCountMap.get(mark.getId()) + 1);
				}
			}
		}
		this.mainFrame.getOverwriteItem().setEnabled(enable);
		if (Setting.USE_CIPHER) {
			this.mainFrame.getOverwriteWithNewPassItem().setEnabled(enable);
			if (this.password == null) {
				enable = false;
			} else {
				enable = true;
			}
		}
		this.setMenuItemEnabled(this.mainFrame.getDeleteItem(), false);
	}

	/**
	 * 指定されたファイルに暗号化したデータを出力します.<br>
	 * 初回出力の場合は暗号入力を要求します． 暗号化されていたファイルを更新する場合は前回使用していたパスワードを再び使用します．
	 * 
	 * @param file
	 */
	public void saveFile(File file, boolean useCipher) {
		try {
			// パスワード確認
			if (useCipher) {
				if (this.password == null) {
					String message = "ファイルを復号するためのパスワードを入力してください.\n(5～128文字)";
					JTextField passField = new JTextField();
					Object[] msg = { message, "パスワード:", passField };
					JOptionPane optionPane = new JOptionPane(msg,
							JOptionPane.QUESTION_MESSAGE,
							JOptionPane.OK_CANCEL_OPTION);
					passField.addKeyListener(new PassWordInputListener(
							optionPane, passField));
					JDialog dialog = optionPane.createDialog(this.mainFrame,
							"パスワード入力");
					dialog.addWindowListener(new InputFocusAdapter<JTextField>(
							passField));
					dialog.setVisible(true);
					if (optionPane.getValue() == null) {
						return;
					}
					Integer select = (Integer) optionPane.getValue();
					if (select == JOptionPane.OK_OPTION) {
						this.password = passField.getText();
					} else {
						return;
					}
				}
			}

			JAXBContext jc = JAXBContext
					.newInstance("org.nisshiee.passwordManager.xml");
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);
			FileOutputStream fos = new FileOutputStream(file);
			JAXBElement<Db> je = new ObjectFactory().createDb(db);
			StringWriter sw = new StringWriter();
			marshaller.marshal(je, sw);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
			BufferedWriter bw = new BufferedWriter(osw);
			String data = new String(sw.getBuffer());
			if (useCipher) {
				if (this.password != null) {
					data = new String(PMCipher.encrypt(data, this.password,
							Setting.CIPHER_SCHEME));
				}
			}
			bw.write(data);
			bw.close();
			osw.close();
			fos.close();
			this.fileName = file.getName();
			if (Setting.USE_CIPHER) {
				this.mainFrame.getOverwriteItem().setEnabled(true);
				this.mainFrame.getOverwriteWithNewPassItem().setEnabled(true);
			}
			this.edited = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveFile(boolean useCipher) {

		if (this.fileName == null) {
			JFileChooser chooser = new JFileChooser(new File("./"));
			chooser.showSaveDialog(this.mainFrame);
			File file = chooser.getSelectedFile();

			if (file == null || file.getName().length() == 0) {
				this.fileName = "password" + System.currentTimeMillis()
						+ ".xml";
				JOptionPane.showMessageDialog(this.mainFrame, "ファイルは\""
						+ fileName + "\"として出力されます．", "報告",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
		this.saveFile(new File(this.fileName), useCipher);
	}

	public void saveFileWithNewPass() {
		this.password = null;
		saveFile(new File(this.fileName), true);
	}

	/**
	 * 指定されたファイルにデータを出力します．<br>
	 * 
	 * @param file
	 * @param useCipher
	 *            暗号化を行うか
	 * @param resetOldPass
	 *            前回まで使用していたパスワードをリセットして新たなパスワードを設定するかどうか
	 */
	public void saveFileWithCipher(File file, boolean useCipher,
			boolean resetOldPass) {
		try {
			JAXBContext jc = JAXBContext
					.newInstance("org.nisshiee.passwordManager.xml");
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);

			FileOutputStream fos = new FileOutputStream(file);
			JAXBElement<Db> je = new ObjectFactory().createDb(db);
			StringWriter sw = new StringWriter();
			marshaller.marshal(je, sw);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
			BufferedWriter bw = new BufferedWriter(osw);
			String data = new String(sw.getBuffer());
			if (Setting.USE_CIPHER) {
				if (useCipher) {
					if (resetOldPass) {
						this.password = null;
					}
					String message = "ファイルを復号するためのパスワードを入力してください.";
					if (this.password == null) {
						this.password = JOptionPane.showInputDialog(
								this.mainFrame, message);
					}
					if (this.password == null) {
						return;
					} else if (this.password.length() < 5
							|| this.password.length() > 128) {
						message = "ファイル出力は行われませんでした.\n";
						message += "パスワードは5文字以上128文字以下(半角)で設定してください.";
						JOptionPane.showMessageDialog(this.mainFrame, message,
								"不正なパスワード", JOptionPane.WARNING_MESSAGE);
						this.password = null;
						return;
					}
					data = new String(PMCipher.encrypt(data, this.password,
							Setting.CIPHER_SCHEME));
				}
			}
			bw.write(data);
			bw.close();
			osw.close();
			fos.close();
			this.fileName = file.getName();
			if (Setting.USE_CIPHER) {
				this.mainFrame.getOverwriteItem().setEnabled(true);
				this.mainFrame.getOverwriteWithNewPassItem().setEnabled(true);
			}
			this.edited = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void selectService(Service service) {
		this.mainFrame.setService(service, this.db.getMarkDefs());
	}

	public void search(String string) {
		// 大文字小文字の差異を吸収
		string = string.toLowerCase(Locale.ENGLISH);

		// 検索HITするマークを列挙
		Set<BigInteger> hitMarks = new HashSet<BigInteger>();
		MarkDefs markDefs = this.db.getMarkDefs();
		if (markDefs != null) {
			for (Def def : markDefs.getDef()) {
				String name = def.getName().toLowerCase(Locale.ENGLISH);
				if (name.contains(string)) {
					hitMarks.add(def.getId());
				}
			}
		}

		List<Service> serviceList = new ArrayList<Service>(this.db.getData()
				.getService());
		for (Directory directory : this.db.getData().getDirectory()) {
			getAllService(directory, serviceList);
		}

		List<Service> services = new ArrayList<Service>();
		serviceLoopLabel: for (Service service : serviceList) {
			// マークチェック
			for (Mark mark : service.getMark()) {
				if (hitMarks.contains(mark.getId())) {
					services.add(service);
					continue serviceLoopLabel;
				}
			}

			// サービス名チェック
			if (service.getName().toLowerCase(Locale.ENGLISH).contains(string)) {
				services.add(service);
				continue serviceLoopLabel;
			}

			// ワードチェック
			for (Word word : service.getWord()) {
				if (word.getName().toLowerCase(Locale.ENGLISH).contains(string)) {
					services.add(service);
					continue serviceLoopLabel;
				}
				if (word.getValue().toLowerCase(Locale.ENGLISH)
						.contains(string)) {
					services.add(service);
					continue serviceLoopLabel;
				}
			}
		}

		List<Directory> directoryList = new ArrayList<Directory>();
		for (Directory dir : this.db.getData().getDirectory()) {
			directoryList.add(dir);
			getAllDirectories(dir, directoryList);
		}
		List<Directory> dirList = new ArrayList<Directory>();
		directoryLoopLabel: for (Directory dir : directoryList) {
			// ディレクトリ名チェック
			if (dir.getName().toLowerCase(Locale.ENGLISH).contains(string)) {
				dirList.add(dir);
				continue directoryLoopLabel;
			}
		}
		this.mainFrame.setDataList(services, dirList);
		this.mainFrame.reload();
	}

	private void getAllDirectories(Directory directory, List<Directory> list) {
		for (Directory sub : directory.getDirectory()) {
			list.add(sub);
			getAllDirectories(sub, list);
		}
	}

	private void getAllService(Directory directory, List<Service> list) {
		list.addAll(directory.getService());
		for (Directory sub : directory.getDirectory()) {
			getAllService(sub, list);
		}
	}

	public void cancelSearch() {
		this.mainFrame.setServiceList(this.db.getData());
		this.mainFrame.reload();
	}

	/**
	 * 選択されているサービスを取得します．
	 * 
	 * @return
	 */
	public Service getSelectedService() {
		Service res = null;
		DefaultMutableTreeNode tmp = (DefaultMutableTreeNode) this.mainFrame
				.getServiceTree().getLastSelectedPathComponent();
		if (tmp != null && tmp.getUserObject() != null) {
			res = (Service) tmp.getUserObject();
		}
		return res;
	}

	/**
	 * 選択されているノードを取得します．
	 * 
	 * @return
	 */
	public DefaultMutableTreeNode getSelectedTreeNode() {
		return (DefaultMutableTreeNode) this.mainFrame.getServiceTree()
				.getLastSelectedPathComponent();
	}

	/**
	 * 選択されているサービスのrow番目の値を取得します.
	 * 
	 * @param row
	 * @return
	 */
	private String getWordValue(int row) {
		return this.getSelectedService().getWord().get(row).getValue();
	}

	/**
	 * 選択されているサービスのrow番目の値をクリップボードにコピーする
	 * 
	 * @param row
	 */
	public void copyValue(int row) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection ss = new StringSelection(this.getWordValue(row));
		clipboard.setContents(ss, ss);
	}

	private void copyValue(String data) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection ss = new StringSelection(data);
		clipboard.setContents(ss, ss);
	}

	/**
	 * 選択されているサービスのrow番目の値を変更します.<br>
	 * JOptionPaneによって変更する値を取得します.
	 * 
	 * @param row
	 */
	public void updateValue(int row, JTable wordTable) {
		Service service = this.getSelectedService();
		Word word = service.getWord().get(row);
		String message = "値の変更を行います.\n" + "対象サービス名 : " + service.getName()
				+ "\n" + "変更する値名 : " + word.getName() + "\n" + "元の値 : "
				+ word.getValue();
		String data = JOptionPane.showInputDialog(this.mainFrame, message,
				word.getValue());
		if (data != null && data.length() > 0) {
			word.setValue(data);
			wordTable.setValueAt(data, row, 1);
			this.edited = true;
		}
	}

	/**
	 * 開いているファイル名を取得します.
	 * 
	 * @return
	 */
	public String getOpeningFileName() {
		return this.fileName;
	}

	public void deleteService() {
		DefaultMutableTreeNode node = this.getSelectedTreeNode();
		if (!node.isRoot()) {
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
					.getParent();
			String msg = "";
			if (node.getAllowsChildren()) {// ディレクトリの場合
				msg = "ディレクトリ \"" + node.getUserObject().toString()
						+ "\"内の全てのサービス及びディレクトリ";
			} else {// サービスの場合
				msg = "サービス \"" + ((Service) node.getUserObject()).getName()
						+ "\"";
			}
			msg += "を削除してもよいですか?";
			int select = JOptionPane.showConfirmDialog(this.mainFrame, msg,
					"削除", JOptionPane.YES_NO_OPTION);
			switch (select) {
			case 0:
				if (parent.isRoot()) {
					if (node.getAllowsChildren()) {
						this.db.getData().getDirectory()
								.remove((Directory) node.getUserObject());
					} else {
						this.db.getData().getService()
								.remove((Service) node.getUserObject());
					}
				} else {
					Directory dir = (Directory) parent.getUserObject();
					if (node.getAllowsChildren()) {
						dir.getDirectory().remove(
								(Directory) node.getUserObject());
					} else {
						dir.getService().remove((Service) node.getUserObject());
					}
				}
				parent.remove(node);
				this.updateFrame();
				this.edited = true;
				break;
			default:
				break;
			}
		}
	}

	public void updateDirectoryName() {
		Directory directory = (Directory) this.getSelectedTreeNode()
				.getUserObject();
		String message = "ディレクトリ名の変更を行います.\n" + "元のディレクトリ名 : "
				+ directory.getName();
		String data = JOptionPane.showInputDialog(this.mainFrame, message,
				directory.getName());
		if (data != null && data.length() > 0) {
			directory.setName(data);
			this.edited = true;
		}
	}

	public void updateServiceName() {
		Service service = this.getSelectedService();
		String message = "サービス名の変更を行います.\n" + "元のサービス名 : " + service.getName();
		String data = JOptionPane.showInputDialog(this.mainFrame, message,
				service.getName());
		if (data != null && data.length() > 0) {
			service.setName(data);
			this.edited = true;
		}
	}

	public void addService() {
		String data = JOptionPane.showInputDialog(this.mainFrame,
				"新規追加するサービス名を入力してください");
		if (data != null) {
			int childIndex = -1;
			Service service = createService(data);
			DefaultMutableTreeNode node = this.getSelectedTreeNode();
			if (node == null || node.isRoot()) {
				childIndex = this.db.getData().getService().size();
				this.db.getData().getService().add(service);
				node = this.mainFrame.getRoot();
			} else if (!node.getAllowsChildren()) {
				node = (DefaultMutableTreeNode) node.getParent();
				if (node.getUserObject() == null) {
					node = this.mainFrame.getRoot();
					childIndex = this.db.getData().getService().size();
					this.db.getData().getService().add(service);
				} else {
					Directory directory = ((Directory) node.getUserObject());
					childIndex = directory.getService().size();
					directory.getService().add(service);
				}
			} else {// ディレクトリ内に生成する場合
				Directory directory = (Directory) node.getUserObject();
				childIndex = directory.getService().size();
				directory.getService().add(service);
			}
			DefaultMutableTreeNode aNode = this.createServiceNode(service);
			node.insert(aNode, childIndex);

			this.edited = true;
			this.updateFrame();
			this.selectService(service);
			this.mainFrame.getServiceTree().setSelectionPath(
					new TreePath(aNode.getPath()));
		}
	}

	/**
	 * 新規ディレクトリの追加
	 */
	public void addDirectory() {
		String data = JOptionPane.showInputDialog(this.mainFrame,
				"新規追加するディレクトリ名を入力してください");
		if (data != null) {
			Directory directory = createDirectory(data);
			DefaultMutableTreeNode node = this.getSelectedTreeNode();
			if (node == null || node.isRoot()) {
				// System.out.println("debug  rootに追加");
				this.db.getData().getDirectory().add(directory);
				node = this.mainFrame.getRoot();
			} else if (!node.getAllowsChildren()) {// serviceにフォーカスがあっている場合
				node = (DefaultMutableTreeNode) node.getParent();
				// System.out.println("debug for insert " + node);
				if (node.getUserObject() == null) {
					this.db.getData().getDirectory().add(directory);
					node = this.mainFrame.getRoot();
				} else {
					((Directory) node.getUserObject()).getDirectory().add(
							directory);

				}
			} else {// directoryにフォーカスがあっている場合
				((Directory) node.getUserObject()).getDirectory()
						.add(directory);
			}
			node.add(this.createDirectoryNode(directory));
			// this.updateFrame(this.db.getData());
			// this.selectService(service);

			this.edited = true;
			this.updateFrame();
			// this.updateFrame(db.getData().getService());
		}

	}

	private DefaultMutableTreeNode createServiceNode(Service service) {
		DefaultMutableTreeNode res = new DefaultMutableTreeNode(service);
		res.setAllowsChildren(false);
		return res;
	}

	/**
	 * 新しいサービスを生成します
	 * 
	 * @param name
	 * @return
	 */
	private Service createService(String name) {
		Service service = new Service();
		service.setName(name);
		List<Word> words = service.getWord();
		Word w = new Word();
		w.setName("ID");
		w.setValue("");
		words.add(w);
		w = new Word();
		w.setName("Pass");
		String value = "";
		if (this.makePass) {
			value = PasswordGenerator.make(this.passLength, this.useLowerCase,
					this.useUpperCase, this.useNumber);
		}
		w.setValue(value);
		words.add(w);
		return service;
	}

	private DefaultMutableTreeNode createDirectoryNode(Directory directory) {
		DefaultMutableTreeNode res = new DefaultMutableTreeNode(directory);
		res.setAllowsChildren(true);
		return res;
	}

	private Directory createDirectory(String name) {
		Directory directory = new Directory();
		directory.setName(name);
		return directory;
	}

	/**
	 * アプリケーションを更新する
	 * 
	 * @param serviceList
	 */
	private void updateFrame() {
		TreePath path = this.mainFrame.getServiceTree().getPathForRow(0);
		Enumeration<TreePath> tmp = this.mainFrame.getServiceTree()
				.getExpandedDescendants(path);

		this.mainFrame.reload();
		this.selectService(new Service());

		JTree tree = this.mainFrame.getServiceTree();
		while (tmp != null && tmp.hasMoreElements()) {
			tree.expandPath((TreePath) tmp.nextElement());
		}
	}

	/**
	 * サービスに登録されているマークを取り除きます．
	 */
	public void deleteMark(Def def) {
		Service service = getSelectedService();
		List<Mark> markList = service.getMark();
		BigInteger id = def.getId();
		List<Mark> deleteList = new ArrayList<Mark>();
		for (Mark mark : markList) {
			if (mark.getId().equals(id)) {
				deleteList.add(mark);
			}
		}
		markList.removeAll(deleteList);
		this.mainFrame.setService(service, this.db.getMarkDefs());
		this.markCountMap
				.put(id, this.markCountMap.get(id) - deleteList.size());
		if (this.markCountMap.get(id) == 0) {
			this.markCountMap.remove(id);
			this.db.getMarkDefs().getDef()
					.remove(getDef(id, this.db.getMarkDefs().getDef()));
		}
		this.edited = true;
	}

	/**
	 * サービスにマークを登録します.
	 * 
	 * @param serviceIcon
	 */
	public void addMark(Service service) {
		// Service service = (Service) node.getUserObject();
		List<Mark> markList = service.getMark();
		List<BigInteger> mIdList = new ArrayList<BigInteger>();
		for (Mark m : markList) {
			mIdList.add(m.getId());
		}
		List<BigInteger> idList = new ArrayList<BigInteger>();
		List<String> markNameList = new ArrayList<String>();
		for (Def def : this.db.getMarkDefs().getDef()) {
			if (!idList.contains(def.getId()) && !mIdList.contains(def.getId())) {
				idList.add(def.getId());
				markNameList.add(def.getName());
			}
		}
		String newMark = "新規マーク";
		markNameList.add(newMark);
		String selection = (String) JOptionPane.showInputDialog(this.mainFrame,
				"サービスにマークを追加します.", "マーク追加", JOptionPane.QUESTION_MESSAGE, null,
				markNameList.toArray(), newMark);
		if (selection != null) {
			Def def = null;
			if (selection.equals(newMark)) {
				def = createNewMark(this.db.getMarkDefs().getDef());
			} else {
				def = getDef(selection, this.db.getMarkDefs().getDef());
			}
			if (def != null && !mIdList.contains(def.getId())) {
				Mark mark = new Mark();
				mark.setId(def.getId());
				service.getMark().add(mark);
				this.selectService(service);
				int count = 1;
				if (this.markCountMap.containsKey(def.getId())) {
					count = this.markCountMap.get(def.getId());
				}
				this.markCountMap.put(def.getId(), count);
				this.edited = true;
			}
		}
	}

	private Def createNewMark(List<Def> defList) {
		long maxId = Long.MIN_VALUE;
		List<String> nameList = new ArrayList<String>();
		for (Def def : defList) {
			nameList.add(def.getName());
			if (maxId < def.getId().longValue()) {
				maxId = def.getId().longValue();
			}
		}
		String data = JOptionPane.showInputDialog(this.mainFrame,
				"新たに作成するマーク名を入力してください.");
		Def res = null;
		if (data != null && data.length() > 0) {
			if (!nameList.contains(data)) {
				res = new Def();
				res.setId(new BigInteger((maxId + 1) + ""));
				res.setName(data);
				defList.add(res);
			} else {
				res = defList.get(nameList.indexOf(data));
			}
		}
		return res;
	}

	private Def getDef(String name, List<Def> defList) {
		Def res = null;
		for (Def def : defList) {
			if (name.equals(def.getName())) {
				res = def;
				break;
			}
		}
		return res;
	}

	private Def getDef(BigInteger id, List<Def> defList) {
		Def res = null;
		for (Def def : defList) {
			if (id.equals(def.getId())) {
				res = def;
				break;
			}
		}
		return res;
	}

	/**
	 * マーク名の変更を行います
	 */
	public void updateMarkName(Def def) {
		if (def != null) {
			String message = "マーク名の変更を行います.\n" + "対象マーク名 : " + def.getName();
			String data = JOptionPane.showInputDialog(this.mainFrame, message);
			if (data != null && data.length() > 0) {
				def.setName(data);
				this.edited = true;
			}
		}
	}

	/**
	 * サービスに要素を追加する
	 */
	public void addWordElement(JTable wordTable) {
		List<String> elementList = new ArrayList<String>();
		for (Word word : getSelectedService().getWord()) {
			elementList.add(word.getName());
		}
		String data = JOptionPane.showInputDialog(this.mainFrame,
				"新たに作成する要素名を入力してください.");
		if (data != null && data.length() > 0 && !elementList.contains(data)) {
			addWordElement(data, wordTable);
		}
	}

	/**
	 * サービスに要素を追加する実行部メソッド
	 * 
	 * @param name
	 * @param wordTable
	 */
	private void addWordElement(String name, JTable wordTable) {
		String data = JOptionPane.showInputDialog(this.mainFrame, name
				+ "の値を入力してください.");
		if (data != null && data.length() > 0) {
			Service service = getSelectedService();
			List<Word> wordList = service.getWord();
			Word word = new Word();
			word.setName(name);
			word.setValue(data);
			wordList.add(word);
			this.mainFrame.setService(service, this.db.getMarkDefs());
			this.edited = true;
		}
	}

	private void addWordElement2(String name, String value, JTable wordTable) {
		Service service = getSelectedService();
		List<Word> wordList = service.getWord();
		Word word = new Word();
		word.setName(name);
		word.setValue(value);
		wordList.add(word);
		this.mainFrame.setService(service, this.db.getMarkDefs());
		this.edited = true;
	}

	public void addWordElement2(JTable wordTable) {
		JTextField elementName = new JTextField();
		JTextField value = new JTextField();
		Object[] msg = { "要素名:", elementName, "値:", value };
		JOptionPane op = new JOptionPane(msg, JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null, null);

		JDialog dialog = op.createDialog(this.mainFrame, "要素の追加を行います．");
		// elementNameにfocusを設定
		dialog.addWindowListener(new InputFocusAdapter<JTextField>(elementName));
		System.out.println("displayable :" + value.isDisplayable());
		dialog.setVisible(true);
		try {
			Integer select = (Integer) op.getValue();
			if (select == JOptionPane.OK_OPTION) {
				String name = elementName.getText();
				String val = value.getText();
				System.out.println(name + ":" + val);
				if (name.length() == 0 || name == null) {
					String message = "要素名が空の要素は作成することができません．";
					JOptionPane.showMessageDialog(this.mainFrame, message,
							"要素追加エラー", JOptionPane.ERROR_MESSAGE);
				} else {
					addWordElement2(name, val, wordTable);
				}
			} else {
				System.err.println("Canceled");
			}
		} catch (Exception e) {
			System.err.println("Canceled");
		}
	}

	/**
	 * サービスから要素を削除する
	 */
	public void deleteWordElement(int row) {
		Service service = getSelectedService();
		List<Word> wordList = service.getWord();
		// String name = wordList.get(row).getName();
		// List<Word> deleteList = new ArrayList<Word>();
		// for (Word w : wordList) {
		// if (name.equals(w.getName())) {
		// deleteList.add(w);
		// }
		// }
		// wordList.removeAll(deleteList);
		wordList.remove(row);
		this.mainFrame.setService(service, this.db.getMarkDefs());
		this.edited = true;
	}

	public void setMenuItemEnabled(JMenuItem item, boolean enable) {
		item.setEnabled(enable);
	}

	/**
	 * valudationを行う
	 * 
	 * @param db
	 *            読み込んできたデータ
	 * @return
	 */
	private boolean checkValidation(Db db) {
		boolean res = true;
		// System.out
		// .println("debug --------------------validation---------------------");
		List<BigInteger> idList = new ArrayList<BigInteger>();
		// 登録されているマークのリストからマークidのリストを生成
		for (Def def : db.getMarkDefs().getDef()) {
			idList.add(def.getId());
		}

		// ディレクトリに入れられていないserviceの整合性チェック
		for (Service service : db.getData().getService()) {
			res = checkValidation(service, idList);
		}

		// 各ディレクトリに対する整合性のチェック
		for (Directory directory : db.getData().getDirectory()) {
			res = checkValidation(directory, idList);
			// System.out.println(directory.getName());
		}
		// System.out
		// .println("debug ---------------------------------------------------");
		return res;
	}

	/**
	 * あるdirectoryに対するvalidationを行う．<br>
	 * 1.このディレクトリに入れられているサービスの整合性チェック<br>
	 * 2.このディレクトリに入れられているサブディレクトリに対する整合性チェック
	 * <p>
	 * 同じディレクトリ内に重複する名前のサービスが存在する場合はここでは見逃す．(そのどちらもが重要である可能性があるため．)<br>
	 * サブディレクトリ名に対しても同様．
	 * 
	 * @param directory
	 * @param idList
	 *            登録されているマークのidのリスト
	 * @return
	 */
	private boolean checkValidation(Directory directory, List<BigInteger> idList) {
		boolean res = true;
		for (Service service : directory.getService()) {
			res = checkValidation(service, idList);
		}

		for (Directory sub : directory.getDirectory()) {
			res = checkValidation(sub, idList);
		}

		return res;
	}

	/**
	 * あるserviceに対するvalidationを行う．<br>
	 * 1.登録されていないマークを参照している場合はその参照を削除する<br>
	 * 2.重複してマークを参照している場合はまとめてしまう．
	 * 
	 * @param service
	 * @param idList
	 *            登録されているマークのidのリスト
	 * @return
	 */
	private boolean checkValidation(Service service, List<BigInteger> idList) {
		boolean res = true;
		Set<BigInteger> exist = new HashSet<BigInteger>();
		Set<Mark> remove = new HashSet<Mark>();
		for (Mark mark : service.getMark()) {
			if (!idList.contains(mark.getId())) {
				remove.add(mark);
				res = false;
			} else if (!exist.contains(mark.getId())) {
				exist.add(mark.getId());
			} else {
				remove.add(mark);
				res = false;
			}
		}
		service.getMark().removeAll(remove);
		return res;
	}

	/**
	 * ノードの入れ替えを行います．<br>
	 * この処理は2段階のメソッド呼び出しによって行われます．<br>
	 * このメソッドでは移動先にノードを追加する処理を行います．
	 * 
	 * @param indeces
	 * @param movePoint
	 */
	public void moveTreeNodeFirstStep(DefaultMutableTreeNode node,
			TreePath destPath, int childIndex) {
		if (destPath != null) {
			DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) destPath
					.getLastPathComponent();

			if (childIndex < 0) {// 要素上にカーソルがあっている場合
				// node2はディレクトリ以外にはありえない.
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
						.getParent();
				Directory destDir = (Directory) node2.getUserObject();
				if (node.getAllowsChildren()) {
					Directory directory = (Directory) node.getUserObject();
					childIndex = node2.getChildCount();
					if (parent.isRoot()) {
						this.db.getData().getDirectory().remove(directory);
					} else {
						((Directory) parent.getUserObject()).getDirectory()
								.remove(directory);
					}
					destDir.getDirectory()
							.add(childIndex - destDir.getService().size(),
									directory);
				} else {
					Service service = (Service) node.getUserObject();
					childIndex = destDir.getService().size();
					if (parent.isRoot()) {
						this.db.getData().getService().remove(service);
					} else {
						((Directory) parent.getUserObject()).getService()
								.remove(service);
					}
					destDir.getService().add(childIndex, service);
				}

			} else {// 要素間にカーソルがあっている場合
				if (node.getParent().equals(node2)) {// 同一の場所での移動を行う場合

					int index = node2.getIndex(node);
					if (node.getAllowsChildren()) {// 移動物がディレクトリ

						if (index < childIndex) {
							childIndex--;
						}

						// node2.insert(node, childIndex);
						Directory directory = (Directory) node.getUserObject();
						if (node2.isRoot()) {
							this.db.getData().getDirectory().remove(directory);
							this.db.getData()
									.getDirectory()
									.add(childIndex
											- this.db.getData().getService()
													.size(), directory);
						} else {
							Directory destDir = (Directory) node2
									.getUserObject();
							destDir.getDirectory().remove(directory);
							destDir.getDirectory()
									.add(childIndex
											- ((Directory) node2.getUserObject())
													.getService().size(),
											directory);
						}

					} else {// 移動物がサービス
						if (index < childIndex) {
							childIndex--;
						}

						Service service = (Service) node.getUserObject();
						if (node2.isRoot()) {
							this.db.getData().getService().remove(service);
							this.db.getData().getService()
									.add(childIndex, service);
						} else {
							Directory directory = ((Directory) node2
									.getUserObject());
							directory.getService().remove(service);
							directory.getService().add(childIndex, service);
						}
					}
				} else {
					if (node.getAllowsChildren()) {
						Directory directory = (Directory) node.getUserObject();
						if (((DefaultMutableTreeNode) node.getParent())
								.isRoot()) {
							this.db.getData().getDirectory().remove(directory);
						} else {
							((Directory) ((DefaultMutableTreeNode) node
									.getParent()).getUserObject())
									.getDirectory().remove(directory);
						}
						if (node2.isRoot()) {
							int clog = this.db.getData().getService().size();
							this.db.getData().getDirectory()
									.add(childIndex - clog, directory);
						} else {
							Directory destDir = (Directory) node2
									.getUserObject();
							destDir.getDirectory().add(
									childIndex - destDir.getService().size(),
									directory);
						}
					} else {
						Service service = (Service) node.getUserObject();
						if (((DefaultMutableTreeNode) node.getParent())
								.isRoot()) {
							this.db.getData().getService().remove(service);
						} else {
							((Directory) ((DefaultMutableTreeNode) node
									.getParent()).getUserObject()).getService()
									.remove(service);
						}
						if (node2.isRoot()) {
							this.db.getData().getService()
									.add(childIndex, service);
						} else {
							((Directory) node2.getUserObject()).getService()
									.add(childIndex, service);
						}
					}
				}
			}
			this.destNode = node2;
			this.indexOfChild = childIndex;
		}
	}

	int indexOfChild = -1;
	DefaultMutableTreeNode destNode = null;

	/**
	 * ノードの入れ替えを行います．<br>
	 * この処理は2段階のメソッド呼び出しによって行われます．<br>
	 * このメソッドでは移動した後のゴミの不要になったノードを削除する処理を行います．
	 * 
	 */
	public void moveTreeNodeSecondStep(DefaultMutableTreeNode node) {
		this.edited = true;
		this.destNode.insert(node, this.indexOfChild);
		this.updateFrame();
		this.destNode = null;
		this.indexOfChild = -1;
	}

	public Data getData() {
		return this.db.getData();
	}

	private void writeFile(File file, byte[] data) throws IOException {
		writeFile(file, data, false);
	}

	private void writeFile(File file, byte[] data, boolean append)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(file, append);
		fos.write(data);
		fos.close();
	}

	/**
	 * 渡されたInputStreamを用いてファイルからバイト列を読み取ります．<br>
	 * 読み取るバイト列の最大長はSetting.ENCODE_FILE_SIZEによって指定されます．
	 * 
	 * @param is
	 *            InputStream
	 * @return バイト列
	 */
	private byte[] readFile(InputStream is) {
		byte[] res = null;
		try {
			List<Byte> list = new ArrayList<Byte>();
			int chr;
			while (list.size() <= Setting.ENCODE_FILE_SIZE
					&& (chr = is.read()) != -1) {
				list.add((byte) chr);
			}
			res = new byte[list.size()];
			for (int i = 0; i < list.size(); i++) {
				res[i] = list.get(i);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * サービスにファイルを登録します.
	 * 
	 * @param service
	 *            選択されているサービス
	 * @param file
	 *            登録するファイル
	 */
	public void registPMFile(Service service, File file) {
		List<PMFile> fileList = service.getFile();
		try {
			File saveDir = new File("./REG-FILE");
			if (!saveDir.exists() || !saveDir.isDirectory()) {
				saveDir.mkdirs();
			}
			FileInputStream fis = new FileInputStream(file);
			int num = 0;
			byte[] data;
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(String.valueOf(System.currentTimeMillis()).getBytes());
			// 生成されたパスワード(digest)
			byte[] digest = md5.digest();
			while ((data = readFile(fis)).length > 0) {
				byte[] enData = PMCipher.encrypt(data, digest,
						Setting.CIPHER_SCHEME);
				this.writeFile(
						new File(Setting.DIRNAME_OF_SAVING_PMFILE
								+ file.getName() + num
								+ Setting.ENCODE_FILE_EXT), enData);
				num++;
			}
			fis.close();
			PMFile f = new PMFile();
			f.setName(file.getName());
			f.setPassword(new String(new BASE64Encoder().encode(digest)));
			f.setNum(num);
			fileList.add(f);
			this.selectService(service);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (PMCipherException e) {
			e.printStackTrace();
		}
	}

	public void deletePMFileOld(PMFile file) {
		File f = new File(Setting.DIRNAME_OF_SAVING_PMFILE + file.getName());
		if (f != null && f.exists() && f.isFile()) {
			f.delete();
			this.getSelectedService().getFile().remove(file);
			this.saveFile(new File(this.fileName), Setting.USE_CIPHER);
		}
		this.selectService(this.getSelectedService());
	}

	public void deletePMFile(PMFile file) {
		if (file.getNum() != null) {
			for (int i = 0; i < file.getNum(); i++) {
				File f = new File(Setting.DIRNAME_OF_SAVING_PMFILE
						+ file.getName() + i + ".pmf");
				if (f != null && f.exists() && f.isFile()) {
					f.delete();
				}
			}
			this.getSelectedService().getFile().remove(file);
			this.saveFile(new File(this.fileName), Setting.USE_CIPHER);
			this.selectService(this.getSelectedService());
		} else {
			deletePMFileOld(file);
		}
	}

	public void outputPMFile(PMFile file, File outputFile) {
		try {
			byte[] key = new BASE64Decoder().decodeBuffer(file.getPassword());
			if (file.getNum() != null) {
				for (int i = 0; i < file.getNum().intValue(); i++) {
					File f = new File(Setting.DIRNAME_OF_SAVING_PMFILE
							+ file.getName() + i + Setting.ENCODE_FILE_EXT);
					if (f != null && f.exists() && f.isFile()) {
						byte[] data = PMCipher.decrypt(this.readFile(f), key,
								Setting.CIPHER_SCHEME);
						boolean append = true;
						if (i == 0) {
							append = false;
						}
						this.writeFile(outputFile, data, append);
					} else {
						String message = "ファイル出力中にエラーが発生しました．\n"
								+ "ファイル出力処理を中断します.";
						JOptionPane.showMessageDialog(this.mainFrame, message,
								"ファイル出力エラー", JOptionPane.ERROR_MESSAGE);
						break;
					}
				}
			} else {
				outputPMFileOld(file, outputFile);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PMCipherException e) {
			e.printStackTrace();
		}
	}

	public void outputPMFileOld(PMFile file, File outputFile) {
		try {
			String path = outputFile.getPath();
			int index = path.lastIndexOf(".pmf");
			if (index > 0) {
				path = path.substring(0, index);
			}
			outputFile = new File(path);
			File f = new File(Setting.DIRNAME_OF_SAVING_PMFILE + file.getName());
			if (f != null && f.exists() && f.isFile()) {
				byte[] data = PMCipher.decrypt(this.readFile(f),
						new BASE64Decoder().decodeBuffer(file.getPassword()),
						Setting.CIPHER_SCHEME);
				this.writeFile(outputFile, data);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PMCipherException e) {
			e.printStackTrace();
		}
	}

	private void deleteDirectory(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				if (f.isFile()) {
					f.delete();
				} else {
					deleteDirectory(f);
				}
			}
			file.delete();
		}
	}

	/**
	 * 暗号化された者たちを生データとして出力する．
	 * 
	 * @param file
	 *            出力先ディレクトリ
	 */
	public void doExport(File file) {
		// JFileChooser chooser = new JFileChooser(new File("./"));
		// chooser.setMultiSelectionEnabled(false);
		// chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// int select = chooser.showOpenDialog(this.mainFrame);
		if (this.fileName == null) {
			JOptionPane.showMessageDialog(this.mainFrame,
					"このファイルは一度保存する必要があります．", "エクスポートエラー",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			if (file.exists() && file.isDirectory()) {
				String message = "ディレクトリ" + file.getName()
						+ "はすでに存在しています．\n消去してもよろしいですか？";
				int select = JOptionPane.showConfirmDialog(this.mainFrame,
						message, "削除可否", JOptionPane.YES_NO_OPTION);
				if (select == JOptionPane.YES_OPTION) {
					deleteDirectory(file);
				} else {
					return;
				}
			}
			file.mkdirs();
			// 出力先ディレクトリへの絶対パス
			String path = file.getAbsolutePath();
			String separator = System.getProperty("file.separator");
			// password.xmlを出力
			File xmlFile = new File(path + separator + this.fileName);
			this.saveFile(xmlFile, false);
			// reg-fileを出力
			String regPath = path + separator + "REG-FILE";
			File regFile = new File(regPath);
			regFile.mkdir();
			Data data = this.db.getData();
			outputPMFileFromServices(data.getService(), regPath, separator);
			outputPMFileFromDirectories(data.getDirectory(), regPath, separator);
			JOptionPane.showMessageDialog(this.mainFrame, "エクスポートが終了しました．");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void outputPMFileFromServices(List<Service> services,
			String regPath, String separator) {
		for (Service service : services) {
			for (PMFile pm : service.getFile()) {
				this.outputPMFile(pm,
						new File(regPath + separator + pm.getName()));
			}
		}
	}

	private void outputPMFileFromDirectories(List<Directory> dirs,
			String regPath, String separator) {
		for (Directory dir : dirs) {
			for (Service service : dir.getService()) {
				for (PMFile pm : service.getFile()) {
					this.outputPMFile(pm,
							new File(regPath + separator + pm.getName()));
				}
			}
			outputPMFileFromDirectories(dir.getDirectory(), regPath, separator);
		}
	}

	public boolean isEditted() {
		return this.edited;
	}

	public void createAndShowRandomPass() {
		final String pass = PasswordGenerator.make(this.passLength,
				this.useLowerCase, this.useUpperCase, this.useNumber);
		JTextField text = new JTextField(pass);
		text.setEditable(false);
		JOptionPane optionPane = new JOptionPane(text,
				JOptionPane.INFORMATION_MESSAGE);
		JPanel panel = (JPanel) optionPane.getComponent(optionPane
				.getComponentCount() - 1);
		JButton button = new JButton("クリップボードにコピー");
		button.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				copyValue(pass);
			}
		});
		panel.add(button);
		JDialog dialog = optionPane.createDialog(this.mainFrame, "パスワード生成");
		dialog.setVisible(true);
	}
}
