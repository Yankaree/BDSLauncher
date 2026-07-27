package com.bdslauncher.service;

import com.bdslauncher.model.InstallState;
import com.bdslauncher.util.Printer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class InstallStateManager {

    private static final Path INSTALL_FILE = Path.of("data", "install.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    public static InstallState load() {
        if (!Files.exists(INSTALL_FILE)) {
            Printer.printInfo("InstallState", "No install.json found, creating default");
            InstallState state = new InstallState();
            save(state);
            return state;
        }

        try {
            InstallState state = mapper.readValue(INSTALL_FILE.toFile(), InstallState.class);
            Printer.printInfo("InstallState", "Loaded from install.json");
            return state;
        } catch (IOException e) {
            Printer.printError("Failed to load install.json: " + e.getMessage());
            InstallState state = new InstallState();
            save(state);
            return state;
        }
    }

    public static void save(InstallState state) {
        try {
            Files.createDirectories(INSTALL_FILE.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(INSTALL_FILE.toFile(), state);
        } catch (IOException e) {
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
