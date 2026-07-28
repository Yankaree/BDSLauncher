package me.ngcsonsplash.bdslauncher.service;

import me.ngcsonsplash.bdslauncher.model.BdsMetadata;
import me.ngcsonsplash.bdslauncher.model.InstallState;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BdsDownloadManager {

    private static final Path BDS_DIR = Path.of("data", "bds");
    private static final Path CACHE_DIR = Path.of("data", "cache");

    private final HttpClient httpClient;
    private final BdsMetadataFetcher metadataFetcher;

    public BdsDownloadManager() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(60))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.metadataFetcher = new BdsMetadataFetcher();
    }

    public void installFirstTime(InstallState state) throws Exception {
        Printer.printSection("First BDS Installation");

        List<String> versions = metadataFetcher.getAvailableVersions();
        if (versions.isEmpty()) {
            throw new RuntimeException("No BDS versions available");
        }

        Printer.println("Available Bedrock versions:");
        Printer.println();
        for (int i = 0; i < Math.min(versions.size(), 20); i++) {
            Printer.println("  " + (i + 1) + ". " + versions.get(i));
        }
        Printer.println();

        int choice = -1;
        while (choice == -1) {
            choice = Printer.readChoice("Select version (1-" + Math.min(versions.size(), 20) + "): ",
                    Math.min(versions.size(), 20));
            if (choice == -1) {
                Printer.printError("Invalid choice, try again");
            }
        }

        String selectedVersion = versions.get(choice - 1);
        Printer.printInfo("Selected", selectedVersion);

        downloadAndInstall(selectedVersion, state);
    }

    public void updateToVersion(String version, InstallState state) throws Exception {
        Printer.printSection("BDS Update");
        downloadAndInstall(version, state);
    }

    private void downloadAndInstall(String version, InstallState state) throws Exception {
        Files.createDirectories(CACHE_DIR);
        Files.createDirectories(BDS_DIR);

        BdsMetadata metadata = metadataFetcher.fetchMetadata(version);
        String downloadUrl = metadata.getBinary().getLinux().getUrl();
        String sha256 = metadata.getBinary().getLinux().getSha256();

        Printer.printInfo("Version", metadata.getVersion());
        Printer.printInfo("URL", downloadUrl);
        Printer.printInfo("SHA256", sha256);
        Printer.println();

        Path zipFile = CACHE_DIR.resolve("bedrock-server-" + version + ".zip");
        Printer.printInfo("Downloading", "bedrock-server-" + version + ".zip");
        downloadFile(downloadUrl, zipFile);

        Printer.printInfo("Extracting", "to data/bds/");
        extractZip(zipFile, BDS_DIR);

        Path bdsBinary = BDS_DIR.resolve("bedrock_server");
        if (Files.exists(bdsBinary)) {
            setExecutable(bdsBinary);
            Printer.printInfo("bedrock_server", "extracted and set executable");
        } else {
            Printer.printWarning("bedrock_server not found after extract, listing directory:");
            try {
                Files.list(BDS_DIR).forEach(p -> Printer.println("  " + p.getFileName()));
            } catch (Exception ignored) {}
            throw new RuntimeException("BDS zip did not contain bedrock_server");
        }

        Files.deleteIfExists(zipFile);

        Path versionFile = BDS_DIR.resolve("version.txt");
        Files.writeString(versionFile, version);

        state.getBds().setInstalled(true);
        state.getBds().setVersion(version);
        state.getBds().setPlatform("linux");
        state.getBds().setInstalledAt(java.time.Instant.now().toString());
        InstallStateManager.save(state);

        Printer.printSuccess("BDS " + version + " installed");
    }

    public boolean isUpdateAvailable(String currentVersion) throws Exception {
        String latestVersion = metadataFetcher.getLatestVersion();
        return !currentVersion.equals(latestVersion);
    }

    public String getLatestVersion() throws Exception {
        return metadataFetcher.getLatestVersion();
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

    private void extractZip(Path zipFile, Path targetDir) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = targetDir.resolve(entry.getName()).normalize();

                if (!entryPath.startsWith(targetDir)) {
                    throw new RuntimeException("Zip entry attempts path traversal: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    private void setExecutable(Path path) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("chmod", "+x", path.toString());
        pb.inheritIO();
        pb.start().waitFor();
    }
}
