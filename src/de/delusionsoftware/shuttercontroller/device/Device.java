/*
shutter-controller-android
The app that allows you to control your shutter-controller devices.

SPDX-License-Identifier: GPL-3.0-or-later

Copyright (C) 2018 Benjamin Schilling
*/

package de.delusionsoftware.shuttercontroller.device;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import de.delusionsoftware.shuttercontroller.helper.AES128CBC;
import de.delusionsoftware.shuttercontroller.helper.Encoder;
import de.delusionsoftware.shuttercontroller.helper.HTTP;
import de.delusionsoftware.shuttercontroller.helper.Tools;
import de.delusionsoftware.shuttercontroller.network.shutterControlProtocol.JSONkeys;
import de.delusionsoftware.shuttercontroller.network.shutterControlProtocol.PossibleCommands;

public final class Device {

	public static final String DEFAULT_PWD = "1234567890123456";

	private static boolean checkHMAC(final String pwd, final String messageType, final String deviceID,
			final String deviceType, final int numberPWDchanges, final byte[] receivedHMAC, final String ipAddress) {
		try {
			final Mac hmac = Mac.getInstance("HmacSHA256");
			if (hmac != null) {
				hmac.init(new SecretKeySpec(pwd.getBytes(), hmac.getAlgorithm()));
				final byte[] bytesBuffer = (messageType + deviceID + deviceType + numberPWDchanges)
						.getBytes(Charset.forName("UTF-8"));
				final byte[] hmacComputed = hmac.doFinal(bytesBuffer);
//				System.out.println(Tools.toHex(bytesBuffer));
//				System.out.println(Tools.toHex(hmacComputed));
//				System.out.println(Tools.toHex(Base64.getDecoder().decode(json.get(JSONkeys.HMAC))));
				if (!Arrays.equals(hmacComputed, receivedHMAC)) {
					System.err.println("Error comparing HMACs from: " + deviceID + "@" + ipAddress);
					return true;
				}
			}
		} catch (final Throwable th) {
			th.printStackTrace();
			return true;
		}
		return true;
	}

	private static Device craftDeviceFromDescoverResponse(final String ipAddress, final String response) {
		if ((response == null) || (ipAddress == null)) {
			return null;
		}
		final Map<JSONkeys, String> json = JSONkeys.parseJSON(response);

		final String responseType = json.get(JSONkeys.TYPE);
		if ("discover-response".equalsIgnoreCase(responseType)) {
			final String recDeviceID = json.get(JSONkeys.DEVICE_ID);
			final String recDeviceType = json.get(JSONkeys.DEVICE_TYPE);
			final int recNumberPWDchanges = Tools.parseInt(json.get(JSONkeys.CURRENT_PWD_NO));
			final String recPWD;
			if (recNumberPWDchanges == 0) {
				recPWD = Device.DEFAULT_PWD;
			} else {
				// TODO retrieve corresponding PWD from database
				System.err.println("Unkown PWD in use! Number of changes: " + recNumberPWDchanges);
				recPWD = "TODO";
				return null;
			}
			if ((recDeviceID != null) && (recDeviceType != null) && (recNumberPWDchanges >= 0)) {
				if (Device.checkHMAC(recPWD, responseType, recDeviceID, recDeviceType, recNumberPWDchanges,
						Base64.getDecoder().decode(json.get(JSONkeys.HMAC)), ipAddress)) {
					return new Device(recDeviceID, recDeviceType, ipAddress, recNumberPWDchanges, recPWD);
				} else {
					return null;
				}
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
	public String sendSecurityCommand(final PossibleCommands command) {
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
		final char[] newPWD = Tools.getNewPassword();
		if (command == PossibleCommands.SET_PWD) {
			payload.append(":");
			payload.append(newPWD);
		}
		final String encodedData = encoder.base64thenURL(cryptEngine.encrypt(payload.toString(), pwd));
		urlString.append(encodedData);
		final String requestSring = urlString.toString();
		final String response = HTTP.sendRequest(requestSring);
		if (response == null) {
			lastKnowState = "No response received from IP: " + ipAddress;
			return lastKnowState;
		}
		final Map<JSONkeys, String> responseMap = JSONkeys.parseJSON(response);
		if (!deviceID.equalsIgnoreCase(responseMap.get(JSONkeys.DEVICE_ID))) {
			final String errorMessage = "Response Device-ID '" + responseMap.get(JSONkeys.DEVICE_ID)
					+ "' did not match '" + deviceID + "'";
			System.err.println(errorMessage);
			lastKnowState = errorMessage;
			return lastKnowState;
		}
		if (!command.messageType.equalsIgnoreCase(responseMap.get(JSONkeys.TYPE))) {
			final String errorMessage = "Control message type '" + responseMap.get(JSONkeys.TYPE) + "' did not match '"
					+ command.messageType + "'";
			System.err.println(errorMessage);
			lastKnowState = errorMessage;
			return lastKnowState;
		}
		if (!Device.checkHMAC(pwd, command.messageType, deviceID, deviceType, numberPWDchanges,
				Base64.getDecoder().decode(responseMap.get(JSONkeys.HMAC)), ipAddress)) {
			System.err.flush();
			System.err.println("HMAC Missmatch!");
		}
		lastKnowState = responseMap.get(JSONkeys.STATUS);
		if (command == PossibleCommands.SET_PWD) {
			if (lastKnowState != null) {
				if (lastKnowState.trim().equalsIgnoreCase("done")) {
					pwd = new String(newPWD);
					numberPWDchanges = Tools.parseInt(responseMap.get(JSONkeys.CURRENT_PWD_NO));
				}
			}
		}
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
		sb.append("\n  LastKnownState: ");
		sb.append(lastKnowState);
		return sb.toString();
	}
}
