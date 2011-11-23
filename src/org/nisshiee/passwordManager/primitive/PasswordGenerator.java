package org.nisshiee.passwordManager.primitive;

import java.security.SecureRandom;

public class PasswordGenerator {
	private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String NUMBER = "0123456789";

	public static String make(int length, boolean useLower, boolean useUpper,
			boolean useNumber) {
		String res = "";
		if ((useLower || useUpper || useNumber)) {
			String data = "";
			if (useLower) {
				data += LOWER;
			}
			if (useUpper) {
				data += UPPER;
			}
			if (useNumber) {
				data += NUMBER;
			}
			StringBuffer buf = new StringBuffer();
			SecureRandom rnd = new SecureRandom();
			rnd.nextInt();
			int len = data.length();
			int range = len * 3;
			for (int i = 0; i < length; i++) {
				int val = rnd.nextInt(range);
				buf.append(data.charAt(val % len));
			}
			res = buf.toString();
		}
		return res;
	}
}
