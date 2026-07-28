package me.ngcsonsplash.bdslauncher.service;

import me.ngcsonsplash.bdslauncher.model.InstallState;
import me.ngcsonsplash.bdslauncher.util.Json;
import me.ngcsonsplash.bdslauncher.util.Printer;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class McxboxBroadcastManager {

    private static final String MCXBOX_API = "https://api.github.com/repos/MCXboxBroadcast/Broadcaster/releases/latest";
    private static final Path MCXBOX_DIR = Path.of("data", "mcxboxbroadcast");
    private static final Path MCXBOX_JAR = MCXBOX_DIR.resolve("MCXboxBroadcast.jar");
    private static final Path MCXBOX_CONFIG = MCXBOX_DIR.resolve("config");

    private final HttpClient httpClient;

    public McxboxBroadcastManager() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
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
            downloadFile(downloadUrl, MCXBOX_JAR);

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
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MCXBOX_API))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "BDSLauncher/1.0")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> root = Json.asMap(Json.parse(response.body()));
        if (root == null) return null;

        Object assetsObj = root.get("assets");
        if (!(assetsObj instanceof List<?> assets)) return null;

        String fallback = null;
        for (Object item : assets) {
            if (!(item instanceof Map<?, ?> asset)) continue;
            @SuppressWarnings("unchecked")
            Map<String, Object> a = (Map<String, Object>) asset;
            String name = Json.asString(a.get("name"));
            String url = Json.asString(a.get("browser_download_url"));
            if (name == null || url == null) continue;
            if (name.equals("MCXboxBroadcastStandalone.jar")) return url;
            if (fallback == null && name.endsWith(".jar")) fallback = url;
        }

        return fallback;
    }

    private void downloadFile(String url, Path target) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "BDSLauncher/1.0")
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + " downloading " + url);
        }

        try (InputStream in = response.body()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
