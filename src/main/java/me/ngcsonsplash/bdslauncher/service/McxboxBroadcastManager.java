package me.ngcsonsplash.bdslauncher.service;

import me.ngcsonsplash.bdslauncher.model.InstallState;
import me.ngcsonsplash.bdslauncher.util.CurlDownload;
import me.ngcsonsplash.bdslauncher.util.Printer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

public class McxboxBroadcastManager {

    private static final String MCXBOX_API = "https://api.github.com/repos/MCXboxBroadcast/Broadcaster/releases/latest";
    private static final Path MCXBOX_DIR = Path.of("data", "mcxboxbroadcast");
    private static final Path MCXBOX_JAR = MCXBOX_DIR.resolve("MCXboxBroadcast.jar");
    private static final Path MCXBOX_CONFIG = MCXBOX_DIR.resolve("config");

    private final ObjectMapper mapper;

    public McxboxBroadcastManager() {
        this.mapper = new ObjectMapper();
    }

    public boolean isInstalled() {
        return Files.exists(MCXBOX_JAR);
    }

    public void setup(InstallState state) {
        Printer.printSection("MCXboxBroadcast Setup");

        if (isInstalled()) {
            Printer.printSuccess("MCXboxBroadcast already installed");
            state.getMcxboxbroadcast().setInstalled(true);
            return;
        }

        Printer.printInfo("Status", "Not installed, downloading...");
        download();
        state.getMcxboxbroadcast().setInstalled(true);
        state.getMcxboxbroadcast().setVersion("latest");
        InstallStateManager.save(state);
    }

    private void download() {
        try {
            Files.createDirectories(MCXBOX_DIR);
            Files.createDirectories(MCXBOX_CONFIG);

            String downloadUrl = getLatestReleaseUrl();
            if (downloadUrl == null) {
                throw new RuntimeException("Could not find MCXboxBroadcast release URL");
            }

            Printer.printInfo("Downloading", downloadUrl);
            CurlDownload.download(downloadUrl, MCXBOX_JAR);

            long jarSize = Files.size(MCXBOX_JAR);
            if (jarSize < 1000) {
                Files.delete(MCXBOX_JAR);
                throw new RuntimeException("Downloaded file is too small (" + jarSize + " bytes), likely not a valid JAR");
            }

            Printer.printSuccess("MCXboxBroadcast downloaded (" + (jarSize / 1024 / 1024) + " MB)");
        } catch (Exception e) {
            Printer.printError("Failed to download MCXboxBroadcast: " + e.getMessage());
            throw new RuntimeException("MCXboxBroadcast download failed", e);
        }
    }

    private String getLatestReleaseUrl() throws Exception {
        String json = CurlDownload.fetch(MCXBOX_API, "Accept", "application/vnd.github+json");
        JsonNode root = mapper.readTree(json);

        JsonNode assets = root.get("assets");
        if (assets == null || !assets.isArray()) return null;

        String fallback = null;
        for (JsonNode asset : assets) {
            String name = asset.get("name").asText();
            if (name.equals("MCXboxBroadcastStandalone.jar")) {
                return asset.get("browser_download_url").asText();
            }
            if (fallback == null && name.endsWith(".jar")) {
                fallback = asset.get("browser_download_url").asText();
            }
        }

        return fallback;
    }
}
