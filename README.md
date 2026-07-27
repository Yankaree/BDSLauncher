# BDSLauncher

Linux Minecraft Bedrock Dedicated Server Launcher — Java 21 CLI.

[![Build](https://github.com/Yankaree/BDSLauncher/actions/workflows/build.yml/badge.svg)](https://github.com/Yankaree/BDSLauncher/actions/workflows/build.yml)

## Requirements

- Java 21 LTS
- Maven
- Linux (amd64 / aarch64)

## Build

```bash
mvn clean package
```

## Usage

```bash
java -jar BDSLauncher.jar
```

## Features

- **Environment Check** — validates Java 21+, Linux OS, architecture, disk space
- **BDS Auto-Install** — download & install BDS from [EndstoneMC/bedrock-server-data](https://github.com/EndstoneMC/bedrock-server-data)
- **Auto-Update** — check for new BDS versions, auto or prompt
- **MCXboxBroadcast** — auto-download & manage Xbox live integration
- **Tailscale** — optional VPN networking via Tailscale
- **World Pack Sync** — auto-generate `world_behavior_packs.json` / `world_resource_packs.json`
- **Interactive Console** — type commands directly into BDS
- **Graceful Shutdown** — proper `stop` signal with timeout, then force kill

## Directory Structure

```
BDSLauncher.jar
data/
├── install.json
├── cache/
├── logs/
├── tailscale/
├── mcxboxbroadcast/
│   ├── MCXboxBroadcast.jar
│   └── config/
└── bds/
    ├── bedrock_server
    ├── server.properties
    ├── permissions.json
    ├── allowlist.json
    └── worlds/
```

## Configuration

All configuration is stored in `data/install.json`:

```json
{
  "launcherVersion": "1.0.0",
  "autoUpdate": false,
  "bds": {
    "installed": false,
    "version": null,
    "platform": "linux"
  },
  "mcxboxbroadcast": {
    "installed": false,
    "version": "latest"
  },
  "tailscale": {
    "enabled": false,
    "version": "1.101.162",
    "authKey": null,
    "userspace": true
  },
  "cleanup": {
    "deleteMCXboxBroadcastLog": true
  }
}
```

## Flow

```
Main.main()
├─ EnvironmentCheck      (Java, Linux, Arch, Disk)
├─ InstallStateManager   (load/validate install.json)
├─ TailscaleManager      (optional VPN setup)
├─ McxboxBroadcastManager (download JAR)
├─ BdsDownloadManager    (first install if needed)
├─ UpdateManager         (check & update BDS)
├─ WorldPackManager      (sync packs)
├─ ShutdownHook          (cleanup on exit)
├─ ProcessManager        (start MCXbox → start BDS)
```

## Metadata Source

Uses [EndstoneMC/bedrock-server-data](https://github.com/EndstoneMC/bedrock-server-data) for BDS version metadata.

## License

MIT
