package me.ngcsonsplash.bdslauncher.service;

import me.ngcsonsplash.bdslauncher.model.InstallState;
import me.ngcsonsplash.bdslauncher.util.Printer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class InstallStateManager {

    private static final Path CONFIG_FILE = Path.of("data", "config.txt");

    public static InstallState load() {
        if (!Files.exists(CONFIG_FILE)) {
            Printer.printInfo("Config", "No config.txt found, creating default");
            InstallState state = new InstallState();
            save(state);
            return state;
        }

        try {
            InstallState state = new InstallState();
            String content = Files.readString(CONFIG_FILE);
            String section = "";
            for (String line : content.split("\\r?\\n")) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (line.startsWith("[") && line.endsWith("]")) {
                    section = line.substring(1, line.length() - 1).trim();
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String key = line.substring(0, eq).trim();
                String val = line.substring(eq + 1).trim();
                setConfig(state, section, key, val);
            }
            Printer.printInfo("Config", "Loaded from config.txt");
            return state;
        } catch (Exception e) {
            Printer.printError("Failed to load config.txt: " + e.getMessage());
            InstallState state = new InstallState();
            save(state);
            return state;
        }
    }

    public static void save(InstallState state) {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            StringBuilder sb = new StringBuilder();
            appendLine(sb, "launcherVersion", state.getLauncherVersion());
            appendLine(sb, "autoUpdate", String.valueOf(state.isAutoUpdate()));
            if (state.getLastUpdateCheck() != null)
                appendLine(sb, "lastUpdateCheck", state.getLastUpdateCheck());
            sb.append('\n');
            appendSection(sb, "bds", Map.of(
                "installed", String.valueOf(state.getBds().isInstalled()),
                "version", state.getBds().getVersion() != null ? state.getBds().getVersion() : "",
                "platform", state.getBds().getPlatform(),
                "installedAt", state.getBds().getInstalledAt() != null ? state.getBds().getInstalledAt() : ""
            ));
            appendSection(sb, "mcxboxbroadcast", Map.of(
                "installed", String.valueOf(state.getMcxboxbroadcast().isInstalled()),
                "version", state.getMcxboxbroadcast().getVersion()
            ));
            appendSection(sb, "tailscale", Map.of(
                "enabled", String.valueOf(state.getTailscale().isEnabled()),
                "version", state.getTailscale().getVersion(),
                "authKey", state.getTailscale().getAuthKey() != null ? state.getTailscale().getAuthKey() : "",
                "userspace", String.valueOf(state.getTailscale().isUserspace())
            ));
            appendSection(sb, "cleanup", Map.of(
                "deleteMCXboxBroadcastLog", String.valueOf(state.getCleanup().isDeleteMCXboxBroadcastLog())
            ));
            Files.writeString(CONFIG_FILE, sb.toString());
        } catch (Exception e) {
            Printer.printError("Failed to save config.txt: " + e.getMessage());
        }
    }

    private static void appendLine(StringBuilder sb, String key, String value) {
        sb.append(key).append(" = ").append(value).append('\n');
    }

    private static void appendSection(StringBuilder sb, String name, Map<String, String> entries) {
        sb.append('[').append(name).append("]\n");
        for (var e : entries.entrySet()) {
            appendLine(sb, e.getKey(), e.getValue());
        }
        sb.append('\n');
    }

    private static void setConfig(InstallState state, String section, String key, String value) {
        switch (section) {
            case "" -> {
                switch (key) {
                    case "launcherVersion" -> state.setLauncherVersion(value);
                    case "autoUpdate" -> state.setAutoUpdate(Boolean.parseBoolean(value));
                    case "lastUpdateCheck" -> state.setLastUpdateCheck(value.isEmpty() ? null : value);
                }
            }
            case "bds" -> {
                switch (key) {
                    case "installed" -> state.getBds().setInstalled(Boolean.parseBoolean(value));
                    case "version" -> state.getBds().setVersion(value.isEmpty() ? null : value);
                    case "platform" -> state.getBds().setPlatform(value.isEmpty() ? "linux" : value);
                    case "installedAt" -> state.getBds().setInstalledAt(value.isEmpty() ? null : value);
                }
            }
            case "mcxboxbroadcast" -> {
                switch (key) {
                    case "installed" -> state.getMcxboxbroadcast().setInstalled(Boolean.parseBoolean(value));
                    case "version" -> state.getMcxboxbroadcast().setVersion(value.isEmpty() ? "latest" : value);
                }
            }
            case "tailscale" -> {
                switch (key) {
                    case "enabled" -> state.getTailscale().setEnabled(Boolean.parseBoolean(value));
                    case "version" -> state.getTailscale().setVersion(value.isEmpty() ? "1.101.162" : value);
                    case "authKey" -> state.getTailscale().setAuthKey(value.isEmpty() ? null : value);
                    case "userspace" -> state.getTailscale().setUserspace(Boolean.parseBoolean(value));
                }
            }
            case "cleanup" -> {
                if (key.equals("deleteMCXboxBroadcastLog"))
                    state.getCleanup().setDeleteMCXboxBroadcastLog(Boolean.parseBoolean(value));
            }
        }
    }

    public static void validate(InstallState state) {
        if (state.getBds().isInstalled()) {
            Path bdsBinary = Path.of("data", "bds", "bedrock_server");
            if (!Files.exists(bdsBinary)) {
                Printer.printWarning("BDS marked as installed but bedrock_server not found");
                state.getBds().setInstalled(false);
                save(state);
            }
        }

        if (state.getMcxboxbroadcast().isInstalled()) {
            Path mcxboxJar = Path.of("data", "mcxboxbroadcast", "MCXboxBroadcast.jar");
            if (!Files.exists(mcxboxJar)) {
                Printer.printWarning("MCXboxBroadcast marked as installed but JAR not found");
                state.getMcxboxbroadcast().setInstalled(false);
                save(state);
            }
        }
    }
}
