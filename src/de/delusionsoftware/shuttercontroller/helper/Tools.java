package de.delusionsoftware.shuttercontroller.helper;

import java.security.SecureRandom;

public final class Tools {
	public static final String hexChars = "0123456789ABCDEF";
	public static final String pwCharacters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";
	private static final SecureRandom sr = new SecureRandom();

	public static char[] getNewPassword() {
		final char[] pwChars = new char[16];
		for (int i = 0; i < pwChars.length; ++i) {
			pwChars[i] = Tools.pwCharacters.charAt(Tools.sr.nextInt(Tools.pwCharacters.length()));
		}
		return pwChars;
	}

	public static int parseInt(final String strNumber) {
		try {
			return Integer.parseInt(strNumber);
		} catch (final Throwable th) {
		}
		return Integer.MIN_VALUE;
	}

	public static String toHex(final byte[] ba) {
		if (ba == null) {
			return "";
		}
		final StringBuilder sb = new StringBuilder(1024);
		boolean other = false;
		for (final byte b : ba) {
			sb.append(Tools.hexChars.charAt((b >> 4) & 15));
			sb.append(Tools.hexChars.charAt(b & 15));
			if (other) {
				sb.append(' ');
			}
			other = !other;
		}
		return sb.toString();
	}

	private Tools() {
	}
}
