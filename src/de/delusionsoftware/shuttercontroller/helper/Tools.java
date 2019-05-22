package de.delusionsoftware.shuttercontroller.helper;

public final class Tools {
	public static final String hexChars = "0123456789ABCDEF";

	public static String toHex(final byte[] ba) {
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
