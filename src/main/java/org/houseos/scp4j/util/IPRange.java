/*
 * secure_control_protocol
 * IPRange Class
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2020 Marcel Jaehn
 */

package org.houseos.scp4j.util;

import java.util.ArrayList;
import java.util.List;

public class IPRange {
    long netmask;
    long address = 0;

    public IPRange(String networkAddress, long netmask) {
        long[] octets = getOctetsOfIpAddress(networkAddress);
        this.address = octetsToInteger(octets);
        this.netmask = netmask;
    }

    long[] getOctetsOfIpAddress(String ipAddress) {
        long[] octets = new long[4];
        String[] splitResult = ipAddress.split("\\.");
        for (int i = 0; i<splitResult.length; i++) {
            octets[i] = Long.valueOf(splitResult[i]);
        }
        return octets;
    }

    static long octetsToInteger(long[] octets) {
        long address = 0;
        address += (octets[0] << 24);
        address += (octets[1] << 16);
        address += (octets[2] << 8);
        address += (octets[3]);
        return address;
    }

    static long[] longegerToOctets(long address) {
        long[] octets = new long[4];
        octets[0] = (address & (255 << 24)) >> 24;
        octets[1] = (address & (255 << 16)) >> 16;
        octets[2] = (address & (255 << 8)) >> 8;
        octets[3] = (address & (255));
        return octets;
    }

    static String octetsToString(long[] octets) {
        return octets[0] + "." + octets[1] + "." + octets[2] + "." + octets[3];
    }

    long[] calculateLastIpAddress() {
        long[] octets = new long[4];

        // Determine host bits
        long hostbits = 32 - netmask;
        // Set all host bits to 1
        long invertor = 0;
        for (long i = 0; i < hostbits; i++) {
          invertor += Math.pow(2, i);
        }
        long lastAddress = address | invertor;
        //substract 1 to get last address instead of broadcast address
        lastAddress--;

        octets[0] = (lastAddress & (255 << 24)) >> 24;
        octets[1] = (lastAddress & (255 << 16)) >> 16;
        octets[2] = (lastAddress & (255 << 8)) >> 8;
        octets[3] = lastAddress & (255);
        return octets;
    }

    long[] calculateNetworkAddress() {
        long[] octets = new long[4];

        // Get only the network bits set to 1
        long invertor = 0;
        for (long i = 0; i < netmask; i++) {
          invertor += Math.pow(2, 31 - i);
        }
        long lastAddress = address & invertor;

        octets[0] = (lastAddress & (255 << 24)) >> 24;
        octets[1] = (lastAddress & (255 << 16)) >> 16;
        octets[2] = (lastAddress & (255 << 8)) >> 8;
        octets[3] = lastAddress & (255);
        return octets;
    }

    public List<String> getAllIpAddressesInRange() {
        List<String> ipAddresses = new ArrayList<>();

        //start with lowest address
        long currentAddress = octetsToInteger(calculateNetworkAddress());
        //increment address using long value
        while (currentAddress < octetsToInteger(calculateLastIpAddress())) {
            currentAddress++;
            ipAddresses.add(octetsToString(longegerToOctets(currentAddress)));
        }
        // generate the string representation and store it in list
        return ipAddresses;
    }

    void prlongBitmask(long address) {
        System.out.println("Bitmask: " + Long.toString(address, 2));
    }
}
