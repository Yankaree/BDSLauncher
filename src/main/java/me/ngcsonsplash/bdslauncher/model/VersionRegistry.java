package me.ngcsonsplash.bdslauncher.model;

import me.ngcsonsplash.bdslauncher.util.Json;
import java.util.*;

public class VersionRegistry {

    private Release release;
    private Preview preview;

    public Release getRelease() { return release; }

    @SuppressWarnings("unchecked")
    public static VersionRegistry fromMap(Map<String, Object> map) {
        if (map == null) return null;
        VersionRegistry r = new VersionRegistry();
        if (map.get("release") instanceof Map rm) r.release = Release.fromMap(rm);
        if (map.get("preview") instanceof Map pm) r.preview = Preview.fromMap(pm);
        return r;
    }

    public static class Release {
        private String latest;
        private List<String> versions;

        public String getLatest() { return latest; }
        public List<String> getVersions() { return versions; }

        @SuppressWarnings("unchecked")
        static Release fromMap(Map<String, Object> map) {
            Release r = new Release();
            r.latest = (String) map.get("latest");
            if (map.get("versions") instanceof List l) r.versions = (List<String>) (List) l;
            return r;
        }
    }

    public static class Preview {
        private String latest;
        private List<String> versions;

        public String getLatest() { return latest; }
        public List<String> getVersions() { return versions; }

        @SuppressWarnings("unchecked")
        static Preview fromMap(Map<String, Object> map) {
            Preview p = new Preview();
            p.latest = (String) map.get("latest");
            if (map.get("versions") instanceof List l) p.versions = (List<String>) (List) l;
            return p;
        }
    }
}
