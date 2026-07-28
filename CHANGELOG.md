# Changelog

## [1.0.1] - 2026-07-28

### Bug Fixes
- **UpdateManager**: Fix NullPointerException when BDS version is null in install.json (now auto-reinstalls)
- **ConsoleInputManager**: Fix virtual thread not being detached on shutdown, preventing stale I/O errors
- **TailscaleManager**: Fix NullPointerException when Tailscale version is missing from config
- **McxboxBroadcastManager**: Fix NullPointerException when GitHub API asset entries lack required fields
- **BdsDownloadManager**: Fix zip cache files not being cleaned up after extraction
- **WorldPackManager**: Fix `\r\n` line endings not being handled in server.properties parsing

### Performance
- **Dependency reduction**: Replaced `commons-compress` 1.26.2 (~1.5 MB) with built-in `java.util.zip`
- **Zero external dependencies**: Removed Jackson databind 2.17.1 (~2.3 MB), replaced with custom lightweight `Json.java` parser
- **JAR size**: 4.8 MB → **59 KB** (98.8% reduction), zero third-party dependencies
