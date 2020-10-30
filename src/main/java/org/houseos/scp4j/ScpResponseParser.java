/*
 * secure_control_protocol
 * ScpResponseParser Class
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2020 Marcel Jaehn
 */
package org.houseos.scp4j;

import com.google.gson.Gson;
import java.util.Base64;
import java.util.List;

class ScpResponseDiscover {

    static final String TYPE = "discover-response";
    String deviceId;
    String deviceType;
    int currentPasswordNumber;
    String hmac;

    ScpResponseDiscover(String deviceId, String deviceType, int currentPasswordNumber,
            final String hmac) {
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.currentPasswordNumber = currentPasswordNumber;
        this.hmac = hmac;
    }

    // Returns a ScpResponseDiscover if HMAC valid, otherwise null
    static ScpResponseDiscover fromJson(String json, List<ScpDevice> devices) {
        Gson g = new Gson();
        ScpResponseDiscover discoverResponse = g.fromJson(json, ScpResponseDiscover.class);

        String password = null;
        if (devices != null) {
            ScpDevice scpDevice = devices.stream().filter(element -> element.deviceId.equals(discoverResponse.deviceId))
                    .findFirst().orElse(null);
            if (scpDevice != null) {
                password = scpDevice.knownPassword;
            }
        }

        // Check hmac before additional processing
        if (new ScpCrypto().verifyHMAC(ScpResponseDiscover.TYPE + discoverResponse.deviceId
                + discoverResponse.deviceType + discoverResponse.currentPasswordNumber,
                discoverResponse.hmac,
                password)) {
            return discoverResponse;
        }
        return null;
    }
}

class ScpResponseFetchNvcn {

    static final String TYPE = "security-fetch-nvcn";
    String deviceId;
    String nvcn;

    ScpResponseFetchNvcn(String deviceId, String nvcn) {
        this.deviceId = deviceId;
        this.nvcn = nvcn;
    }

    static ScpResponseFetchNvcn fromJson(String json) {
        Gson g = new Gson();
        ScpResponseFetchNvcn nvcnResponse = g.fromJson(json, ScpResponseFetchNvcn.class);

        if (nvcnResponse.deviceId == null
                || nvcnResponse.deviceId.isEmpty()
                || nvcnResponse.nvcn == null
                || nvcnResponse.nvcn.isEmpty()) {
            return null;
        }

        return nvcnResponse;
    }
}

class ScpResponseSetPassword {

    static final String EXPECTED_TYPE = "security-pw-change";
    String type;
    String deviceId;
    String currentPasswordNumber;
    String result;

    ScpResponseSetPassword(String deviceId, String currentPasswordNumber, String result) {
        this.deviceId = deviceId;
        this.currentPasswordNumber = currentPasswordNumber;
        this.result = result;
    }

    static ScpResponseSetPassword fromJson(String inputJson, String password) {
        Gson g = new Gson();
        WrappedScpResponse wrappedScpResponse = g.fromJson(inputJson, WrappedScpResponse.class);
        if (wrappedScpResponse.response == null
                || wrappedScpResponse.response.isEmpty()
                || wrappedScpResponse.hmac == null
                || wrappedScpResponse.hmac.isEmpty()) {
            return null;
        }
        String response = wrappedScpResponse.response;
        String hmac = wrappedScpResponse.hmac;

        // Check hmac before additional processing
        if (new ScpCrypto().verifyHMAC(response, hmac, password)) {
            byte[] decodedPayloadBytes = Base64.getDecoder().decode(response);
            String decodedPayload = new String(decodedPayloadBytes);
            ScpResponseSetPassword scpResponse = g.fromJson(decodedPayload, ScpResponseSetPassword.class);
            if (scpResponse.type.equals(EXPECTED_TYPE)) {
                return scpResponse;
            }
        }
        return null;
    }
}

class ScpResponseSetWifiConfig {

    static final String EXPECTED_TYPE = "security-wifi-config";
    String type;
    String deviceId;
    String result;

    ScpResponseSetWifiConfig(String deviceId, String result) {
        this.deviceId = deviceId;
        this.result = result;
    }

    static ScpResponseSetWifiConfig fromJson(String inputJson, String password) {
        Gson g = new Gson();
        WrappedScpResponse wrappedScpResponse = g.fromJson(inputJson, WrappedScpResponse.class);
        if (wrappedScpResponse.response == null
                || wrappedScpResponse.response.isEmpty()
                || wrappedScpResponse.hmac == null
                || wrappedScpResponse.hmac.isEmpty()) {
            return null;
        }
        String response = wrappedScpResponse.response;
        String hmac = wrappedScpResponse.hmac;

        // Check hmac before additional processing
        if (new ScpCrypto().verifyHMAC(response, hmac, password)) {
            byte[] decodedPayloadBytes = Base64.getDecoder().decode(response);
            String decodedPayload = new String(decodedPayloadBytes);
            ScpResponseSetWifiConfig scpResponse = g.fromJson(decodedPayload, ScpResponseSetWifiConfig.class);
            if (scpResponse.type.equals(EXPECTED_TYPE)) {
                return scpResponse;
            }
        }
        return null;
    }
}

class ScpResponseRestart {

    static final String EXPECTED_TYPE = "security-restart";
    String type;
    String deviceId;
    String result;

    ScpResponseRestart(String deviceId, String result) {
        this.deviceId = deviceId;
        this.result = result;
    }

    static ScpResponseRestart fromJson(String inputJson, String password) {
        Gson g = new Gson();
        WrappedScpResponse wrappedScpResponse = g.fromJson(inputJson, WrappedScpResponse.class);
        if (wrappedScpResponse.response == null
                || wrappedScpResponse.response.isEmpty()
                || wrappedScpResponse.hmac == null
                || wrappedScpResponse.hmac.isEmpty()) {
            return null;
        }
        String response = wrappedScpResponse.response;
        String hmac = wrappedScpResponse.hmac;

        // Check hmac before additional processing
        if (new ScpCrypto().verifyHMAC(response, hmac, password)) {
            byte[] decodedPayloadBytes = Base64.getDecoder().decode(response);
            String decodedPayload = new String(decodedPayloadBytes);
            ScpResponseRestart scpResponse = g.fromJson(decodedPayload, ScpResponseRestart.class);
            if (scpResponse.type.equals(EXPECTED_TYPE)) {
                return scpResponse;
            }
        }
        return null;
    }
}

class ScpResponseResetToDefault {

    static final String EXPECTED_TYPE = "security-reset-to-default";
    String type;
    String deviceId;
    String result;

    ScpResponseResetToDefault(String deviceId, String result) {
        this.deviceId = deviceId;
        this.result = result;
    }

    static ScpResponseResetToDefault fromJson(String inputJson, String password) {
        Gson g = new Gson();
        WrappedScpResponse wrappedScpResponse = g.fromJson(inputJson, WrappedScpResponse.class);
        if (wrappedScpResponse.response == null
                || wrappedScpResponse.response.isEmpty()
                || wrappedScpResponse.hmac == null
                || wrappedScpResponse.hmac.isEmpty()) {
            return null;
        }
        String response = wrappedScpResponse.response;
        String hmac = wrappedScpResponse.hmac;

        // Check hmac before additional processing
        if (new ScpCrypto().verifyHMAC(response, hmac, password)) {
            byte[] decodedPayloadBytes = Base64.getDecoder().decode(response);
            String decodedPayload = new String(decodedPayloadBytes);
            ScpResponseResetToDefault scpResponse = g.fromJson(decodedPayload, ScpResponseResetToDefault.class);
            if (scpResponse.type.equals(EXPECTED_TYPE)) {
                return scpResponse;
            }
        }
        return null;
    }
}

class ScpResponseControl {

    static final String EXPECTED_TYPE = "control";
    String type;
    String action;
    String deviceId;
    String result;

    ScpResponseControl(String action, String deviceId, String result) {
        this.action = action;
        this.deviceId = deviceId;
        this.result = result;
    }

    static ScpResponseControl fromJson(String inputJson, String password) {
        Gson g = new Gson();
        WrappedScpResponse wrappedScpResponse = g.fromJson(inputJson, WrappedScpResponse.class);

        if (wrappedScpResponse.response == null
                || wrappedScpResponse.response.isEmpty()
                || wrappedScpResponse.hmac == null
                || wrappedScpResponse.hmac.isEmpty()) {
            return null;
        }
        String response = wrappedScpResponse.response;
        String hmac = wrappedScpResponse.hmac;

        // Check hmac before additional processing
        if (new ScpCrypto().verifyHMAC(response, hmac, password)) {
            byte[] decodedPayloadBytes = Base64.getDecoder().decode(response);
            String decodedPayload = new String(decodedPayloadBytes);
            ScpResponseControl scpResponse = g.fromJson(decodedPayload, ScpResponseControl.class);
            if (scpResponse.type.equals(EXPECTED_TYPE)) {
                return scpResponse;
            }
        }
        return null;
    }
}

class WrappedScpResponse {

    String response;
    String hmac;
}

public class ScpResponseParser {

    static ScpResponseDiscover parseDiscoverResponse(String response, List<ScpDevice> devices) {
        return ScpResponseDiscover.fromJson(response, devices);
    }

    static ScpResponseFetchNvcn parseNvcnResponse(String response) {
        return ScpResponseFetchNvcn.fromJson(response);
    }

    static ScpResponseSetPassword parseSetPasswordResponse(String response, String password) {
        return ScpResponseSetPassword.fromJson(response, password);
    }

    static ScpResponseSetWifiConfig parseSetWifiConfigResponse(String response, String password) {
        return ScpResponseSetWifiConfig.fromJson(response, password);
    }

    static ScpResponseRestart parseRestartDeviceResponse(String response, String password) {
        return ScpResponseRestart.fromJson(response, password);
    }

    static ScpResponseResetToDefault parseResetToDefault(String response, String password) {
        return ScpResponseResetToDefault.fromJson(response, password);
    }

    static ScpResponseControl parseControlResponse(String response, String password) {
        return ScpResponseControl.fromJson(response, password);
    }
}
