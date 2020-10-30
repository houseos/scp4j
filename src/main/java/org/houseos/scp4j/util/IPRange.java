/*
 * secure_control_protocol
 * IPRange Class
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2020 Marcel Jaehn
 */
package org.houseos.scp4j.util;

import java.util.ArrayList;
import java.util.List;

public final class IPRange {

    private static final int AMOUNT_OF_OCTETS = 4;
    private static final int BITMASK_ONE_BYTE = 255;
    private static final int SHIFT_3_BYTES = 24; // 3 * 8
    private static final int SHIFT_2_BYTES = 16; // 2 * 8
    private static final int SHIFT_1_BYTE = 8;   // 1 * 8

    private IPRange() {
        //private constructor, because this is a utility class
    }

    private static long[] getOctetsOfIpAddress(String ipAddress) {
        long[] octets = new long[AMOUNT_OF_OCTETS];
        String[] splitResult = ipAddress.split("\\.");
        for (int i = 0; i < splitResult.length; i++) {
            octets[i] = Long.valueOf(splitResult[i]);
        }
        return octets;
    }

    private static long octetsToInteger(long[] octets) {
        long address = 0;
        address += (octets[0] << SHIFT_3_BYTES);
        address += (octets[1] << SHIFT_2_BYTES);
        address += (octets[2] << SHIFT_1_BYTE);
        address += (octets[3]);
        return address;
    }

    private static long[] integerToOctets(long address) {
        long[] octets = new long[AMOUNT_OF_OCTETS];
        octets[0] = (address & (BITMASK_ONE_BYTE << SHIFT_3_BYTES)) >> SHIFT_3_BYTES;
        octets[1] = (address & (BITMASK_ONE_BYTE << SHIFT_2_BYTES)) >> SHIFT_2_BYTES;
        octets[2] = (address & (BITMASK_ONE_BYTE << SHIFT_1_BYTE)) >> SHIFT_1_BYTE;
        octets[3] = (address & (BITMASK_ONE_BYTE));
        return octets;
    }

    private static String octetsToString(long[] octets) {
        return octets[0] + "." + octets[1] + "." + octets[2] + "." + octets[3];
    }

    private static long[] calculateLastIpAddress(long address, long netmask) {
        long[] octets = new long[AMOUNT_OF_OCTETS];

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

        octets[0] = (lastAddress & (BITMASK_ONE_BYTE << SHIFT_3_BYTES)) >> SHIFT_3_BYTES;
        octets[1] = (lastAddress & (BITMASK_ONE_BYTE << SHIFT_2_BYTES)) >> SHIFT_2_BYTES;
        octets[2] = (lastAddress & (BITMASK_ONE_BYTE << SHIFT_1_BYTE)) >> SHIFT_1_BYTE;
        octets[3] = lastAddress & (BITMASK_ONE_BYTE);
        return octets;
    }

    private static long[] calculateNetworkAddress(long address, long netmask) {
        long[] octets = new long[AMOUNT_OF_OCTETS];

        // Get only the network bits set to 1
        long invertor = 0;
        final int maximalExponent = 31;
        for (long i = 0; i < netmask; i++) {
            invertor += Math.pow(2, maximalExponent - i);
        }
        long lastAddress = address & invertor;

        octets[0] = (lastAddress & (BITMASK_ONE_BYTE << SHIFT_3_BYTES)) >> SHIFT_3_BYTES;
        octets[1] = (lastAddress & (BITMASK_ONE_BYTE << SHIFT_2_BYTES)) >> SHIFT_2_BYTES;
        octets[2] = (lastAddress & (BITMASK_ONE_BYTE << SHIFT_1_BYTE)) >> SHIFT_1_BYTE;
        octets[3] = lastAddress & (BITMASK_ONE_BYTE);
        return octets;
    }

    public static List<String> getAllIpAddressesInRange(String networkAddress, long netmask) {
        long[] octets = getOctetsOfIpAddress(networkAddress);
        long address = octetsToInteger(octets);

        List<String> ipAddresses = new ArrayList<>();

        //start with lowest address
        long currentAddress = octetsToInteger(calculateNetworkAddress(address, netmask));
        //increment address using long value
        while (currentAddress < octetsToInteger(calculateLastIpAddress(address, netmask))) {
            currentAddress++;
            ipAddresses.add(octetsToString(integerToOctets(currentAddress)));
        }
        // generate the string representation and store it in list
        return ipAddresses;
    }

    void printBitmask(long address) {
        System.out.println("Bitmask: " + Long.toString(address, 2));
    }
}
