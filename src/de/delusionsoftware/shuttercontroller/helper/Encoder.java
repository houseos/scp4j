/*
shutter-controller-android
The app that allows you to control your shutter-controller devices.

SPDX-License-Identifier: GPL-3.0-or-later

Copyright (C) 2018 Benjamin Schilling
*/

package de.delusionsoftware.shuttercontroller.helper;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Base64;

public class Encoder {

	private String base64(final byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	public String base64thenURL(final byte[] data) {
		return url(base64(data));
	}

	public String url(final String data) {
		return URLEncoder.encode(data, Charset.forName("UTF-8"));
	}

}
