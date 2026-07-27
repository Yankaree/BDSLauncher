# BDSLauncher

Java 21 launcher cho Minecraft Bedrock Dedicated Server trên Linux — thiết kế đặc biệt cho **Pterodactyl Panel** khi server không có Minecraft Bedrock egg.

[![Build](https://github.com/Yankaree/BDSLauncher/actions/workflows/build.yml/badge.svg)](https://github.com/Yankaree/BDSLauncher/actions/workflows/build.yml)

## Vấn đề

Pterodactyl Panel hỗ trợ Minecraft Java Server nhưng **không có egg chính thức cho Bedrock Dedicated Server**. Nếu bạn muốn host BDS trên Pterodactyl, bạn cần một launcher để:

- Tự cài đặt BDS
- Tự cập nhật phiên bản mới
- Quản lý process (start/stop/graceful shutdown)
- Đọc/ghi console trực tiếp từ panel

**BDSLauncher** giải quyết tất cả vấn đề này.

## Cách hoạt động

```
Pterodactyl Panel
       │
       ▼
  BDSLauncher.jar  ──►  Tự download BDS nếu chưa có
       │                Tự cập nhật khi có phiên bản mới
       ▼
  bedrock_server   ──►  Server chạy bình thường
                        Console I/O hoạt động trên panel
```

Bạn chỉ cần đặt `BDSLauncher.jar` vào start command của Pterodactyl:

```
java -Xmx2G -jar BDSLauncher.jar
```

## Tính năng

- **Auto-install BDS** — tải và cài đặt BDS từ [EndstoneMC/bedrock-server-data](https://github.com/EndstoneMC/bedrock-server-data), không cần làm thủ công
- **Auto-update** — kiểm tra phiên bản mới, tự cập nhật hoặc hỏi ý kiến bạn
- **MCXboxBroadcast** — tự tải và quản lý Xbox Live integration
- **Tailscale** — hỗ trợ VPN networking tùy chọn
- **World Pack Sync** — tự tạo `world_behavior_packs.json` / `world_resource_packs.json`
- **Interactive Console** — nhập lệnh trực tiếp vào BDS từ panel
- **Graceful Shutdown** — gửi lệnh `stop`, đợi 30s, sau đó force kill nếu cần
- **Environment Check** — kiểm tra Java 21+, Linux, architecture, disk space

## Pterodactyl Setup

### 1. Tạo Server mới

- **Egg:** Java / Spring Boot (hoặc egg bất kỳ có Java)
- **Docker Image:** `ghcr.io/parkervcp/yolks:java_21`
- **Start Command:** `java -Xmx2G -jar BDSLauncher.jar`

### 2. Upload BDSLauncher.jar

Upload `BDSLauncher.jar` vào `/home/container/` trên server.

### 3. Chạy

Khởi động server. Lần đầu sẽ hiện danh sách versions — chọn version muốn cài. Từ lần sau BDS sẽ tự chạy.

### 4. Directory Structure

```
/home/container/
├── BDSLauncher.jar
└── data/
    ├── install.json
    ├── cache/
    ├── logs/
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

Tất cả config nằm trong `data/install.json`:

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

| Field | Mô tả |
|-------|-------|
| `autoUpdate` | `true` = tự cập nhật BDS khi có phiên bản mới |
| `tailscale.enabled` | `true` = bật Tailscale VPN |
| `tailscale.authKey` | Auth key cho Tailscale (để trống nếu login interactive) |
| `cleanup.deleteMCXboxBroadcastLog` | Xóa log MCXboxBroadcast sau khi tắt |

## Flow

```
Main.main()
├─ EnvironmentCheck       Kiểm tra Java 21+, Linux, arch, disk
├─ InstallStateManager    Load/validate install.json
├─ TailscaleManager       Setup VPN (nếu enabled)
├─ McxboxBroadcastManager Tải MCXboxBroadcast.jar
├─ BdsDownloadManager     Cài BDS lần đầu nếu chưa có
├─ UpdateManager          Kiểm tra và cập nhật BDS
├─ WorldPackManager       Đồng bộ packs
├─ ShutdownHook           Dọn dẹp khi tắt
├─ ProcessManager         Start MCXbox → Start BDS
```

## Build

```bash
# Yêu cầu: Java 21, Maven
mvn clean package
cp target/BDSLauncher-1.0.0.jar BDSLauncher.jar
```

## Metadata Source

Sử dụng [EndstoneMC/bedrock-server-data](https://github.com/EndstoneMC/bedrock-server-data) cho metadata phiên bản BDS.

## License

MIT
