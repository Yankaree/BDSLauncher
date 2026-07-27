package me.ngcsonsplash.bdslauncher.service;

import me.ngcsonsplash.bdslauncher.util.Printer;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessManager {

    private Process bdsProcess;
    private Process mcxboxProcess;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public void startMcxboxBroadcast() {
        Printer.printSection("MCXboxBroadcast");

        Path jar = Path.of("data", "mcxboxbroadcast", "MCXboxBroadcast.jar");
        if (!java.nio.file.Files.exists(jar)) {
            Printer.printError("MCXboxBroadcast.jar not found");
            return;
        }

        try {
            long jarSize = java.nio.file.Files.size(jar);
            if (jarSize < 1000) {
                Printer.printError("MCXboxBroadcast.jar is too small (" + jarSize + " bytes), likely corrupted. Delete it and re-run.");
                return;
            }
        } catch (Exception e) {
            Printer.printError("Cannot read MCXboxBroadcast.jar: " + e.getMessage());
            return;
        }

        try {
            Path logFile = Path.of("data", "logs", "mcxboxbroadcast.log");
            java.nio.file.Files.createDirectories(logFile.getParent());

            ProcessBuilder pb = new ProcessBuilder("java", "-jar", jar.toAbsolutePath().toString());
            pb.directory(Path.of("data", "mcxboxbroadcast").toFile());
            java.io.File logTarget = logFile.toFile();
            pb.redirectOutput(ProcessBuilder.Redirect.to(logTarget));
            pb.redirectError(ProcessBuilder.Redirect.to(logTarget));
            pb.redirectInput(ProcessBuilder.Redirect.from(new java.io.File("/dev/null")));

            mcxboxProcess = pb.start();

            Printer.printSuccess("MCXboxBroadcast started (PID: " + mcxboxProcess.pid() + ")");

            Thread.sleep(2000);

        } catch (Exception e) {
            Printer.printError("Failed to start MCXboxBroadcast: " + e.getMessage());
        }
    }

    public void startBds(ConsoleInputManager inputManager) {
        Printer.printSection("Bedrock Server");

        Path bdsDir = Path.of("data", "bds");
        Path bdsBinary = bdsDir.resolve("bedrock_server");

        if (!java.nio.file.Files.exists(bdsBinary)) {
            Printer.printError("bedrock_server not found in data/bds/");
            Printer.printInfo("Tip", "Delete data/install.json and re-run to reinstall BDS");
            return;
        }

        try {
            if (!java.nio.file.Files.isExecutable(bdsBinary)) {
                Printer.printWarning("bedrock_server has no execute permission, fixing...");
                ProcessBuilder chmod = new ProcessBuilder("chmod", "+x", bdsBinary.toAbsolutePath().toString());
                chmod.inheritIO();
                chmod.start().waitFor();
            }
        } catch (Exception e) {
            Printer.printError("Cannot set execute permission: " + e.getMessage());
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(bdsBinary.toAbsolutePath().toString());
            pb.directory(bdsDir.toFile());
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            pb.redirectInput(ProcessBuilder.Redirect.PIPE);

            bdsProcess = pb.start();
            running.set(true);

            if (inputManager != null) {
                inputManager.attachBDSInput(bdsProcess);
            }

            Printer.printSuccess("Bedrock Server starting (PID: " + bdsProcess.pid() + ")");

            int exitCode = bdsProcess.waitFor();
            Printer.printInfo("BDS exited", "code: " + exitCode);

        } catch (Exception e) {
            Printer.printError("Failed to start Bedrock Server: " + e.getMessage());
        }
    }

    public void sendCommand(String command) {
        if (bdsProcess != null && bdsProcess.isAlive()) {
            try {
                OutputStream os = bdsProcess.getOutputStream();
                synchronized (os) {
                    os.write((command + "\n").getBytes());
                    os.flush();
                }
            } catch (Exception e) {
                Printer.printError("Failed to send command: " + e.getMessage());
            }
        }
    }

    public void stopBds() {
        if (bdsProcess != null && bdsProcess.isAlive()) {
            Printer.printInfo("Stopping", "BDS...");
            sendCommand("stop");
            try {
                bdsProcess.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
                if (bdsProcess.isAlive()) {
                    bdsProcess.destroyForcibly();
                }
            } catch (Exception ignored) {}
        }
    }

    public void stopMcxbox() {
        if (mcxboxProcess != null && mcxboxProcess.isAlive()) {
            Printer.printInfo("Stopping", "MCXboxBroadcast...");
            mcxboxProcess.destroyForcibly();
            try {
                mcxboxProcess.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception ignored) {}
        }
    }

    public void shutdown(boolean deleteMcxboxLog) {
        Printer.printSection("Shutting Down");
        stopBds();
        stopMcxbox();

        if (deleteMcxboxLog) {
            Path logFile = Path.of("data", "logs", "mcxboxbroadcast.log");
            try {
                java.nio.file.Files.deleteIfExists(logFile);
            } catch (Exception e) {
                Printer.printWarning("Unable to delete MCXboxBroadcast log");
            }
        }

        running.set(false);
        Printer.printSuccess("Launcher stopped");
    }

    public boolean isRunning() {
        return running.get();
    }
}
