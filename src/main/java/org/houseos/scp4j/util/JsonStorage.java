/*
 * secure_control_protocol
 * JsonStorage Class
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2020 Marcel Jaehn
 */
package org.houseos.scp4j.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.houseos.scp4j.ScpDevice;

public final class JsonStorage {

    private JsonStorage() {
        //private constructor, because this is a utility class
    }

    public static void storeDevice(ScpDevice device, String path) {
        try {
            //read file
            String json = readFile(path, StandardCharsets.UTF_8);
            Gson g = new Gson();
            Type listType = new TypeToken<ArrayList<ScpDevice>>() {
                //no implementation
            }.getType();
            List<ScpDevice> devices = new Gson().fromJson(json, listType);
            //add to List, remove if it already exists to mitigate duplicates
            devices.removeIf(element -> element.getDeviceId().equals(device.getDeviceId()));
            devices.add(device);
            System.out.println(devices);
            //write the file and list to JSON
            FileWriter fw = new FileWriter(path);
            g.toJson(devices, fw);
            fw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
