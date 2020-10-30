/*
 * secure_control_protocol
 * Main Class
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2020 Marcel Jaehn
 */
package org.houseos.scp4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.houseos.scp4j.util.JsonStorage;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "scp4j", description = "Secure Control Protocol CLI Client",
        subcommands = {
            ControlCommand.class,
            DiscoverCommand.class,
            ProvisionCommand.class,
            ResetCommand.class,
            UpdateCommand.class
        }
)
public class ScpClient implements Runnable {

    @Option(names = {"-h", "--help"}, description = "Print this usage information.", usageHelp = true)
    boolean usageHelpRequested;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new CommandLine(new ScpClient()).execute(args);
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}

@Command(name = "discover", description = "Discover all devices in a given IP range.")
class DiscoverCommand implements Runnable {

    @Option(names = {"-h", "--help"}, description = "Print this usage information.", usageHelp = true)
    boolean usageHelpRequested;

    @Option(names = {"-i", "--ipaddress"}, required = true, description = "IP address from the subnet to be scanned.")
    private String ipAddress;

    @Option(names = {"-m", "--mask"}, required = true, description = "The subnet mask of the network to scan.")
    private String mask;

    @Override
    public void run() {
        if (usageHelpRequested) {
            CommandLine.usage(this, System.out);
        } else {
            System.out.println("scp_client Discover");
            Scp scp = Scp.getInstance();
            scp.doDiscover(ipAddress, mask);
            System.out.println(scp.newDevices);
        }
    }
}

@Command(name = "provision", description = "Provision all available devices.")
class ProvisionCommand implements Runnable {

    @Option(names = {"-h", "--help"}, description = "Print this usage information.", usageHelp = true)
    boolean usageHelpRequested;

    @Option(names = {"-i", "--ipaddress"}, required = true, description = "IP address from the subnet to be scanned.")
    private String ipAddress;

    @Option(names = {"-m", "--mask"}, required = true, description = "The subnet mask of the network to scan.")
    private String mask;

    @Option(names = {"-s", "--ssid"}, required = true,
            description = "The SSID of the Wifi the device should connect to.")
    private String ssid;

    @Option(names = {"-p", "--password"}, required = true, description = "The Wifi password.")
    private String password;

    @Option(names = {"-j", "--json"}, required = true, description = "Export the provisioned devices to the given "
            + "JSON file to be able to load them for the next command.")
    private String jsonPath;

    @Override
    public void run() {
        if (usageHelpRequested) {
            CommandLine.usage(this, System.out);
        } else {
            System.out.println("scp_client Provision");
            Scp scp = Scp.getInstance();
            scp.doDiscoverThenDoProvisioning(ipAddress, mask, ssid, password, jsonPath);
        }
    }
}

@Command(name = "reset", description = "Reset the selected device.")
class ResetCommand implements Runnable {

    @Option(names = {"-h", "--help"}, description = "Print this usage information.", usageHelp = true)
    boolean usageHelpRequested;

    @Option(names = {"-d", "--deviceId"}, required = true, description = "The ID of the device to control.")
    private String deviceId;

    @Option(names = {"-j", "--json"}, required = true,
            description = "Path to the JSON file containing all known devices.")
    private String jsonPath;

    @Override
    public void run() {
        if (usageHelpRequested) {
            CommandLine.usage(this, System.out);
        } else {
            try {
                System.out.println("scp_client reset");
                Scp scp = Scp.getInstance();
                String json = JsonStorage.readFile(jsonPath, StandardCharsets.UTF_8);
                scp.knownDevicesFromJson(json);
                scp.resetToDefault(deviceId);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

@Command(name = "control", description = "Control the selected device.")
class ControlCommand implements Runnable {

    @Option(names = {"-h", "--help"}, description = "Print this usage information.", usageHelp = true)
    boolean usageHelpRequested;

    @Option(names = {"-c", "--command"}, required = true, description = "The command to send to the device.")
    private String command;

    @Option(names = {"-d", "--deviceId"}, required = true, description = "The ID of the device to control.")
    private String deviceId;

    @Option(names = {"-j", "--json"}, required = true,
            description = "Path to the JSON file containing all known devices.")
    private String jsonPath;

    @Override
    public void run() {
        if (usageHelpRequested) {
            CommandLine.usage(this, System.out);
        } else {
            try {
                System.out.println("scp_client control");
                Scp scp = Scp.getInstance();
                String json = JsonStorage.readFile(jsonPath, StandardCharsets.UTF_8);
                scp.knownDevicesFromJson(json);
                scp.control(deviceId, command);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

@Command(name = "update", description = "Update the IP addresses of all devices in a given IP range.")
class UpdateCommand implements Runnable {

    @Option(names = {"-h", "--help"}, description = "Print this usage information.", usageHelp = true)
    boolean usageHelpRequested;

    @Option(names = {"-i", "--ipaddress"}, required = true, description = "IP address from the subnet to be scanned.")
    private String ipAddress;

    @Option(names = {"-m", "--mask"}, required = true, description = "The subnet mask of the network to scan.")
    private String mask;

    @Option(names = {"-j", "--json"}, required = true,
            description = "Path to the JSON file containing all known devices.")
    private String jsonPath;

    @Override
    public void run() {
        if (usageHelpRequested) {
            CommandLine.usage(this, System.out);
        } else {
            try {
                System.out.println("scp_client update");
                Scp scp = Scp.getInstance();
                String json = JsonStorage.readFile(jsonPath,
                        StandardCharsets.UTF_8);
                scp.knownDevicesFromJson(json);
                scp.doUpdate(ipAddress, mask, jsonPath);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
