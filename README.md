# BDSLauncher

Java 21 launcher cho Minecraft Bedrock Dedicated Server trên Linux — thiết kế cho **Pterodactyl Panel** (dùng Java egg thay vì Bedrock egg).

## Cách dùng

1. Upload `BDSLauncher.jar` lên Pterodactyl server (dùng Java egg)
2. Bấm **Start** — lần đầu sẽ hỏi chọn version BDS muốn cài
3. Các lần sau tự động chạy, không cần thao tác

## Tính năng

- Zero dependencies — JAR chỉ **59 KB**, không cần thêm thư viện
- Auto-install BDS + auto-update
- MCXboxBroadcast tích hợp Xbox Live
- Tailscale VPN tùy chọn
- World Pack Sync (đồng bộ behavior/resource packs)
- Graceful Shutdown (stop BDS đúng cách, không crash)

## Cấu trúc

```
/home/container/
├── BDSLauncher.jar
└── data/
    ├── config.txt            ← Cấu hình (INI format)
    ├── bds/
    │   ├── bedrock_server
    │   ├── server.properties
    │   └── worlds/
    └── mcxboxbroadcast/
        └── MCXboxBroadcast.jar
```

## Cấu hình (`data/config.txt`)

```ini
launcherVersion = 1.0.0
autoUpdate = true

[bds]
installed = true
version = 1.26.34

[mcxboxbroadcast]
installed = true
version = latest

[tailscale]
enabled = false
version = 1.101.162
authKey =

[cleanup]
deleteMCXboxBroadcastLog = true
```

| Key | Mô tả |
|-----|-------|
| `autoUpdate` | Tự cập nhật BDS khi có bản mới |
| `tailscale.enabled` | Bật/tắt Tailscale VPN |
| `tailscale.authKey` | Auth key (để trống nếu login interactive) |
| `cleanup.deleteMCXboxBroadcastLog` | Xoá log MCXbox sau khi tắt |

## Flow

```
Main.main()
├─ EnvironmentCheck        Kiểm tra Java 21+, Linux, disk
├─ InstallStateManager     Đọc data/config.txt
├─ TailscaleManager        Setup VPN (nếu enabled)
├─ McxboxBroadcastManager  Tải MCXboxBroadcast
├─ BdsDownloadManager      Cài BDS nếu chưa có
├─ UpdateManager           Kiểm tra cập nhật
├─ WorldPackManager        Đồng bộ packs
├─ ProcessManager          Start MCXbox → Start BDS
└─ Shutdown Hook           Stop BDS → Stop MCXbox → Stop Tailscale
```

## Build

```bash
export JAVA_HOME=.tools/jdk-21.0.4
export PATH="$JAVA_HOME/bin:.tools/apache-maven-3.9.6/bin:$PATH"
mvn clean package -q
cp target/BDSLauncher-1.0.0.jar BDSLauncher.jar
```

Không cần shade plugin — không có dependency ngoài.

## License

MIT
