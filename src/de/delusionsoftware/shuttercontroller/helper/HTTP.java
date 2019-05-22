package de.delusionsoftware.shuttercontroller.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public final class HTTP {

	public static String sendRequest(final String url) {

		try {
			final URL urlobj = new URL(url);
//			System.out.println(url);
			final URLConnection conn = urlobj.openConnection();
			try (final InputStream is = conn.getInputStream();) {
				final byte[] ba = new byte[1024 * 1024];
				final int len = is.read(ba);
				if (len > 0) {
					return new String(ba, 0, len, Charset.forName("UTF-8"));
				}
			} catch (final Exception e) {
//				e.printStackTrace();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
