/*
shutter-controller-android
The app that allows you to control your shutter-controller devices.

SPDX-License-Identifier: GPL-3.0-or-later

Copyright (C) 2018 Benjamin Schilling
*/

package de.delusionsoftware.shuttercontroller.helper;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class AES128CBC {

	private static final int IV_SIZE = 16;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private Cipher cipher;

	public AES128CBC() {
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (final NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (final NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}

	public byte[] encrypt(final String data, final String key) {
		final SecretKeySpec ownKey = new SecretKeySpec(key.getBytes(), "AES");
		final byte[] iv = new byte[AES128CBC.IV_SIZE];
		AES128CBC.SECURE_RANDOM.nextBytes(iv);
		final IvParameterSpec ivSpec = new IvParameterSpec(iv);
		try {
			cipher.init(Cipher.ENCRYPT_MODE, ownKey, ivSpec);
			final byte[] cipherText = cipher.doFinal(data.getBytes());
			final byte[] returnValue = new byte[iv.length + cipherText.length];
			System.arraycopy(iv, 0, returnValue, 0, iv.length);
			System.arraycopy(cipherText, 0, returnValue, iv.length, cipherText.length);
			return returnValue;
		} catch (final Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
}
