/*
shutter-controller-android
The app that allows you to control your shutter-controller devices.

SPDX-License-Identifier: GPL-3.0-or-later

Copyright (C) 2018 Benjamin Schilling
*/

package de.delusionsoftware.shuttercontroller.network.shutterControlProtocol;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import de.delusionsoftware.shuttercontroller.device.Device;

public class ShutterControlProtocol {

	private final BlockingQueue<Device> responses = new ArrayBlockingQueue<>(4096);

	public Device getResponse() {
		try {
			return responses.take();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void sendDiscoverHello(final String ipAddress) {
		final Device dev = Device.sendDiscoverHello(ipAddress);
		if (dev != null) {
			responses.offer(dev);
		}
	}
}
