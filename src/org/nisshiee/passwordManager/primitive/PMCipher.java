package org.nisshiee.passwordManager.primitive;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class PMCipher {
	public static byte[] decrypt(String data, String key, String cipherScheme)
			throws PMCipherException {
		try {
			SecretKeySpec sksSpec = new SecretKeySpec(key.getBytes(),
					cipherScheme);
			Cipher cipher;
			cipher = Cipher.getInstance(cipherScheme);
			cipher.init(Cipher.DECRYPT_MODE, sksSpec);
			byte[] decrypted = cipher.doFinal(new BASE64Decoder()
					.decodeBuffer(data));
			return decrypted;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (BadPaddingException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (IOException e) {
			e.printStackTrace();
			throw new PMCipherException();
		}

	}

	public static byte[] decrypt(String data, byte[] key, String cipherScheme)
			throws PMCipherException {
		try {
			SecretKeySpec sksSpec = new SecretKeySpec(key, cipherScheme);
			Cipher cipher;
			cipher = Cipher.getInstance(cipherScheme);
			cipher.init(Cipher.DECRYPT_MODE, sksSpec);
			byte[] decrypted = cipher.doFinal(new BASE64Decoder()
					.decodeBuffer(data));
			return decrypted;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (BadPaddingException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (IOException e) {
			e.printStackTrace();
			throw new PMCipherException();
		}

	}

	public static byte[] encrypt(String data, String key, String cipherScheme)
			throws PMCipherException {
		try {
			SecretKeySpec sksSpec = new SecretKeySpec(key.getBytes(),
					cipherScheme);
			Cipher cipher;
			cipher = Cipher.getInstance(cipherScheme);
			cipher.init(Cipher.ENCRYPT_MODE, sksSpec);
			byte[] encrypted = cipher.doFinal(data.getBytes());
			return new BASE64Encoder().encodeBuffer(encrypted).getBytes();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (BadPaddingException e) {
			e.printStackTrace();
			throw new PMCipherException();
		}
	}

	public static byte[] encrypt(String data, byte[] key, String cipherScheme)
			throws PMCipherException {
		try {
			SecretKeySpec sksSpec = new SecretKeySpec(key, cipherScheme);
			Cipher cipher;
			cipher = Cipher.getInstance(cipherScheme);
			cipher.init(Cipher.ENCRYPT_MODE, sksSpec);
			byte[] encrypted = cipher.doFinal(data.getBytes());
			return new BASE64Encoder().encodeBuffer(encrypted).getBytes();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (BadPaddingException e) {
			e.printStackTrace();
			throw new PMCipherException();
		}
	}

	public static byte[] encrypt(byte[] data, byte[] key, String cipherScheme)
			throws PMCipherException {
		try {
			SecretKeySpec sksSpec = new SecretKeySpec(key, cipherScheme);
			Cipher cipher;
			cipher = Cipher.getInstance(cipherScheme);
			cipher.init(Cipher.ENCRYPT_MODE, sksSpec);
			byte[] encrypted = cipher.doFinal(data);
			return new BASE64Encoder().encodeBuffer(encrypted).getBytes();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			throw new PMCipherException();
		} catch (BadPaddingException e) {
			e.printStackTrace();
			throw new PMCipherException();
		}
	}

}
