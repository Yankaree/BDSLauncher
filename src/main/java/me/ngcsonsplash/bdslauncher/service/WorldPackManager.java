package me.ngcsonsplash.bdslauncher.service;

import me.ngcsonsplash.bdslauncher.util.Json;
import me.ngcsonsplash.bdslauncher.util.Printer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WorldPackManager {

    private static final Path BDS_DIR = Path.of("data", "bds");
    private static final Path WORLDS_DIR = BDS_DIR.resolve("worlds");

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
                    Printer.printWarning("Reason: Invalid manifest.json");
                    continue;
                }

                try {
                    PackInfo info = readManifest(manifestFile, folderName);
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

    @SuppressWarnings("unchecked")
    private PackInfo readManifest(Path manifestFile, String folderName) throws Exception {
        Map<String, Object> root = Json.asMap(Json.parseFile(manifestFile));
        if (root == null) throw new RuntimeException("Invalid manifest.json");

        Map<String, Object> header = Json.asMap(root.get("header"));
        if (header == null) throw new RuntimeException("Missing header in manifest.json");

        String uuid = Json.asString(header.get("uuid"));
        if (uuid == null || uuid.isEmpty()) throw new RuntimeException("Missing or empty UUID in manifest.json");
        uuid = uuid.trim();

        if (!uuid.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
            throw new RuntimeException("Invalid UUID format: " + uuid);
        }

        List<Object> versionList = Json.asList(header.get("version"));
        if (versionList == null || versionList.isEmpty()) {
            throw new RuntimeException("Missing or invalid version in manifest.json");
        }

        int[] version = new int[versionList.size()];
        for (int i = 0; i < versionList.size(); i++) {
            version[i] = Json.asInt(versionList.get(i));
        }

        String name = header.containsKey("name") ? String.valueOf(header.get("name")) : folderName;

        List<ModuleInfo> modules = new ArrayList<>();
        List<Object> modulesList = Json.asList(root.get("modules"));
        if (modulesList != null) {
            for (Object modObj : modulesList) {
                Map<String, Object> mod = Json.asMap(modObj);
                if (mod == null) continue;
                String moduleType = (String) mod.getOrDefault("type", "unknown");
                String moduleUuid = (String) mod.getOrDefault("uuid", "");
                modules.add(new ModuleInfo(moduleType, moduleUuid));
            }
        }

        return new PackInfo(uuid, version, name, modules);
    }

    private void writePackJson(Path jsonFile, List<PackInfo> packs) {
        try {
            Files.createDirectories(jsonFile.getParent());

            if (packs.isEmpty()) {
                Files.writeString(jsonFile, "[]\n");
                Printer.printInfo(jsonFile.getFileName().toString(), "Empty (no packs)");
                return;
            }

            List<Map<String, Object>> entries = new ArrayList<>();
            for (PackInfo p : packs) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("pack_id", p.uuid);
                List<Integer> ver = new ArrayList<>();
                for (int v : p.version) ver.add(v);
                entry.put("version", ver);
                entries.add(entry);
            }

            Files.writeString(jsonFile, Json.prettyPrint(entries) + "\n");
            Printer.printSuccess(jsonFile.getFileName().toString() + " updated (" + packs.size() + " packs)");
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
        final List<ModuleInfo> modules;

        PackInfo(String uuid, int[] version, String name, List<ModuleInfo> modules) {
            this.uuid = uuid;
            this.version = version;
            this.name = name;
            this.modules = modules;
        }
    }

    private static class ModuleInfo {
        final String type;
        final String uuid;

        ModuleInfo(String type, String uuid) {
            this.type = type;
            this.uuid = uuid;
        }
    }
}
