# SamsungAC Binding

This binding born from the ash of the old SamsungAC binding of the openhab-addons (v1) repository

***Note:*** enable TLSv1 by placing it in the /etc/java-21-openjdk/security/java.security file under the line jdk.tls.legacyAlgorithms.
A convenient way to do this is to use the following command from your OH instance shell:
```bash
sudo sed -i '/^jdk\.tls\.legacyAlgorithms/ {/TLSv1/! s/$/,TLSv1/}' /etc/java-21-openjdk/security/java.security
```


This binding provides connectivity for Samsung AC devices made before SmartThing was a thing.

The communication is done via the WiFi interface using a SSL (TLSv1) socket on port 2878/tcp

Data is exchanged in XML format and can be humanely read and interpreted.


## Supported Things

The only supported thing for this binding is Samsung AC devices with a WiFi interface that expose the port 2878/tcp.

## Discovery

Autodiscovery is supported through a background scanning process. Units will be added automatically in the discovered devices list following the mailbox principles

## Thing Configuration

Thing can be configured using the web interface or the configuration file. 
Using the web interface help with token acquisition.

### Configuration parameters

| Name     | Type | Description                                                       | Default | Required | Advanced |
|----------|------|-------------------------------------------------------------------|---------|----------|----------|
| hostname | text | Hostname or IP address of the device                              | N/A     | yes      | no       |
| UID      | text | Unique ID for the device (MAC Address)                            | N/A     | yes      | no       |
| token    | text | Token for authenticating to the device, refer to the next section | N/A     | no       | no       |

## Token
If you already have a token for the device you can simply paste in the token field.

If, instead, you want to acquire a token, you can do so by using the Get Token channel:
1. Link the Get Token channel to an item of type switch
2. Turn on the switch
3. Power On the AC Unit with the IR Remote
4. The token will be sent to the thing, the item will turn off and the thing will go offline
5. Disable the thing
6. Enable the thing again

## Channels

Actual features supported in the binding are exposed as channels.

| Channel              | Type   | Read/Write | Description                                                                                                              |
|----------------------|--------|------------|--------------------------------------------------------------------------------------------------------------------------|
| Power                | Switch | RW         | This turn on/off the AC unit                                                                                             |
| Get Token            | Switch | RW         | Start the token acquisition procedure                                                                                    |
| Internet Connected   | Switch | R          | This channel is updated when the device is connected to the internet                                                     |
| Operation mode       | Text   | RW         | Set the mode of the AC unit (Cool, Dry, Wind, Heat, Auto)                                                                |
| Set Temperature      | Number | RW         | Set the temperature of the AC unit                                                                                       |
| Measured temeprature | Number | R          | Measured temperature of the AC unit                                                                                      |
| Fan Speed            | String | RW         | Set the fan speed of the AC unit                                                                                         |
| Vane vertical        | String | RW         | Set the vane swing feature (Swing or fixed)                                                                              |
| Virus Doctor         | Switch | RW         | Enable/Disable the virus doctor feature                                                                                  |
| Auto clean           | Switch | RW         | Enable/Disable the auto clean feature                                                                                    |
| Cooling mode         | String | RW         | Set the cooling (maybe convienient?) mode for the AC Unit (Off, Quiet, Sleep, Smart, SoftCool, Turbo, WindMode1, 2 and 3 |




## Full Example

_Provide a full usage example based on textual configuration files._
_*.things, *.items examples are mandatory as textual configuration is well used by many users._
_*.sitemap examples are optional._

### Thing Configuration

```java

```

### Item Configuration

```java

```

### Sitemap Configuration

```perl
Optional Sitemap configuration goes here.
Remove this section, if not needed.
```

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
