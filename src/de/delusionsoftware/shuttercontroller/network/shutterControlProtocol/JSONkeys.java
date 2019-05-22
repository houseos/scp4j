package de.delusionsoftware.shuttercontroller.network.shutterControlProtocol;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public enum JSONkeys {
	CURRENT_PWD_NO("currentPasswordNumber"), DEVICE_ID("deviceId"), DEVICE_TYPE("deviceType"), HMAC("hmac"),
	NVCN("nvcn"), PAYLOAD("payload"), RESULT("result"), STATUS("status"), TYPE("type");

	public static Map<JSONkeys, String> parseJSON(final String response) {
		final HashMap<JSONkeys, String> map = new HashMap<>();
		try {
			final JSONObject jo = new JSONObject(response);
			for (final JSONkeys key : JSONkeys.values()) {
				final String str = jo.optString(key.key);
				if (str != null) {
					map.put(key, str.strip());
				}
			}
		} catch (final Throwable th) {
			th.printStackTrace();
		}
		return map;
	}

	public final String key;

	private JSONkeys(final String key) {
		this.key = key;
	}
}
