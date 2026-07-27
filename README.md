# BDSLauncher

Java 21 launcher cho Minecraft Bedrock Dedicated Server trên Linux — thiết kế cho **Pterodactyl Panel** khi không có Minecraft Bedrock egg.

[![Build](https://github.com/Yankaree/BDSLauncher/actions/workflows/build.yml/badge.svg)](https://github.com/Yankaree/BDSLauncher/actions/workflows/build.yml)

## Vấn đề

Pterodactyl hỗ trợ Minecraft Java nhưng **không có egg cho Bedrock Dedicated Server**. BDSLauncher giải quyết vấn đề này — upload JAR, bấm Start, server chạy.

## Cách dùng trên Pterodactyl

1. Tạo server mới trên Pterodactyl (dùng egg Java / Spring Boot)
2. Upload `BDSLauncher.jar` vào server
3. Bấm **Start**

Lần đầu BDSLauncher sẽ hỏi bạn chọn version BDS muốn cài. Từ lần sau server tự chạy, không cần làm gì thêm.

## Tính năng

- **Auto-install BDS** — tự tải và cài đặt, không cần thao tác thủ công
- **Auto-update** — tự cập nhật khi có phiên bản mới
- **MCXboxBroadcast** — tự tải Xbox Live integration
- **Tailscale** — VPN networking tùy chọn
- **World Pack Sync** — tự tạo file pack JSON
- **Console** — nhập lệnh trực tiếp từ panel
- **Graceful Shutdown** — stop đúng cách, không crash

## Cấu trúc thư mục

```
/home/container/
├── BDSLauncher.jar
└── data/
    ├── install.json
    ├── bds/
    │   ├── bedrock_server
    │   ├── server.properties
    │   └── worlds/
    └── mcxboxbroadcast/
        └── MCXboxBroadcast.jar
```

## Cấu hình

Config nằm trong `data/install.json`:

```json
{
  "autoUpdate": false,
  "bds": {
    "installed": false,
    "version": null
  },
  "tailscale": {
    "enabled": false,
    "authKey": null
  }
}
```

| Field | Mô tả |
|-------|-------|
| `autoUpdate` | Tự cập nhật BDS khi có bản mới |
| `tailscale.enabled` | Bật Tailscale VPN |
| `tailscale.authKey` | Auth key (để trống nếu login interactive) |

## Flow

```
Main.main()
├─ EnvironmentCheck       Kiểm tra Java 21+, Linux, disk
├─ InstallStateManager    Load install.json
├─ TailscaleManager       Setup VPN (nếu enabled)
├─ McxboxBroadcastManager Tải MCXboxBroadcast
├─ BdsDownloadManager     Cài BDS nếu chưa có
├─ UpdateManager          Kiểm tra cập nhật
├─ WorldPackManager       Đồng bộ packs
├─ ProcessManager         Start MCXbox → Start BDS
```

## Build

```bash
mvn clean package
cp target/BDSLauncher-1.0.0.jar BDSLauncher.jar
```

## License

MIT
