/*
 * secure_control_protocol
 * ScpDevice Class
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2020 Marcel Jaehn
 */
package org.houseos.scp4j;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class ScpDevice {

    String deviceType;
    public String deviceId;
    List<ScpDeviceAction> actions;

    String ipAddress;
    boolean isDefaultPasswordSet;
    String knownPassword;
    int currentPasswordNumber;

    ScpDevice(String deviceId,
            String deviceType,
            String ipAddress,
            boolean isDefaultPasswordSet,
            String knownPassword,
            int currentPasswordNumber) {
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.ipAddress = ipAddress;
        this.isDefaultPasswordSet = isDefaultPasswordSet;
        this.knownPassword = knownPassword;
        this.currentPasswordNumber = currentPasswordNumber;

        this.actions = new ArrayList<>();
        if (this.deviceType.equals(ScpDeviceTypes.SHUTTER_CONTROL)) {
            this.actions.add(new ScpDeviceAction("Open", "up"));
            this.actions.add(new ScpDeviceAction("Close", "down"));
            this.actions.add(new ScpDeviceAction("Stop", "stop"));
        }
    }

    static List<ScpDevice> devicesfromJson(String json) {
        Gson g = new Gson();
        Type listType = new TypeToken<ArrayList<ScpDevice>>() {
        }.getType();
        List<ScpDevice> devices = new Gson().fromJson(json, listType);
        return devices;
    }

    @Override
    public String toString() {
        return "ScpDevice:\n Type: " + deviceType + "\n ID: " + deviceId + "\n IP: " + ipAddress + "\n default password: " + isDefaultPasswordSet + "\n password: " + knownPassword + "\n current password number: " + currentPasswordNumber;
    }

    public String toJson() {
        return "{\"deviceType\":\"" + deviceType + "\",\"deviceId\":\"" + deviceId + "\",\"ipAddress\":\"" + ipAddress + "\",\"isDefaultPasswordSet\":\"" + isDefaultPasswordSet + "\",\"knownPassword\":\"" + knownPassword + "\",\"currentPasswordNumber\":\"" + currentPasswordNumber + "\"}";
    }
}

class ScpDeviceTypes {

    private ScpDeviceTypes() {
        // this class has no accessible methods
    }

    static final String SHUTTER_CONTROL = "shutter-control";
}

class ScpDeviceAction {

    final String name;
    final String action;

    ScpDeviceAction(String name, String action) {
        this.name = name;
        this.action = action;
    }
}
