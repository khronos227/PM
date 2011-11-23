package org.nisshiee.passwordManager.gui.listener;

import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.nisshiee.passwordManager.Logic;
import org.nisshiee.passwordManager.adaptor.InputFocusAdapter;
import org.nisshiee.passwordManager.gui.MainFrame;
import org.nisshiee.passwordManager.primitive.Setting;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

public class SynchroListener implements ActionListener {
	public static final String PUT = "regist";
	public static final String GET = "synchronize";
	public static final String MERGE = "merge";

	public static final String CREATE = "create connection Key";

	private static final String KeyDirName = "./key/";
	private static final String PrivateFileName = "id_rsa";

	private static final String PackageDIR = "PM";
	private static final String REG_FILE = "REG-FILE";

	private JMenuItem get;
	private JMenuItem put;
	private JMenuItem merge;
	private JMenuItem create;

	private MainFrame mainFrame;
	private Logic logic;

	public SynchroListener(Logic logic, MainFrame mainFrame, JMenuItem get,
			JMenuItem put, JMenuItem merge, JMenuItem create) {
		super();
		this.logic = logic;
		this.mainFrame = mainFrame;
		this.get = get;
		this.put = put;
		this.merge = merge;
		this.create = create;
		if (new File(KeyDirName + PrivateFileName).exists()) {
			this.get.setEnabled(true);
			this.put.setEnabled(true);
			this.merge.setEnabled(true);
			this.create.setEnabled(false);
		} else {
			this.get.setEnabled(false);
			this.put.setEnabled(false);
			this.merge.setEnabled(false);
			this.create.setEnabled(true);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(PUT)) {
			this.executeAction(0);
		} else if (e.getActionCommand().equals(GET)) {
			this.executeAction(1);
			this.logic.openFile(new File("password.xml"));
		} else if (e.getActionCommand().equals(MERGE)) {
			this.executeAction(2);
			this.logic.saveFile(Setting.USE_CIPHER);
		} else if (e.getActionCommand().equals(CREATE)) {
			this.createConnectionKey();
			this.get.setEnabled(true);
			this.put.setEnabled(true);
			this.merge.setEnabled(true);
			this.create.setEnabled(false);
			JOptionPane.showMessageDialog(this.mainFrame,
					"Connection Keys is created");
		}
	}

	private JSch getJSch(String keyFilePath) throws JSchException {
		JSch res = new JSch();
		res.addIdentity(keyFilePath);
		return res;
	}

	private Session getSession(JSch jsch, String user, String host, int port)
			throws JSchException {
		Session res = jsch.getSession(user, host, 22);
		res.setUserInfo(new MyInfo());
		res.connect();
		return res;
	}

	private ChannelSftp getChannelSftp(Session session) throws JSchException {
		ChannelSftp res = (ChannelSftp) session.openChannel("sftp");
		res.connect();
		return res;
	}

	private void executeAction(int select) {
		String colon = ":";
		String hMessage = "host";
		JTextField hostField = new JTextField(20);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(new Label(hMessage));
		panel.add(new Label(colon));
		panel.add(hostField);
		String uMessage = "user";
		JTextField userField = new JTextField(10);
		JPanel panel2 = new JPanel();
		panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
		panel2.add(new Label(uMessage));
		panel2.add(new Label(colon));
		panel2.add(userField);
		// Object[] objs = { message, passField };
		Object[] objs = { panel, panel2 };
		JOptionPane optionPane = new JOptionPane(objs,
				JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		SynchroInfoInputListener siil = new SynchroInfoInputListener(
				optionPane, hostField, userField);
		hostField.addKeyListener(siil);
		userField.addKeyListener(siil);
		JDialog dialog = optionPane.createDialog(this.mainFrame, "接続設定");
		dialog.addWindowListener(new InputFocusAdapter<JTextField>(hostField));
		dialog.setVisible(true);
		if (optionPane.getValue() == null
				|| ((Integer) optionPane.getValue()) == JOptionPane.CANCEL_OPTION) {
			return;
		}
		String host = hostField.getText();
		String user = userField.getText();
		JSch jsch = null;
		try {
			jsch = this.getJSch(KeyDirName + PrivateFileName);
		} catch (JSchException e1) {
			JOptionPane.showMessageDialog(this.mainFrame, "鍵の設定に失敗しました．");
			return;
		}
		Session session = null;
		try {
			session = this.getSession(jsch, user, host, 22);
		} catch (JSchException e) {
			JOptionPane.showMessageDialog(this.mainFrame, "セッションの確立に失敗しました．");
			return;
		}
		ChannelSftp sftp = null;
		try {
			sftp = this.getChannelSftp(session);
		} catch (JSchException e) {
			if (session != null) {
				session.disconnect();
			}
			JOptionPane.showMessageDialog(this.mainFrame,
					"Sftpチャンネルの確立に失敗しました．");
			return;
		}

		// 実際の処理
		try {
			this.action(select, sftp);
		} catch (SftpException e) {
			sftp.disconnect();
			session.disconnect();
			JOptionPane.showMessageDialog(this.mainFrame, "予期せぬエラー", "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this.mainFrame, e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}

		sftp.disconnect();
		session.disconnect();
	}

	private void action(int select, ChannelSftp sftp) throws SftpException,
			Exception {
		switch (select) {
		case 0:// put
			this.putAction(sftp);
			break;
		case 1:// get
			this.getAction(sftp);
			break;
		case 2:
			this.mergeAction(sftp);
			this.mainFrame.reload();
			break;
		}
	}

	private void mergeAction(ChannelSftp sftp) throws SftpException {
		List<?> list = sftp.ls(".");
		// PMディレクトリの有無をチェック
		if (!contains(list, PackageDIR, true)) {
			sftp.mkdir(PackageDIR);
		}
		// PMディレクトリへ移動
		sftp.cd(PackageDIR);
		// password.xmlを移動
		InputStream is = sftp.get("password.xml");
		String str = "";
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (str.length() != 0) {
					str += "\n";
				}
				str += line;
			}
			br.close();
			isr.close();
			is.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this.mainFrame,
					"マージ用ファイルの読み込みに失敗しました");
			return;
		}
		if (str != null && str.length() > 0) {
			try {
				this.logic.merge(str);
				JOptionPane.showMessageDialog(this.mainFrame, "マージが完了しました．");
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this.mainFrame,
						"マージ用ファイルの読み込みに失敗しました");
				return;
			}
		}
	}

	private void putAction(ChannelSftp sftp) throws SftpException, Exception {
		List<?> list = sftp.ls(".");
		// PMディレクトリの有無をチェック
		if (!contains(list, PackageDIR, true)) {
			sftp.mkdir(PackageDIR);
		}
		// PMディレクトリへ移動
		sftp.cd(PackageDIR);
		// password.xmlを移動
		sftp.put("password.xml", "password.xml");

		list = sftp.ls(".");
		// REG-FILEディレクトリの有無をチェック
		if (contains(list, REG_FILE, true)) {
			this.rmREGDir(sftp, REG_FILE);
		}
		sftp.mkdir(REG_FILE);
		// REG-FILEディレクトリへ移動
		sftp.cd(REG_FILE);

		File reg = new File("REG-FILE");
		// 登録されたファイルを移動
		for (File file : reg.listFiles()) {
			if (file.isFile()) {
				sftp.put(file.getPath(), file.getName());
			}
		}
		JOptionPane.showMessageDialog(this.mainFrame, "コミットが完了しました");
	}

	private void rmREGDir(ChannelSftp sftp, String path) throws SftpException,
			Exception {
		try {
			List<?> list = sftp.ls(".");
			if (!contains(list, path, true)) {
				JOptionPane.showMessageDialog(this.mainFrame,
						"存在しないものの削除を行おうとしました．");
				throw new Exception("存在しないものの削除を行おうとしました．\n" + "処理を中断します．");
			}
			sftp.cd(path);
			if (!sftp.pwd().contains(REG_FILE)) {
				throw new Exception("作業対象とはなりえない場所のディレクトリに対する削除を行おうとしました．\n"
						+ "処理を中断します．\n" + path);
			}
			// パス内のファイルを取得
			list = sftp.ls(".");
			for (Object obj : list) {
				LsEntry entry = (LsEntry) obj;
				String name = entry.getFilename();
				if (!name.equals(".") && !name.equals("..")) {
					// 削除を行う対象
					if (entry.getAttrs().isDir()) {
						rmREGDir(sftp, name);
					} else {
						sftp.rm(name);
					}
				}
			}
			sftp.cd("..");
			sftp.rmdir(path);
		} catch (SftpException e) {
			e.printStackTrace();
		}
	}

	private void getAction(ChannelSftp sftp) throws SftpException {
		List<?> list = sftp.ls(".");
		// PMディレクトリの有無をチェック
		if (!contains(list, PackageDIR, true)) {
			JOptionPane.showMessageDialog(this.mainFrame, "登録されているデータが存在しません．");
			return;
		}
		// PMディレクトリへ移動
		sftp.cd(PackageDIR);
		// password.xmlを移動
		sftp.get("password.xml", "password.xml");

		list = sftp.ls(".");
		// REG-FILEディレクトリの有無をチェック
		if (!contains(list, REG_FILE, true)) {
			JOptionPane.showMessageDialog(this.mainFrame, "登録ファイル格納用ディレクトリ\""
					+ REG_FILE + "\"が存在しません．");
			return;
		}
		// REG-FILEディレクトリへ移動
		sftp.cd(REG_FILE);

		File reg = new File(REG_FILE);
		if (reg.exists()) {
			for (File file : reg.listFiles()) {
				file.delete();
			}
		} else {
			reg.mkdirs();
		}
		list = sftp.ls(".");
		// REG-FILEディレクトリの有無をチェック
		for (int i = 0; i < list.size(); i++) {
			LsEntry entry = (LsEntry) list.get(i);
			SftpATTRS attrs = entry.getAttrs();
			if (!attrs.isDir() && !attrs.isLink()) {
				String name = entry.getFilename();
				sftp.get(name, REG_FILE + "/" + name);
			}
		}
		JOptionPane.showMessageDialog(this.mainFrame, "同期が完了しました");
	}

	private boolean contains(List<?> list, String name, boolean dir) {
		boolean res = false;
		Pattern pattern = Pattern.compile(name);
		for (int i = 0; i < list.size(); i++) {
			LsEntry entry = (LsEntry) list.get(i);
			Matcher matcher = pattern.matcher(entry.getFilename());
			if (matcher.matches() && (entry.getAttrs().isDir() == dir)) {
				res = true;
				break;
			}
		}
		return res;
	}

	private void createConnectionKey() {
		String dir = "./key/";
		File file = new File(dir);
		if (!file.exists()) {
			file.mkdirs();
		}
		File keyfile = new File(dir + "id_rsa");

		final int type = KeyPair.RSA;
		final String pass = "";
		JSch jsch = new JSch();
		try {
			KeyPair keyPair = KeyPair.genKeyPair(jsch, type);
			keyPair.setPassphrase(pass);
			keyPair.writePrivateKey(keyfile.getAbsolutePath());
			keyPair.writePublicKey(keyfile.getAbsolutePath() + ".pub",
					"Public Key for PM");
			keyPair.dispose();
		} catch (JSchException e) {
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	class MyInfo implements UserInfo {
		@Override
		public void showMessage(String arg0) {
		}

		@Override
		public boolean promptYesNo(String arg0) {
			return true;
		}

		@Override
		public boolean promptPassword(String arg0) {
			return true;
		}

		@Override
		public boolean promptPassphrase(String arg0) {
			return true;
		}

		@Override
		public String getPassword() {
			return null;
		}

		@Override
		public String getPassphrase() {
			return "";
		}
	}
}
