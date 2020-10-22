/*
 * secure_control_protocol
 * Scp Class
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2020 Marcel Jaehn
 */

package org.houseos.scp4j;

import org.houseos.scp4j.util.JsonStorage;
import org.houseos.scp4j.util.IPRange;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Scp {
    private static Scp instance;

    // List of configured devices known to SCP
    List<ScpDevice> knownDevices;

    // List of newly discovered not configured devices
    List<ScpDevice> newDevices;

    public static Scp getInstance() {
        if (Scp.instance == null) {
            Scp.instance = new Scp();
        }
        return Scp.instance;
    }

    private Scp() {
        knownDevices = new ArrayList<>();
    }

    // Initialize knownDevices from JSON
    void knownDevicesFromJson(String json) {
        knownDevices = ScpDevice.devicesfromJson(json);
    }

    void doDiscover(String subnet, String mask) {
        newDevices = new ArrayList<>();
        // Get a list with all relevant IP addresses
        IPRange range = new IPRange(subnet, Integer.parseInt(mask));
        List<String> allIPs = range.getAllIpAddressesInRange();

        final BlockingQueue<SimpleEntry<String,String>> responses = new LinkedBlockingQueue<>();
        final ExecutorService requesterThreadPool = Executors.newFixedThreadPool(100);

        for (String ip : allIPs) {
            Runnable requester = () -> {
                responses.add(new SimpleEntry<>(ip, ScpMessageSender.sendDiscoverHello(ip)));
            };
            requesterThreadPool.submit(requester);
        }
        requesterThreadPool.shutdown();
        while (!requesterThreadPool.isTerminated()) {
            try {
                requesterThreadPool.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
        
        for (SimpleEntry<String,String> response : responses) {
            if (response.getValue() != null) {
                ScpResponseDiscover parsedResponse =
                        ScpResponseParser.parseDiscoverResponse(response.getValue(), null);
                if (parsedResponse != null) {
                    ScpDevice dev = new ScpDevice(
                        parsedResponse.deviceId,
                        parsedResponse.deviceType,
                        response.getKey(),
                        (parsedResponse.currentPasswordNumber == 0),
                        (parsedResponse.currentPasswordNumber == 0 ? "01234567890123456789012345678901" : ""),
                        parsedResponse.currentPasswordNumber);
                    if (dev.isDefaultPasswordSet) {
                        System.out.println("default password set, adding to new devices.");
                        newDevices.add(dev);
                    } else {
                        System.out.println("default password not set.");
                        if (knownDevices.stream().filter(element -> element.deviceId.equals(dev.deviceId)).findAny().isPresent()) {
                            System.out.println("Device ${dev.deviceId} already known.");
                        } else {
                            System.out.println("Device " + dev.deviceId + " not known, adding to known devices.");
                            knownDevices.add(dev);
                        }
                    }
                    System.out.println("Found device: " + dev.toJson());
                }
            }
        }
    }

    // Updates the IP addresses of all devices in the list of known devices
    void doUpdate(String subnet, String mask, String jsonPath) {
        newDevices = new ArrayList<>();
        // Get a list with all relevant IP addresses
        IPRange range = new IPRange(subnet, Integer.parseInt(mask));
        List<String> allIPs = range.getAllIpAddressesInRange();

        final BlockingQueue<SimpleEntry<String,String>> responses = new LinkedBlockingQueue<>();
        final ExecutorService requesterThreadPool = Executors.newFixedThreadPool(100);

        for (String ip : allIPs) {
            Runnable requester = () -> {
                responses.add(new SimpleEntry<>(ip, ScpMessageSender.sendDiscoverHello(ip)));
            };
            requesterThreadPool.submit(requester);
        }
        requesterThreadPool.shutdown();
        while (!requesterThreadPool.isTerminated()) {
            try {
                requesterThreadPool.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
        
        for (SimpleEntry<String,String> response : responses) {
            if (response.getValue() != null) {
                ScpResponseDiscover parsedResponse =
                        ScpResponseParser.parseDiscoverResponse(response.getValue(), knownDevices);
                if (parsedResponse != null) {
                    ScpDevice scpDevice = knownDevices.stream().filter(element -> element.deviceId.equals(parsedResponse.deviceId)).findFirst().orElse(null);
                    if (scpDevice != null) {
                        scpDevice.ipAddress = response.getKey();
                        JsonStorage.storeDevice(scpDevice, jsonPath);
                        System.out.println("Updated IP address of " + parsedResponse.deviceId);
                    }
                }
            }
        }
    }

    void doDiscoverThenDoProvisioning(String subnet, String mask, String ssid, String wifiPassword, String jsonPath) {
        newDevices = new ArrayList<>();
        // Get a list with all relevant IP addresses
        IPRange range = new IPRange(subnet, Integer.parseInt(mask));
        List<String> allIPs = range.getAllIpAddressesInRange();

        final BlockingQueue<SimpleEntry<String,String>> responses = new LinkedBlockingQueue<>();
        final ExecutorService requesterThreadPool = Executors.newFixedThreadPool(100);

        for (String ip : allIPs) {
            Runnable requester = () -> {
                responses.add(new SimpleEntry<>(ip, ScpMessageSender.sendDiscoverHello(ip)));
            };
            requesterThreadPool.submit(requester);
        }
        requesterThreadPool.shutdown();
        while (!requesterThreadPool.isTerminated()) {
            try {
                requesterThreadPool.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        for (SimpleEntry<String,String> response : responses) {
            if (response.getValue() != null) {
                System.out.println("Received discover response.");
                ScpResponseDiscover parsedResponse =
                    ScpResponseParser.parseDiscoverResponse(response.getValue(), null);
                if (parsedResponse != null) {
                    // create device
                    ScpDevice dev = new ScpDevice(
                        parsedResponse.deviceId,
                        parsedResponse.deviceType,
                        response.getKey(),
                        (parsedResponse.currentPasswordNumber == 0),
                        (parsedResponse.currentPasswordNumber == 0 ? "01234567890123456789012345678901" : ""),
                        parsedResponse.currentPasswordNumber);
                    System.out.println("Found device: " + dev.toString());

                    if (dev.isDefaultPasswordSet) {
                        System.out.println("default password set, adding to new devices.");
                        newDevices.add(dev);
                    } else {
                        System.out.println("default password not set.");
                        if (knownDevices.stream().filter(element -> element.deviceId.equals(dev.deviceId)).findAny().isPresent()) {
                            System.out.println("Device " + dev.deviceId + " already known.");
                        } else {
                            System.out.println("Device " + dev.deviceId + " not known, adding to known devices.");
                            knownDevices.add(dev);
                        }
                    }
                    doProvisioning(dev, ssid, wifiPassword, jsonPath);
                }
            }
        }
    }

    void doProvisioning(ScpDevice device, String ssid, String wifiPassword, String jsonPath) {
        if (ssid == null ||
                ssid.isEmpty() ||
                wifiPassword == null ||
                wifiPassword.isEmpty()) {
            System.out.println("provisioning without ssid or wifiPassword not possible.");
            return;
        }

        // for each new device
        System.out.println("Provisioning device: " + device.deviceId);
        // send security-pw-change
        ScpMessageSender.sendNewPassword(device);

        // send security-wifi-config
        String wifiConfigResponse = ScpMessageSender.sendWifiConfig(device, ssid, wifiPassword);
        // send security-restart
        if (wifiConfigResponse == null) {
            System.out.println("wifiConfig response is null, shutting down.");
            return;
        } else if (wifiConfigResponse.equals(ScpStatus.RESULT_ERROR)) {
            System.out.println("failed to set wifi config.");
            return;
        }
        String restartResponse = ScpMessageSender.sendRestart(device);
        // move device from new devices to known devices
        if (restartResponse != null) {
            System.out.println("Restarting device successfull, removing from new devices and adding to known devices.");
            this.knownDevices.add(device);
            //add to List, remove if it already exists to mitigate duplicates
            this.newDevices.removeIf(element -> element.deviceId.equals(device.deviceId));
            //print all device info
            System.out.println(device.toString());
            JsonStorage.storeDevice(device, jsonPath);
        }
    }

    void control(String deviceId, String command) {
        System.out.println("do control for device: " + deviceId);
        ScpDevice scpDevice = knownDevices.stream().filter(element -> element.deviceId.equals(deviceId)).findFirst().orElse(null);
        if (scpDevice != null) {
            String controlResponse = ScpMessageSender.sendControl(scpDevice, command);
            System.out.println(controlResponse);
            if (controlResponse != null && controlResponse.equals(ScpStatus.RESULT_SUCCESS)) {
                System.out.println("Successfully send cotrol " + command + " to " + deviceId);
            } else {
                System.out.println("Failed to send control " + command + " to " + deviceId);
            }
        }
    }

    void resetToDefault(String deviceId) {
        System.out.println("do control for device: " + deviceId);
        ScpDevice scpDevice = knownDevices.stream().filter(element -> element.deviceId.equals(deviceId)).findFirst().orElse(null);
        if (scpDevice != null) {
            String resetToDefaultResponse = ScpMessageSender.sendResetToDefault(scpDevice);
            System.out.println(resetToDefaultResponse);
            if (resetToDefaultResponse != null && resetToDefaultResponse.equals(ScpStatus.RESULT_SUCCESS)) {
                System.out.println("Successfully send reset to default to " + deviceId);
            } else {
                System.out.println("Failed to send reset to default to " + deviceId);
            }
        }
    }
}
