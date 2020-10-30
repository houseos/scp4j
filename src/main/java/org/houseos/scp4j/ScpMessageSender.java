/*
 * secure_control_protocol
 * ScpMessageSender Class
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2020 Marcel Jaehn
 */
package org.houseos.scp4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ScpMessageSender {

    static final int PORT = 19316;
    static final int HTTP_OK = 200;
    static final int CONNECTION_TIMEOUT = 1000;

    private ScpMessageSender() {
        //this class consists only of static methods
    }

    static String sendDiscoverHello(String ip) {
        return requestHttpServer("http://" + ip + ":" + PORT + "/secure-control/discover-hello?payload=discover-hello");
    }

    static String fetchNVCN(ScpDevice device) {
        //plain text = <salt> + ":" + "security-fetch-nvcn" + ":" + <device ID>
        String salt = new ScpCrypto().generatePassword();
        String payload = salt + ":security-fetch-nvcn:" + device.deviceId;
        ScpJson scpJson = new ScpCrypto().encryptThenEncode(device.knownPassword, payload);

        String query = "nonce=" + urlEncode(scpJson.encryptedPayload.base64Nonce);
        query += "&payload=" + urlEncode(scpJson.encryptedPayload.base64Data);
        query += "&payloadLength=" + scpJson.encryptedPayload.dataLength;
        query += "&mac=" + urlEncode(scpJson.encryptedPayload.base64Mac);

        return requestHttpServer("http://" + device.ipAddress + ":" + PORT + "/secure-control?" + query);
    }

    static String sendNewPassword(ScpDevice device) {
        // get NVCN
        System.out.println("Fetching NVCN");
        String nvcnResponse = fetchNVCN(device);
        if (nvcnResponse == null) {
            return ScpStatus.RESULT_ERROR;
        }
        ScpResponseFetchNvcn parsedNvcnResponse
                = ScpResponseParser.parseNvcnResponse(nvcnResponse);

        String nvcn = parsedNvcnResponse.nvcn;
        // generate new password
        String password = new ScpCrypto().generatePassword();
        //send new password
        // <salt> + ":" + "security-pw-change" + ":" + <device ID> + ":" + <NVCN> + ":" + <new password>

        String salt = new ScpCrypto().generatePassword();
        String payload
                = salt + ":security-pw-change:" + device.deviceId + ":" + nvcn + ":" + password;
        ScpJson scpJson
                = new ScpCrypto().encryptThenEncode(device.knownPassword, payload);

        String query = "nonce=" + urlEncode(scpJson.encryptedPayload.base64Nonce);
        query += "&payload=" + urlEncode(scpJson.encryptedPayload.base64Data);
        query += "&payloadLength=" + scpJson.encryptedPayload.dataLength;
        query += "&mac=" + urlEncode(scpJson.encryptedPayload.base64Mac);

        // await response
        System.out.println("Setting new password");
        String newPasswordResponse
                = requestHttpServer("http://" + device.ipAddress + ":" + PORT + "/secure-control?" + query);

        if (newPasswordResponse == null) {
            System.out.println("failed to send new password");
            return ScpStatus.RESULT_ERROR;
        } else {
            ScpResponseSetPassword parsedResponse
                    = ScpResponseParser.parseSetPasswordResponse(
                            newPasswordResponse, password);
            if (parsedResponse != null) {
                if (parsedResponse.result.equals(ScpStatus.RESULT_DONE)) {
                    System.out.println("Successfully set new password.");
                    device.knownPassword = password;
                    device.currentPasswordNumber
                            = Integer.parseInt(parsedResponse.currentPasswordNumber);
                    device.isDefaultPasswordSet = false;
                    System.out.println(device.toString());
                    return ScpStatus.RESULT_DONE;
                }
            }
        }
        return ScpStatus.RESULT_ERROR;
    }

    static String sendWifiConfig(ScpDevice device, String ssid, String preSharedKey) {
        // get NVCN
        System.out.println("Fetching NVCN");
        String nvcnResponse = fetchNVCN(device);
        if (nvcnResponse == null) {
            return ScpStatus.RESULT_ERROR;
        }
        ScpResponseFetchNvcn parsedNvcnResponse
                = ScpResponseParser.parseNvcnResponse(nvcnResponse);

        String nvcn = parsedNvcnResponse.nvcn;

        //send new wifi credentials
        // <salt> + ":" + "security-wifi-config" + ":" + <device ID> + ":"
        // + <NVCN> + ":" + <ssid> + ":" + <pre-shared-key>
        String salt = new ScpCrypto().generatePassword();
        String payload
                = salt + ":security-wifi-config:" + device.deviceId + ":" + nvcn + ":" + ssid + ":" + preSharedKey;
        ScpJson scpJson
                = new ScpCrypto().encryptThenEncode(device.knownPassword, payload);

        String query = "nonce=" + urlEncode(scpJson.encryptedPayload.base64Nonce);
        query += "&payload=" + urlEncode(scpJson.encryptedPayload.base64Data);
        query += "&payloadLength=" + scpJson.encryptedPayload.dataLength;
        query += "&mac=" + urlEncode(scpJson.encryptedPayload.base64Mac);

        // await response
        System.out.println("Setting new wifi credentials");
        String setWifiCredentialsResponse
                = requestHttpServer("http://" + device.ipAddress + ":" + PORT + "/secure-control?" + query);

        if (setWifiCredentialsResponse == null) {
            System.out.println("failed to send Wifi credentials");
            return ScpStatus.RESULT_ERROR;
        } else {
            ScpResponseSetWifiConfig parsedResponse
                    = ScpResponseParser.parseSetWifiConfigResponse(
                            setWifiCredentialsResponse, device.knownPassword);
            if (parsedResponse != null) {
                if (parsedResponse.result.equals(ScpStatus.RESULT_SUCCESS)) {
                    System.out.println("Successfully set Wifi config, ready for restart.");
                    return ScpStatus.RESULT_DONE;
                } else if (parsedResponse.result.equals(ScpStatus.RESULT_ERROR)) {
                    System.out.println("Failed setting Wifi config.");
                    return ScpStatus.RESULT_ERROR;
                }
            }
        }
        return ScpStatus.RESULT_ERROR;
    }

    static String sendRestart(ScpDevice device) {
        // get NVCN
        System.out.println("Fetching NVCN");
        String nvcnResponse = fetchNVCN(device);
        if (nvcnResponse == null) {
            return ScpStatus.RESULT_ERROR;
        }
        ScpResponseFetchNvcn parsedNvcnResponse
                = ScpResponseParser.parseNvcnResponse(nvcnResponse);

        String nvcn = parsedNvcnResponse.nvcn;

        //send new wifi credentials
        // <salt> + ":" + "security-wifi-config" + ":" + <device ID> + ":" + <NVCN>
        String salt = new ScpCrypto().generatePassword();
        String payload = salt + ":security-restart:" + device.deviceId + ":" + nvcn;
        ScpJson scpJson
                = new ScpCrypto().encryptThenEncode(device.knownPassword, payload);

        String query = "nonce=" + urlEncode(scpJson.encryptedPayload.base64Nonce);
        query += "&payload=" + urlEncode(scpJson.encryptedPayload.base64Data);
        query += "&payloadLength=" + scpJson.encryptedPayload.dataLength;
        query += "&mac=" + urlEncode(scpJson.encryptedPayload.base64Mac);

        // await response
        System.out.println("Restarting device.");
        String restartDeviceResponse
                = requestHttpServer("http://" + device.ipAddress + ":" + PORT + "/secure-control?" + query);

        if (restartDeviceResponse == null) {
            System.out.println("failed to restart device");
            return ScpStatus.RESULT_ERROR;
        } else {
            ScpResponseRestart parsedResponse
                    = ScpResponseParser.parseRestartDeviceResponse(
                            restartDeviceResponse, device.knownPassword);
            if (parsedResponse != null) {
                if (parsedResponse.result.equals(ScpStatus.RESULT_SUCCESS)) {
                    System.out.println("Successfully restarted device.");
                    return ScpStatus.RESULT_DONE;
                } else if (parsedResponse.result.equals(ScpStatus.RESULT_ERROR)) {
                    System.out.println("Failed to restart device.");
                    return ScpStatus.RESULT_ERROR;
                }
            }
        }
        return ScpStatus.RESULT_ERROR;
    }

    static String sendResetToDefault(ScpDevice device) {
        // get NVCN
        System.out.println("Fetching NVCN");
        String nvcnResponse = fetchNVCN(device);
        if (nvcnResponse == null) {
            return ScpStatus.RESULT_ERROR;
        }
        ScpResponseFetchNvcn parsedNvcnResponse
                = ScpResponseParser.parseNvcnResponse(nvcnResponse);

        String nvcn = parsedNvcnResponse.nvcn;

        //send control command
        // <salt> + ":" + "security-reset-to-default" + ":" + <device ID> + ":" + <NVCN>
        String salt = new ScpCrypto().generatePassword();
        String payload = salt + ":security-reset-to-default:" + device.deviceId + ":" + nvcn;
        ScpJson scpJson
                = new ScpCrypto().encryptThenEncode(device.knownPassword, payload);

        String query = "nonce=" + urlEncode(scpJson.encryptedPayload.base64Nonce);
        query += "&payload=" + urlEncode(scpJson.encryptedPayload.base64Data);
        query += "&payloadLength=" + scpJson.encryptedPayload.dataLength;
        query += "&mac=" + urlEncode(scpJson.encryptedPayload.base64Mac);

        // await response
        System.out.println("Send reset to default message");
        String resetToDefaultMessage
                = requestHttpServer("http://" + device.ipAddress + ":" + PORT + "/secure-control?" + query);

        if (resetToDefaultMessage == null) {
            System.out.println("failed to send reset to default message");
            return ScpStatus.RESULT_ERROR;
        } else {
            ScpResponseResetToDefault parsedResponse = ScpResponseParser.parseResetToDefault(resetToDefaultMessage,
                    device.knownPassword);
            if (parsedResponse != null) {
                if (parsedResponse.result.equals(ScpStatus.RESULT_SUCCESS)) {
                    System.out.println("Successfully reset the device to default.");
                    return ScpStatus.RESULT_SUCCESS;
                } else if (parsedResponse.result.equals(ScpStatus.RESULT_ERROR)) {
                    System.out.println("Failed resetting the device to default.");
                    return ScpStatus.RESULT_ERROR;
                }
            }
        }
        return ScpStatus.RESULT_ERROR;
    }

    static String sendControl(ScpDevice device, String action) {
        // get NVCN
        System.out.println("Fetching NVCN");
        String nvcnResponse = fetchNVCN(device);
        if (nvcnResponse == null) {
            return ScpStatus.RESULT_ERROR;
        }

        ScpResponseFetchNvcn parsedNvcnResponse = ScpResponseParser.parseNvcnResponse(nvcnResponse);

        String nvcn = parsedNvcnResponse.nvcn;

        //send control command
        // <salt> + ":" + "control" + ":" + <device ID> + ":" + <NVCN> + ":" + action
        String salt = new ScpCrypto().generatePassword();
        String payload = salt + ":control:" + device.deviceId + ":" + nvcn + ":" + action;
        ScpJson scpJson = new ScpCrypto().encryptThenEncode(device.knownPassword, payload);

        String query = "nonce=" + urlEncode(scpJson.encryptedPayload.base64Nonce);
        query += "&payload=" + urlEncode(scpJson.encryptedPayload.base64Data);
        query += "&payloadLength=" + scpJson.encryptedPayload.dataLength;
        query += "&mac=" + urlEncode(scpJson.encryptedPayload.base64Mac);

        // await response
        System.out.println("Send control command: " + action);
        String controlResponse
                = requestHttpServer("http://" + device.ipAddress + ":" + PORT + "/secure-control?" + query);

        if (controlResponse == null) {
            System.out.println("failed to send control command");
            return ScpStatus.RESULT_ERROR;
        } else {
            ScpResponseControl parsedResponse = ScpResponseParser.parseControlResponse(controlResponse,
                    device.knownPassword);
            if (parsedResponse != null) {
                if (parsedResponse.result.equals(ScpStatus.RESULT_SUCCESS) && action.equals(parsedResponse.action)) {
                    System.out.println("Successfully controlled device.");
                    return ScpStatus.RESULT_SUCCESS;
                } else if (parsedResponse.result.equals(ScpStatus.RESULT_ERROR)
                        || !action.equals(parsedResponse.action)) {
                    System.out.println("Failed controlling device.");
                    return ScpStatus.RESULT_ERROR;
                }
            }
        }

        return ScpStatus.RESULT_ERROR;
    }

    private static String requestHttpServer(String url) {
        String response = "";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() != HTTP_OK) {
                connection.disconnect();
                return null;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response += inputLine;
            }
            in.close();
        } catch (IOException ioex) {
            // can't connect to targetURL
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return response;
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
