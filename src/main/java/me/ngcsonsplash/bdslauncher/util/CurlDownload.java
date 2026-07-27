package me.ngcsonsplash.bdslauncher.util;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CurlDownload {

    public static void download(String url, Path target) throws Exception {
        Files.createDirectories(target.getParent());
        List<String> cmd = List.of(
                "curl", "-fsSL",
                "-o", target.toAbsolutePath().toString(),
                "-A", "BDSLauncher/1.0",
                "--connect-timeout", "30",
                "--max-time", "300",
                url
        );
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        String output;
        try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            output = sb.toString();
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("curl failed (exit " + exitCode + "): " + output.trim());
        }
    }

    public static String fetch(String url) throws Exception {
        List<String> cmd = List.of(
                "curl", "-fsSL",
                "-A", "BDSLauncher/1.0",
                "--connect-timeout", "30",
                "--max-time", "60",
                url
        );
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("curl failed (exit " + exitCode + "): " + sb.toString().trim());
        }
        return sb.toString();
    }

    public static String fetch(String url, String... headers) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add("curl");
        cmd.add("-fsSL");
        cmd.add("-A");
        cmd.add("BDSLauncher/1.0");
        for (int i = 0; i < headers.length; i += 2) {
            cmd.add("-H");
            cmd.add(headers[i] + ": " + headers[i + 1]);
        }
        cmd.add("--connect-timeout");
        cmd.add("30");
        cmd.add("--max-time");
        cmd.add("60");
        cmd.add(url);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("curl failed (exit " + exitCode + "): " + sb.toString().trim());
        }
        return sb.toString();
    }
}
