package de.delusionsoftware.shuttercontroller;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.stream.Stream;

import de.delusionsoftware.shuttercontroller.device.Device;
import de.delusionsoftware.shuttercontroller.helper.Tools;
import de.delusionsoftware.shuttercontroller.network.shutterControlProtocol.PossibleCommands;
import de.delusionsoftware.shuttercontroller.network.shutterControlProtocol.ShutterControlProtocol;

public final class MainClass {

	private static String getIPString(final long ipNumber) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 3; i >= 0; --i) {
			sb.append(((ipNumber >> (8 * i)) & 255));
			if (i > 0) {
				sb.append(".");
			}
		}
		return sb.toString();
	}

	private static long getNumber(final byte[] address) {
		long ret = 0;
		for (final byte b : address) {
			ret = (ret << 8) | (b & 255l);
		}
		return ret;
	}

	private static long getOnes(final int networkPrefixLength) {
		final long ones = (1l << (networkPrefixLength)) - 1;
		return ones;
	}

	private static long getOnes32bits(final int networkPrefixLength) {

		return MainClass.getOnes(networkPrefixLength) << (32 - networkPrefixLength);
	}

	public static void main(final String[] args) throws SocketException {

		if (args.length < 1) {
			MainClass.printHelp();
			return;
		}
		int nicIdx = -1;
		try {
			nicIdx = Integer.parseInt(args[0]);
		} catch (final NumberFormatException nfe) {
			System.err.println("Supplied argument is no valid number");
			nfe.printStackTrace();
			return;
		}
		final NetworkInterface nic = NetworkInterface.getByIndex(nicIdx);
		final InterfaceAddress ifa = nic.getInterfaceAddresses().get(0);
		System.out.println("IP:     " + ifa.getAddress().getHostAddress());
		final short npl = ifa.getNetworkPrefixLength();
		final long ipAddrLow = (MainClass.getNumber(ifa.getAddress().getAddress())) & MainClass.getOnes32bits(npl);
		final long ipAddrHigh = ipAddrLow | MainClass.getOnes(32 - npl);

		System.out.println("Trying to reach scp instances!");
		final ShutterControlProtocol cp = new ShutterControlProtocol();
		for (long i = ipAddrLow + 1; i < ipAddrHigh; i++) {
			final String string = MainClass.getIPString(i);
			new Thread(() -> cp.sendDiscoverHello(string)).start();
		}
		try {
			Thread.sleep(2000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		final long startTime = System.currentTimeMillis();
		while (true) {
			final Device response = cp.getResponse();
			if (response != null) {
				System.out.println("Sending Stop Command to: " + response.getIpAddress());
				response.sendSecurityCommand(PossibleCommands.DIRECTION_STOP);
				System.out.println(response);
				System.out.println();
				System.out.println();
				response.sendSecurityCommand(PossibleCommands.SET_PWD);
				System.out.println(response);
				System.out.println();

				System.out.println();

				System.out.println();

			} else {
				if ((System.currentTimeMillis() - startTime) > 20000) {
					break;
				}
			}
		}
	}

	private static void printHelp() throws SocketException {
		System.err.println("No interface index supplied!");
		System.err.println("Use on of the following NIC numbers:");
		final Stream<NetworkInterface> nics = NetworkInterface.networkInterfaces();
		nics.forEach(t -> {
			try {
				if (t.isUp() && !t.isVirtual() && !t.isLoopback()) {
					System.err.println("  NIC: " + t.getIndex() + " Display Name: " + t.getDisplayName() + "MAC: "
							+ Tools.toHex(t.getHardwareAddress()));
					t.inetAddresses().forEach(t1 -> {
						System.err.println("     Host Address: " + t1.getHostAddress());
					});
				}
			} catch (final SocketException e) {
				e.printStackTrace();
			}
		});
	}
}
