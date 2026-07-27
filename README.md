# BDSLauncher

Linux Minecraft Bedrock Dedicated Server Launcher.

## Requirements

- Java 21 LTS
- Maven
- Linux

## Build

```bash
mvn clean package
```

## Usage

```bash
java -jar BDSLauncher.jar
```

## Features

- MCXboxBroadcast auto-download and management
- BDS auto-download and installation
- Auto-update system
- Pterodactyl compatible
- Foreground process (no daemonize)

## Directory Structure

```
BDSLauncher.jar
data/
├── install.json
├── cache/
├── mcxboxbroadcast/
│   ├── MCXboxBroadcast.jar
│   └── config/
└── bds/
    ├── bedrock_server
    ├── version.txt
    ├── server.properties
    ├── permissions.json
    ├── allowlist.json
    └── worlds/
```

## Metadata Source

Uses [EndstoneMC/bedrock-server-data](https://github.com/EndstoneMC/bedrock-server-data) for BDS version metadata.
