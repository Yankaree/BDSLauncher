package me.ngcsonsplash.bdslauncher.service;

import me.ngcsonsplash.bdslauncher.util.Printer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class EnvironmentCheck {

    public static boolean check() {
        Printer.printSection("Environment");

        boolean javaOk = checkJava();
        boolean linuxOk = checkLinux();
        boolean archOk = checkArch();
        boolean diskOk = checkDiskSpace();

        Printer.println();
        Printer.printStatus("Environment", javaOk && linuxOk && archOk && diskOk);

        return javaOk && linuxOk && archOk && diskOk;
    }

    private static boolean checkJava() {
        try {
            String version = System.getProperty("java.version");
            if (version == null) {
                Printer.printStatus("Java 21+", false);
                return false;
            }

            int majorVersion;
            if (version.startsWith("1.")) {
                majorVersion = Integer.parseInt(version.substring(2, 3));
            } else {
                int dotIndex = version.indexOf('.');
                int dashIndex = version.indexOf('-');
                int endIndex = version.length();
                if (dotIndex != -1) endIndex = Math.min(endIndex, dotIndex);
                if (dashIndex != -1) endIndex = Math.min(endIndex, dashIndex);
                majorVersion = Integer.parseInt(version.substring(0, endIndex));
            }

            boolean ok = majorVersion >= 21;
            Printer.printStatus("Java 21+ (found " + version + ")", ok);
            return ok;
        } catch (Exception e) {
            Printer.printStatus("Java 21+", false);
            return false;
        }
    }

    private static boolean checkLinux() {
        String os = System.getProperty("os.name", "").toLowerCase();
        boolean ok = os.contains("linux");
        Printer.printStatus("Linux OS", ok);
        return ok;
    }

    private static boolean checkArch() {
        String arch = System.getProperty("os.arch", "");
        boolean ok = arch.equals("amd64") || arch.equals("x86_64") || arch.equals("aarch64");
        Printer.printStatus("Architecture (" + arch + ")", ok);
        return ok;
    }

    private static boolean checkDiskSpace() {
        try {
            Path dir = Path.of(".");
            long freeBytes = Files.getFileStore(dir).getUsableSpace();
            long freeMB = freeBytes / (1024 * 1024);
            boolean ok = freeMB > 500;
            Printer.printStatus("Disk space (" + freeMB + " MB free)", ok);
            return ok;
        } catch (Exception e) {
            Printer.printStatus("Disk space", false);
            return false;
        }
    }

    private static void println() {
        System.out.println();
    }
}
