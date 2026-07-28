package me.ngcsonsplash.bdslauncher.service;

import me.ngcsonsplash.bdslauncher.model.InstallState;
import me.ngcsonsplash.bdslauncher.util.Json;
import me.ngcsonsplash.bdslauncher.util.Printer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class InstallStateManager {

    private static final Path INSTALL_FILE = Path.of("data", "install.json");

    public static InstallState load() {
        if (!Files.exists(INSTALL_FILE)) {
            Printer.printInfo("InstallState", "No install.json found, creating default");
            InstallState state = new InstallState();
            save(state);
            return state;
        }

        try {
            Object parsed = Json.parseFile(INSTALL_FILE);
            if (parsed instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typed = (Map<String, Object>) map;
                InstallState state = InstallState.fromMap(typed);
                Printer.printInfo("InstallState", "Loaded from install.json");
                return state;
            }
            throw new RuntimeException("Invalid JSON structure");
        } catch (Exception e) {
            Printer.printError("Failed to load install.json: " + e.getMessage());
            InstallState state = new InstallState();
            save(state);
            return state;
        }
    }

    public static void save(InstallState state) {
        try {
            Files.createDirectories(INSTALL_FILE.getParent());
            String json = Json.prettyPrint(state.toMap());
            Files.writeString(INSTALL_FILE, json);
        } catch (Exception e) {
            Printer.printError("Failed to save install.json: " + e.getMessage());
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
