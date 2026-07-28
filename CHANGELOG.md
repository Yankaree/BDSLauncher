# Changelog

## [1.0.0] - 2026-07-28

Initial release. Zero-dependency Minecraft Bedrock Dedicated Server launcher for Linux/Pterodactyl.

### Features
- Auto-install BDS with interactive version selection
- Auto-update BDS when new versions available
- MCXboxBroadcast integration for Xbox Live
- Optional Tailscale VPN support (userspace networking)
- World Pack Sync (auto-generate world_behavior_packs.json / world_resource_packs.json)
- Console input forwarding to BDS stdin
- Graceful shutdown: BDS `stop` → 30s wait → force kill; MCXbox/Tailscale force kill
- Environment validation (Java 21+, Linux, amd64/aarch64, disk space)

### Technical
- **Zero external dependencies** — JAR is 59 KB standalone
- Custom `Json.java` recursive-descent parser for external JSON (GitHub API, manifest.json)
- INI-style `data/config.txt` for configuration
- `java.util.zip` for BDS zip extraction (no commons-compress)
- Java 21 virtual threads for console input
- No shade plugin, no transitive dependencies
