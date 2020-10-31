# Secure Control Protocol Client Library written in Java

## Dependencies

- [Java Development Kit](https://openjdk.java.net/ "OpenJDK") 1.8
- [Maven](https://maven.apache.org/ "Apache Maven") >= 3.6

## Build

To build the project navigate to its root folder and run:  
`mvn package`

## Run

Run the program with:  
`java -jar target/scp4j-0.0.1-SNAPSHOT-jar-with-dependencies.jar`

## CLI Client

``` lang-none
Usage: scp4j [-h] [COMMAND]
Secure Control Protocol CLI Client
  -h, --help   Print this usage information.
Commands:
  control    Control the selected device.
  discover   Discover all devices in a given IP range.
  provision  Provision all available devices.
  reset      Reset the selected device.
  update     Update the IP addresses of all devices in a given IP range.
```

### control

`java -jar target/scp4j-0.0.1-SNAPSHOT-jar-with-dependencies.jar control`
``` lang-none
Usage: scp4j control [-h] -c=<command> -d=<deviceId> -j=<jsonPath>
Control the selected device.
  -c, --command=<command>   The command to send to the device.
  -d, --deviceId=<deviceId> The ID of the device to control.
  -h, --help                Print this usage information.
  -j, --json=<jsonPath>     Path to the JSON file containing all known devices.
```

### discover

`java -jar target/scp4j-0.0.1-SNAPSHOT-jar-with-dependencies.jar discover`
``` lang-none
Usage: scp4j discover [-h] -i=<ipAddress> -m=<mask>
Discover all devices in a given IP range.
  -h, --help          Print this usage information.
  -i, --ipaddress=<ipAddress>
                      IP address from the subnet to be scanned.
  -m, --mask=<mask>   The subnet mask of the network to scan.
```

### provision

`java -jar target/scp4j-0.0.1-SNAPSHOT-jar-with-dependencies.jar provision`
``` lang-none
Usage: scp4j provision [-h] -i=<ipAddress> -j=<jsonPath> -m=<mask>
                       -p=<password> -s=<ssid>
Provision all available devices.
  -h, --help              Print this usage information.
  -i, --ipaddress=<ipAddress>
                          IP address from the subnet to be scanned.
  -j, --json=<jsonPath>   Export the provisioned devices to the given JSON file
                            to be able to load them for the next command.
  -m, --mask=<mask>       The subnet mask of the network to scan.
  -p, --password=<password>
                          The Wifi password.
  -s, --ssid=<ssid>       The SSID of the Wifi the device should connect to.
```

### reset

`java -jar target/scp4j-0.0.1-SNAPSHOT-jar-with-dependencies.jar reset`
``` lang-none
Usage: scp4j reset [-h] -d=<deviceId> -j=<jsonPath>
Reset the selected device.
  -d, --deviceId=<deviceId>
                          The ID of the device to control.
  -h, --help              Print this usage information.
  -j, --json=<jsonPath>   Path to the JSON file containing all known devices.
```

### update

`java -jar target/scp4j-0.0.1-SNAPSHOT-jar-with-dependencies.jar update`
``` lang-none
Usage: scp4j update [-h] -i=<ipAddress> -j=<jsonPath> -m=<mask>
Update the IP addresses of all devices in a given IP range.
  -h, --help              Print this usage information.
  -i, --ipaddress=<ipAddress>
                          IP address from the subnet to be scanned.
  -j, --json=<jsonPath>   Path to the JSON file containing all known devices.
  -m, --mask=<mask>       The subnet mask of the network to scan.
```

## License
SPDX-License-Identifier: GPL-3.0-only

The full version of the license can be found in LICENSE.