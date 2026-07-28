package me.ngcsonsplash.bdslauncher.model;

public class InstallState {

    private String launcherVersion = "1.0.0";
    private boolean autoUpdate = false;
    private String lastUpdateCheck;
    private final BdsInfo bds = new BdsInfo();
    private final McxboxInfo mcxboxbroadcast = new McxboxInfo();
    private final TailscaleConfig tailscale = new TailscaleConfig();
    private final CleanupConfig cleanup = new CleanupConfig();

    public String getLauncherVersion() { return launcherVersion; }
    public void setLauncherVersion(String v) { launcherVersion = v; }
    public boolean isAutoUpdate() { return autoUpdate; }
    public void setAutoUpdate(boolean v) { autoUpdate = v; }
    public String getLastUpdateCheck() { return lastUpdateCheck; }
    public void setLastUpdateCheck(String v) { lastUpdateCheck = v; }
    public BdsInfo getBds() { return bds; }
    public McxboxInfo getMcxboxbroadcast() { return mcxboxbroadcast; }
    public TailscaleConfig getTailscale() { return tailscale; }
    public CleanupConfig getCleanup() { return cleanup; }

    public static class BdsInfo {
        private boolean installed;
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
    }

    public static class McxboxInfo {
        private boolean installed;
        private String version = "latest";

        public boolean isInstalled() { return installed; }
        public void setInstalled(boolean v) { installed = v; }
        public String getVersion() { return version; }
        public void setVersion(String v) { version = v; }
    }

    public static class TailscaleConfig {
        private boolean enabled;
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
    }

    public static class CleanupConfig {
        private boolean deleteMCXboxBroadcastLog = true;

        public boolean isDeleteMCXboxBroadcastLog() { return deleteMCXboxBroadcastLog; }
        public void setDeleteMCXboxBroadcastLog(boolean v) { deleteMCXboxBroadcastLog = v; }
    }
}
