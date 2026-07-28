package me.ngcsonsplash.bdslauncher.model;

import java.util.*;

public class InstallState {

    private String launcherVersion = "1.0.0";
    private boolean autoUpdate = false;
    private BdsInfo bds = new BdsInfo();
    private McxboxInfo mcxboxbroadcast = new McxboxInfo();
    private String lastUpdateCheck;
    private TailscaleConfig tailscale = new TailscaleConfig();
    private CleanupConfig cleanup = new CleanupConfig();

    public String getLauncherVersion() { return launcherVersion; }
    public void setLauncherVersion(String v) { launcherVersion = v; }
    public boolean isAutoUpdate() { return autoUpdate; }
    public void setAutoUpdate(boolean v) { autoUpdate = v; }
    public BdsInfo getBds() { return bds; }
    public void setBds(BdsInfo v) { bds = v; }
    public McxboxInfo getMcxboxbroadcast() { return mcxboxbroadcast; }
    public void setMcxboxbroadcast(McxboxInfo v) { mcxboxbroadcast = v; }
    public String getLastUpdateCheck() { return lastUpdateCheck; }
    public void setLastUpdateCheck(String v) { lastUpdateCheck = v; }
    public TailscaleConfig getTailscale() { return tailscale; }
    public void setTailscale(TailscaleConfig v) { tailscale = v; }
    public CleanupConfig getCleanup() { return cleanup; }
    public void setCleanup(CleanupConfig v) { cleanup = v; }

    @SuppressWarnings("unchecked")
    public static InstallState fromMap(Map<String, Object> map) {
        InstallState s = new InstallState();
        if (map == null) return s;
        s.launcherVersion = (String) map.getOrDefault("launcherVersion", s.launcherVersion);
        s.autoUpdate = (Boolean) map.getOrDefault("autoUpdate", s.autoUpdate);
        s.lastUpdateCheck = (String) map.get("lastUpdateCheck");
        if (map.get("bds") instanceof Map bm) s.bds = BdsInfo.fromMap(bm);
        if (map.get("mcxboxbroadcast") instanceof Map mm) s.mcxboxbroadcast = McxboxInfo.fromMap(mm);
        if (map.get("tailscale") instanceof Map tm) s.tailscale = TailscaleConfig.fromMap(tm);
        if (map.get("cleanup") instanceof Map cm) s.cleanup = CleanupConfig.fromMap(cm);
        return s;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("launcherVersion", launcherVersion);
        map.put("autoUpdate", autoUpdate);
        map.put("bds", bds.toMap());
        map.put("mcxboxbroadcast", mcxboxbroadcast.toMap());
        map.put("tailscale", tailscale.toMap());
        map.put("cleanup", cleanup.toMap());
        if (lastUpdateCheck != null) map.put("lastUpdateCheck", lastUpdateCheck);
        return map;
    }

    public static class BdsInfo {
        private boolean installed = false;
        private String version;
        private String platform = "linux";
        private String installedAt;

        public boolean isInstalled() { return installed; }
        public void setInstalled(boolean v) { installed = v; }
        public String getVersion() { return version; }
        public void setVersion(String v) { version = v; }
        public String getPlatform() { return platform; }
        public void setPlatform(String v) { platform = v; }
        public String getInstalledAt() { return installedAt; }
        public void setInstalledAt(String v) { installedAt = v; }

        @SuppressWarnings("unchecked")
        static BdsInfo fromMap(Map<String, Object> map) {
            BdsInfo i = new BdsInfo();
            i.installed = (Boolean) map.getOrDefault("installed", false);
            i.version = (String) map.get("version");
            i.platform = (String) map.getOrDefault("platform", "linux");
            i.installedAt = (String) map.get("installedAt");
            return i;
        }

        Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("installed", installed);
            if (version != null) m.put("version", version);
            m.put("platform", platform);
            if (installedAt != null) m.put("installedAt", installedAt);
            return m;
        }
    }

    public static class McxboxInfo {
        private boolean installed = false;
        private String version = "latest";

        public boolean isInstalled() { return installed; }
        public void setInstalled(boolean v) { installed = v; }
        public String getVersion() { return version; }
        public void setVersion(String v) { version = v; }

        @SuppressWarnings("unchecked")
        static McxboxInfo fromMap(Map<String, Object> map) {
            McxboxInfo i = new McxboxInfo();
            i.installed = (Boolean) map.getOrDefault("installed", false);
            i.version = (String) map.getOrDefault("version", "latest");
            return i;
        }

        Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("installed", installed);
            m.put("version", version);
            return m;
        }
    }

    public static class TailscaleConfig {
        private boolean enabled = false;
        private String version = "1.101.162";
        private String authKey;
        private boolean userspace = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean v) { enabled = v; }
        public String getVersion() { return version; }
        public void setVersion(String v) { version = v; }
        public String getAuthKey() { return authKey; }
        public void setAuthKey(String v) { authKey = v; }
        public boolean isUserspace() { return userspace; }
        public void setUserspace(boolean v) { userspace = v; }

        @SuppressWarnings("unchecked")
        static TailscaleConfig fromMap(Map<String, Object> map) {
            TailscaleConfig c = new TailscaleConfig();
            c.enabled = (Boolean) map.getOrDefault("enabled", false);
            c.version = (String) map.getOrDefault("version", "1.101.162");
            c.authKey = (String) map.get("authKey");
            c.userspace = (Boolean) map.getOrDefault("userspace", true);
            return c;
        }

        Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("enabled", enabled);
            m.put("version", version);
            if (authKey != null) m.put("authKey", authKey);
            m.put("userspace", userspace);
            return m;
        }
    }

    public static class CleanupConfig {
        private boolean deleteMCXboxBroadcastLog = true;

        public boolean isDeleteMCXboxBroadcastLog() { return deleteMCXboxBroadcastLog; }
        public void setDeleteMCXboxBroadcastLog(boolean v) { deleteMCXboxBroadcastLog = v; }

        @SuppressWarnings("unchecked")
        static CleanupConfig fromMap(Map<String, Object> map) {
            CleanupConfig c = new CleanupConfig();
            c.deleteMCXboxBroadcastLog = (Boolean) map.getOrDefault("deleteMCXboxBroadcastLog", true);
            return c;
        }

        Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("deleteMCXboxBroadcastLog", deleteMCXboxBroadcastLog);
            return m;
        }
    }
}
