package me.ngcsonsplash.bdslauncher.service;

import me.ngcsonsplash.bdslauncher.model.InstallState;
import me.ngcsonsplash.bdslauncher.util.Printer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class TailscaleManager {

    private static final Path TAILSCALE_DIR = Path.of("data", "tailscale");
    private static final Path TAILSCALED = TAILSCALE_DIR.resolve("tailscaled");
    private static final Path TAILSCALE_BIN = TAILSCALE_DIR.resolve("tailscale");
    private static final Path STATE_FILE = TAILSCALE_DIR.resolve("tailscale.state");
    private static final Path SOCKET_FILE = TAILSCALE_DIR.resolve("tailscale.sock");
    private static final Path LOGGED_IN_FLAG = TAILSCALE_DIR.resolve("logged_in.flag");
    private static final Path VERSION_FILE = TAILSCALE_DIR.resolve("version.txt");
    private static final Path LOG_DIR = Path.of("data", "logs");
    private static final Path TAILSCALED_LOG = LOG_DIR.resolve("tailscaled.log");

    private static final String TAILSCALE_DOWNLOAD_BASE = "https://pkgs.tailscale.com/stable/tailscale_${version}_amd64.tgz";

    private Process tailscaledProcess;
    private final HttpClient httpClient;

    public TailscaleManager() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void setup(InstallState state) throws Exception {
        Printer.printSection("Tailscale Setup");

        if (!state.getTailscale().isEnabled()) {
            Printer.printInfo("Tailscale", "Disabled, skipping");
            return;
        }

        String configVersion = state.getTailscale().getVersion();

        if (isVersionChanged(configVersion)) {
            Printer.printInfo("Tailscale", "Version changed to " + configVersion + ", reinstalling...");
            cleanInstall();
        }

        checkInstalled(configVersion);
        startDaemon();

        if (isFirstTimeLogin()) {
            firstLogin(state);
        } else {
            Printer.printInfo("Tailscale", "Already authenticated, skipping login");
        }

        verifyStatus();
    }

    public void checkInstalled(String expectedVersion) throws Exception {
        Printer.printInfo("Tailscale", "Checking binaries...");

        if (Files.exists(TAILSCALED) && Files.exists(TAILSCALE_BIN)) {
            String installedVersion = getInstalledVersion();
            if (expectedVersion.equals(installedVersion)) {
                Printer.printSuccess("Tailscale binaries found (v" + installedVersion + ")");
                return;
            }
            Printer.printInfo("Tailscale", "Version mismatch (installed: " + installedVersion + ", expected: " + expectedVersion + ")");
        }

        install(expectedVersion);
    }

    private boolean isVersionChanged(String configVersion) throws Exception {
        if (!Files.exists(VERSION_FILE)) {
            return true;
        }
        String installedVersion = Files.readString(VERSION_FILE).trim();
        return !configVersion.equals(installedVersion);
    }

    private String getInstalledVersion() throws Exception {
        if (Files.exists(VERSION_FILE)) {
            return Files.readString(VERSION_FILE).trim();
        }
        return null;
    }

    private void cleanInstall() throws Exception {
        Printer.printInfo("Tailscale", "Cleaning old installation...");
        Files.deleteIfExists(LOGGED_IN_FLAG);
        Files.deleteIfExists(TAILSCALED);
        Files.deleteIfExists(TAILSCALE_BIN);
        Files.deleteIfExists(VERSION_FILE);
    }

    private void install(String version) throws Exception {
        Printer.printInfo("Tailscale", "Downloading " + version + "...");

        Files.createDirectories(TAILSCALE_DIR);

        String url = TAILSCALE_DOWNLOAD_BASE.replace("${version}", version);
        Path tgzFile = TAILSCALE_DIR.resolve("tailscale.tgz");

        downloadFile(url, tgzFile);
        extractTgz(tgzFile, TAILSCALE_DIR);
        Files.deleteIfExists(tgzFile);

        Path extractedDir = TAILSCALE_DIR.resolve("tailscale_" + version + "_amd64");
        if (Files.exists(extractedDir)) {
            Path extractedTailscaled = extractedDir.resolve("tailscaled");
            Path extractedTailscale = extractedDir.resolve("tailscale");

            if (Files.exists(extractedTailscaled)) {
                Files.move(extractedTailscaled, TAILSCALED, StandardCopyOption.REPLACE_EXISTING);
            }
            if (Files.exists(extractedTailscale)) {
                Files.move(extractedTailscale, TAILSCALE_BIN, StandardCopyOption.REPLACE_EXISTING);
            }

            deleteDirectory(extractedDir);
        }

        setExecutable(TAILSCALED);
        setExecutable(TAILSCALE_BIN);
        Files.writeString(VERSION_FILE, version);

        Printer.printSuccess("Tailscale installed (v" + version + ")");
    }

    public void startDaemon() throws Exception {
        Printer.printInfo("Tailscale", "Starting tailscaled...");

        if (tailscaledProcess != null && tailscaledProcess.isAlive()) {
            Printer.printInfo("Tailscale", "Already running");
            return;
        }

        Files.createDirectories(TAILSCALE_DIR);
        Files.createDirectories(LOG_DIR);

        ProcessBuilder pb = new ProcessBuilder(
                TAILSCALED.toAbsolutePath().toString(),
                "--tun=userspace-networking",
                "--state=" + STATE_FILE.toAbsolutePath(),
                "--socket=" + SOCKET_FILE.toAbsolutePath()
        );
        pb.directory(TAILSCALE_DIR.toFile());

        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(TAILSCALED_LOG.toFile()));

        tailscaledProcess = pb.start();

        Printer.printInfo("Tailscale", "Waiting for socket...");

        int maxWait = 15;
        for (int i = 0; i < maxWait; i++) {
            if (Files.exists(SOCKET_FILE)) {
                Printer.printSuccess("tailscaled started (PID: " + tailscaledProcess.pid() + ")");
                return;
            }
            Thread.sleep(1000);
        }

        if (!Files.exists(SOCKET_FILE)) {
            throw new RuntimeException("tailscaled failed to start within " + maxWait + "s");
        }
    }

    private boolean isFirstTimeLogin() {
        return !Files.exists(LOGGED_IN_FLAG);
    }

    private void firstLogin(InstallState state) throws Exception {
        Printer.printInfo("Tailscale", "First-time authentication required");

        String authKey = state.getTailscale().getAuthKey();

        if (authKey != null && !authKey.isEmpty()) {
            loginWithAuthKey(authKey);
        } else {
            loginInteractive();
        }

        Files.createDirectories(TAILSCALE_DIR);
        Files.writeString(LOGGED_IN_FLAG, "logged_in=true");

        Printer.printSuccess("Tailscale authentication successful");
    }

    private void loginWithAuthKey(String authKey) throws Exception {
        Printer.printInfo("Tailscale", "Authenticating with auth key...");

        ProcessBuilder pb = new ProcessBuilder(
                TAILSCALE_BIN.toAbsolutePath().toString(),
                "--socket=" + SOCKET_FILE.toAbsolutePath(),
                "up",
                "--auth-key=" + authKey
        );
        pb.inheritIO();

        int exitCode = pb.start().waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("tailscale up failed with exit code: " + exitCode);
        }
    }

    private void loginInteractive() throws Exception {
        Printer.printInfo("Tailscale", "Starting interactive login...");

        ProcessBuilder pb = new ProcessBuilder(
                TAILSCALE_BIN.toAbsolutePath().toString(),
                "--socket=" + SOCKET_FILE.toAbsolutePath(),
                "up"
        );
        pb.inheritIO();

        int exitCode = pb.start().waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("tailscale up failed with exit code: " + exitCode);
        }
    }

    public boolean verifyStatus() throws Exception {
        Printer.printInfo("Tailscale", "Checking status...");

        ProcessBuilder pb = new ProcessBuilder(
                TAILSCALE_BIN.toAbsolutePath().toString(),
                "--socket=" + SOCKET_FILE.toAbsolutePath(),
                "status"
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();
        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            output = sb.toString();
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            Printer.printError("Tailscale status check failed (code: " + exitCode + ")");
            return false;
        }

        Printer.printSuccess("Tailscale is running");
        return true;
    }

    public void stop() {
        Printer.printInfo("Tailscale", "Stopping tailscaled...");

        if (tailscaledProcess != null && tailscaledProcess.isAlive()) {
            try {
                tailscaledProcess.destroyForcibly();
                tailscaledProcess.waitFor(10, TimeUnit.SECONDS);
                Printer.printSuccess("tailscaled stopped");
            } catch (Exception e) {
                Printer.printError("Failed to stop tailscaled: " + e.getMessage());
            }
        } else {
            Printer.printInfo("Tailscale", "Not running");
        }
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

    private void extractTgz(Path tgzFile, Path targetDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("tar", "xzf", tgzFile.toAbsolutePath().toString());
        pb.directory(targetDir.toFile());
        pb.inheritIO();
        int exitCode = pb.start().waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("tar extraction failed with exit code: " + exitCode);
        }
    }

    private void setExecutable(Path path) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("chmod", "+x", path.toString());
        pb.inheritIO();
        pb.start().waitFor();
    }

    private void deleteDirectory(Path dir) throws Exception {
        if (Files.isDirectory(dir)) {
            try (var stream = Files.list(dir)) {
                for (Path child : stream.toList()) {
                    deleteDirectory(child);
                }
            }
            Files.delete(dir);
        } else {
            Files.deleteIfExists(dir);
        }
    }
}
