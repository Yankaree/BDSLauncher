package me.ngcsonsplash.bdslauncher.service;

import me.ngcsonsplash.bdslauncher.model.BdsMetadata;
import me.ngcsonsplash.bdslauncher.model.InstallState;
import me.ngcsonsplash.bdslauncher.util.CurlDownload;
import me.ngcsonsplash.bdslauncher.util.Printer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class BdsDownloadManager {

    private static final Path BDS_DIR = Path.of("data", "bds");
    private static final Path CACHE_DIR = Path.of("data", "cache");

    private final BdsMetadataFetcher metadataFetcher;
    private final ObjectMapper mapper;

    public BdsDownloadManager() {
        this.metadataFetcher = new BdsMetadataFetcher();
        this.mapper = new ObjectMapper();
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
        CurlDownload.download(downloadUrl, zipFile);

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

    private void extractZip(Path zipFile, Path targetDir) throws Exception {
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(Files.newInputStream(zipFile))) {
            ZipArchiveEntry entry;
            while ((entry = zis.getNextZipEntry()) != null) {
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
            }
        }
    }

    private void setExecutable(Path path) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("chmod", "+x", path.toString());
        pb.inheritIO();
        pb.start().waitFor();
    }
}
