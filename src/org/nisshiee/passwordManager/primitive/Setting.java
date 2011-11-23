package org.nisshiee.passwordManager.primitive;

public class Setting {
	public final static String SERVICE_ICON_FILE_NAME = "/img/service.gif";
	public final static String FILE_ICON_FILE_NAME = "/img/pmfile.png";// 名前キモッ
	public final static String APPLICATION_ICON_FILE_NAME = "/img/frameIcon";
	public final static String APPLICATION_ICON_FILE_EXT = ".png";

	public final static String XML_CONTEXT_PATH = "org.nisshiee.passwordManager.xml";

	public final static boolean USE_CIPHER = true;
	public final static String CIPHER_SCHEME = "RC2";

	public final static String DIRNAME_OF_SAVING_PMFILE = "./REG-FILE/";

	/**
	 * 登録ファイルの暗号化時の拡張子
	 */
	public final static String ENCODE_FILE_EXT = ".pmf";
	/**
	 * ファイルを追加する際の最大暗号化ファイルサイズ．<br>
	 * これを超える場合は次のファイルが作成される．
	 */
	public final static int ENCODE_FILE_SIZE = 1000000;
}
