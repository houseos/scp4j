/*
shutter-controller-android
The app that allows you to control your shutter-controller devices.

SPDX-License-Identifier: GPL-3.0-or-later

Copyright (C) 2018 Benjamin Schilling
*/

package de.delusionsoftware.shuttercontroller.device;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import de.delusionsoftware.shuttercontroller.helper.AES128CBC;
import de.delusionsoftware.shuttercontroller.helper.Encoder;
import de.delusionsoftware.shuttercontroller.helper.HTTP;
import de.delusionsoftware.shuttercontroller.helper.Tools;
import de.delusionsoftware.shuttercontroller.network.shutterControlProtocol.JSONkeys;
import de.delusionsoftware.shuttercontroller.network.shutterControlProtocol.NetworkCommands;

public final class Device {

	public static final String DEFAULT_PWD = "1234567890123456";

	private static Device craftDeviceFromDescoverResponse(final String ipAddress, final String response) {
		if ((response == null) || (ipAddress == null)) {
			return null;
		}
		final Map<JSONkeys, String> json = JSONkeys.parseJSON(response);

		final String responseType = json.get(JSONkeys.TYPE);
		if ("discover-response".equalsIgnoreCase(responseType)) {
			final String recDeviceID = json.get(JSONkeys.DEVICE_ID);
			final String recDeviceType = json.get(JSONkeys.DEVICE_TYPE);
			int recNumberPWDchanges = 0;
			try {
				recNumberPWDchanges = Integer.parseInt(json.get(JSONkeys.CURRENT_PWD_NO));
			} catch (final NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}

			final String recPWD;
			if (recNumberPWDchanges == 0) {
				recPWD = Device.DEFAULT_PWD;
			} else {
				// TODO retrieve corresponding PWD from database
				recPWD = "TODO";
			}
			if ((recDeviceID != null) && (recDeviceType != null) && (recNumberPWDchanges >= 0)) {
				try {
					final Mac hmac = Mac.getInstance("HmacSHA256");
					if (hmac != null) {
						hmac.init(new SecretKeySpec(Device.DEFAULT_PWD.getBytes(), hmac.getAlgorithm()));
						final byte[] bytesBuffer = (responseType + recDeviceID + recDeviceType + recNumberPWDchanges)
								.getBytes(Charset.forName("UTF-8"));
						for (final byte b : bytesBuffer) {
							System.out.print(b);
							System.out.print(",");
						}
						System.out.println();
						System.out.println("HMAC: " + Tools.toHex(hmac.doFinal(bytesBuffer)));
					}
					System.out.println(
							"HMACDecoded: " + Tools.toHex(Base64.getDecoder().decode(json.get(JSONkeys.HMAC))));
				} catch (final Throwable th) {
					th.printStackTrace();
				}
				return new Device(recDeviceID, recDeviceType, ipAddress, recNumberPWDchanges, recPWD);
			}
		}
		return null;
	}

	public static Device sendDiscoverHello(final String ipAddress) {
		Device ret = null;
		final String response = HTTP
				.sendRequest("http://" + ipAddress + "/secure-control/discover-hello?payload=discover-hello");
		if (response != null) {
			ret = Device.craftDeviceFromDescoverResponse(ipAddress, response);
		}
		return ret;
	}

	private final String deviceID;

	private final String deviceType;

	private final String ipAddress;

	private String lastKnowState = "";

	private int numberPWDchanges = 0;

	private String pwd = Device.DEFAULT_PWD;

	public Device(final String deviceID, final String deviceType, final String ipAddress, final int numberPWDchanges,
			final String pwd) {
		this.deviceID = deviceID;
		this.deviceType = deviceType;
		this.ipAddress = ipAddress;
		this.numberPWDchanges = numberPWDchanges;
		this.pwd = pwd;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getLastKnowState() {
		return lastKnowState;
	}

	public int getNumberPWDchanges() {
		return numberPWDchanges;
	}

	/**
	 * return the "status" from the http response JSON object
	 *
	 * @param command
	 * @return
	 */
	public String sendSecurityCommand(final NetworkCommands.Commands command) {
		lastKnowState = "";
		final StringBuilder urlString = new StringBuilder();
		urlString.append("http://");
		urlString.append(ipAddress);
		urlString.append("/");
		urlString.append(command.endPoint);
		urlString.append("?payload=");

		// encrypt device id with password
		final AES128CBC cryptEngine = new AES128CBC();
		final Encoder encoder = new Encoder();

		final StringBuilder payload = new StringBuilder();
		payload.append("1223213213"); // TODO Fetch real NVCN
		payload.append(":");
		payload.append(deviceID);
		payload.append(":");
		payload.append(command.messageType);

		final String encodedData = encoder.base64thenURL(cryptEngine.encrypt(payload.toString(), pwd));
		urlString.append(encodedData);
		final String requestSring = urlString.toString();
		System.out.println("URL: " + requestSring);
		final String response = HTTP.sendRequest(requestSring);
		if (response == null) {
			return lastKnowState;
		}
		final Map<JSONkeys, String> responseMap = JSONkeys.parseJSON(response);
		if (!deviceID.equalsIgnoreCase(responseMap.get(JSONkeys.DEVICE_ID))) {
			System.err.println("Response Device-ID did not match!");
			return lastKnowState;
		}
		if (!command.messageType.equalsIgnoreCase(responseMap.get(JSONkeys.DEVICE_TYPE))) {
			System.err.println("Control device-type did not match!");
			return lastKnowState;
		}
		lastKnowState = responseMap.get(JSONkeys.STATUS);
		return lastKnowState;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(Device.class.getCanonicalName());
		sb.append("\n  Device ID: ");
		sb.append(deviceID);
		sb.append("\n  Device Type: ");
		sb.append(deviceType);
		sb.append("\n  IP Address: ");
		sb.append(ipAddress);
		sb.append("\n  Password changes: ");
		sb.append(numberPWDchanges);
		sb.append("\n  Password: ");
		sb.append(pwd);
		return sb.toString();
	}
}
