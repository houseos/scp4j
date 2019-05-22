package de.delusionsoftware.shuttercontroller;

import de.delusionsoftware.shuttercontroller.network.shutterControlProtocol.ShutterControlProtocol;

public final class MainClass {

	public static void main(final String[] args) {
		System.out.println("Hallo!!!!");
		final ShutterControlProtocol cp = new ShutterControlProtocol();
		for (int i = 1; i < 254; i++) {
			final String string = "192.168.99." + i;
			new Thread(() -> cp.sendDiscoverHello(string)).start();
		}
		while (true) {
			System.out.println(cp.getResponse());
		}
	}
}
