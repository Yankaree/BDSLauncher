package me.ngcsonsplash.bdslauncher.model;

import me.ngcsonsplash.bdslauncher.util.Json;
import java.util.*;

public class BdsMetadata {

    private String version;
    private Binary binary;

    public String getVersion() { return version; }
    public Binary getBinary() { return binary; }

    @SuppressWarnings("unchecked")
    public static BdsMetadata fromMap(Map<String, Object> map) {
        if (map == null) return null;
        BdsMetadata m = new BdsMetadata();
        m.version = (String) map.get("version");
        if (map.get("binary") instanceof Map bm) m.binary = Binary.fromMap(bm);
        return m;
    }

    public static class Binary {
        private PlatformInfo windows;
        private PlatformInfo linux;

        public PlatformInfo getWindows() { return windows; }
        public PlatformInfo getLinux() { return linux; }

        @SuppressWarnings("unchecked")
        static Binary fromMap(Map<String, Object> map) {
            Binary b = new Binary();
            if (map.get("windows") instanceof Map wm) b.windows = PlatformInfo.fromMap(wm);
            if (map.get("linux") instanceof Map lm) b.linux = PlatformInfo.fromMap(lm);
            return b;
        }
    }

    public static class PlatformInfo {
        private String url;
        private String sha256;

        public String getUrl() { return url; }
        public String getSha256() { return sha256; }

        @SuppressWarnings("unchecked")
        static PlatformInfo fromMap(Map<String, Object> map) {
            PlatformInfo p = new PlatformInfo();
            p.url = (String) map.get("url");
            p.sha256 = (String) map.get("sha256");
            return p;
        }
    }
}
