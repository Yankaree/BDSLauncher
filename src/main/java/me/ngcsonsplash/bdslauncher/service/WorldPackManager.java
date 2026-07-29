package me.ngcsonsplash.bdslauncher.service;

import me.ngcsonsplash.bdslauncher.util.Printer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorldPackManager {

    private static final Path BDS_DIR = Path.of("data", "bds");
    private static final Path WORLDS_DIR = BDS_DIR.resolve("worlds");

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "\"uuid\"\\s*:\\s*\"([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})\"");
    private static final Pattern NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern VERSION_PATTERN = Pattern.compile("\"version\"\\s*:\\s*\\[([^\\]]*)\\]");

    private String activeWorld;
    private Path worldDir;

    public void synchronize() {
        Printer.printSection("Pack Manager");

        if (!detectActiveWorld()) {
            Printer.printWarning("No active world found, skipping pack sync");
            return;
        }

        Printer.printInfo("Active world", activeWorld);

        synchronizeBehaviorPacks();
        synchronizeResourcePacks();

        Printer.printSuccess("Pack synchronization complete");
    }

    private boolean detectActiveWorld() {
        Path serverProperties = BDS_DIR.resolve("server.properties");
        if (!Files.exists(serverProperties)) return false;

        try {
            String content = Files.readString(serverProperties);
            for (String line : content.split("\\r?\\n")) {
                line = line.trim();
                if (line.startsWith("level-name=")) {
                    activeWorld = line.substring("level-name=".length()).trim();
                    break;
                }
            }

            if (activeWorld == null || activeWorld.isEmpty()) return false;

            worldDir = WORLDS_DIR.resolve(activeWorld);
            return Files.exists(worldDir);

        } catch (IOException e) {
            Printer.printError("Failed to read server.properties: " + e.getMessage());
            return false;
        }
    }

    private void synchronizeBehaviorPacks() {
        Printer.printInfo("Scanning", "behavior_packs...");
        Path behaviorPacksDir = worldDir.resolve("behavior_packs");
        List<PackInfo> packs = scanPacks(behaviorPacksDir);
        writePackJson(worldDir.resolve("world_behavior_packs.json"), packs);
    }

    private void synchronizeResourcePacks() {
        Printer.printInfo("Scanning", "resource_packs...");
        Path resourcePacksDir = worldDir.resolve("resource_packs");
        List<PackInfo> packs = scanPacks(resourcePacksDir);
        writePackJson(worldDir.resolve("world_resource_packs.json"), packs);
    }

    private List<PackInfo> scanPacks(Path packsDir) {
        List<PackInfo> packs = new ArrayList<>();
        if (!Files.exists(packsDir) || !Files.isDirectory(packsDir)) return packs;

        Map<String, PackInfo> dedupMap = new LinkedHashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(packsDir)) {
            for (Path packFolder : stream) {
                if (!Files.isDirectory(packFolder)) continue;

                String folderName = packFolder.getFileName().toString();
                Path manifestFile = packFolder.resolve("manifest.json");

                if (!Files.exists(manifestFile)) {
                    Printer.printWarning("Skipped: " + packsDir.getFileName() + "/" + folderName);
                    Printer.printWarning("Reason: Missing manifest.json");
                    continue;
                }

                try {
                    PackInfo info = parseManifest(manifestFile, folderName);
                    if (dedupMap.containsKey(info.uuid)) {
                        Printer.printWarning("Duplicate UUID " + info.uuid + " in " + folderName + ", keeping first");
                        continue;
                    }
                    dedupMap.put(info.uuid, info);
                    packs.add(info);
                    Printer.printInfo("Found pack", info.name + " (" + info.uuid + " v" + formatVersion(info.version) + ")");
                } catch (Exception e) {
                    Printer.printWarning("Skipped: " + packsDir.getFileName() + "/" + folderName);
                    Printer.printWarning("Reason: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            Printer.printError("Failed to scan " + packsDir.getFileName() + ": " + e.getMessage());
        }

        return packs;
    }

    private PackInfo parseManifest(Path manifestFile, String folderName) throws Exception {
        String content = Files.readString(manifestFile);

        int headerIdx = content.indexOf("\"header\"");
        if (headerIdx < 0) throw new RuntimeException("Missing header in manifest.json");

        int headerStart = content.indexOf('{', headerIdx);
        if (headerStart < 0) throw new RuntimeException("Invalid header in manifest.json");

        int headerEnd = findMatchingBrace(content, headerStart);
        if (headerEnd < 0) throw new RuntimeException("Unclosed header object in manifest.json");

        String header = content.substring(headerStart, headerEnd + 1);

        Matcher uuidMatcher = UUID_PATTERN.matcher(header);
        if (!uuidMatcher.find()) throw new RuntimeException("Missing UUID in manifest.json");
        String uuid = uuidMatcher.group(1).trim();

        Matcher nameMatcher = NAME_PATTERN.matcher(header);
        String name = nameMatcher.find() ? nameMatcher.group(1) : folderName;
        if (name == null || name.isEmpty()) name = folderName;

        Matcher versionMatcher = VERSION_PATTERN.matcher(header);
        if (!versionMatcher.find()) throw new RuntimeException("Missing version in manifest.json");
        int[] version = parseVersionArray(versionMatcher.group(1));

        return new PackInfo(uuid, version, name);
    }

    private int findMatchingBrace(String s, int openIdx) {
        int depth = 1;
        for (int i = openIdx + 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private int[] parseVersionArray(String arrayContent) {
        String[] parts = arrayContent.split(",");
        int[] version = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            version[i] = Integer.parseInt(parts[i].trim());
        }
        return version;
    }

    private void writePackJson(Path jsonFile, List<PackInfo> packs) {
        try {
            Files.createDirectories(jsonFile.getParent());

            StringBuilder sb = new StringBuilder();
            sb.append("[\n");

            for (int i = 0; i < packs.size(); i++) {
                PackInfo p = packs.get(i);
                sb.append("  {\n");
                sb.append("    \"pack_id\": \"").append(p.uuid).append("\",\n");
                sb.append("    \"version\": [");
                for (int j = 0; j < p.version.length; j++) {
                    if (j > 0) sb.append(", ");
                    sb.append(p.version[j]);
                }
                sb.append("]\n");
                sb.append("  }");
                if (i < packs.size() - 1) sb.append(",");
                sb.append("\n");
            }

            sb.append("]\n");

            Files.writeString(jsonFile, sb.toString());

            if (packs.isEmpty()) {
                Printer.printInfo(jsonFile.getFileName().toString(), "Empty (no packs)");
            } else {
                Printer.printSuccess(jsonFile.getFileName().toString() + " updated (" + packs.size() + " packs)");
            }
        } catch (IOException e) {
            Printer.printError("Failed to write " + jsonFile.getFileName() + ": " + e.getMessage());
        }
    }

    private static String formatVersion(int[] version) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < version.length; i++) {
            if (i > 0) sb.append(".");
            sb.append(version[i]);
        }
        return sb.toString();
    }

    private static class PackInfo {
        final String uuid;
        final int[] version;
        final String name;

        PackInfo(String uuid, int[] version, String name) {
            this.uuid = uuid;
            this.version = version;
            this.name = name;
        }
    }
}
