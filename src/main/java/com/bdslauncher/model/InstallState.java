package com.bdslauncher.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InstallState {

    @JsonProperty("launcherVersion")
    private String launcherVersion = "1.0.0";

    @JsonProperty("autoUpdate")
    private boolean autoUpdate = false;

    @JsonProperty("bds")
    private BdsInfo bds = new BdsInfo();

    @JsonProperty("mcxboxbroadcast")
    private McxboxInfo mcxboxbroadcast = new McxboxInfo();

    @JsonProperty("lastUpdateCheck")
    private String lastUpdateCheck;

    @JsonProperty("tailscale")
    private TailscaleConfig tailscale = new TailscaleConfig();

    @JsonProperty("cleanup")
    private CleanupConfig cleanup = new CleanupConfig();

    public String getLauncherVersion() { return launcherVersion; }
    public void setLauncherVersion(String launcherVersion) { this.launcherVersion = launcherVersion; }

    public boolean isAutoUpdate() { return autoUpdate; }
    public void setAutoUpdate(boolean autoUpdate) { this.autoUpdate = autoUpdate; }

    public BdsInfo getBds() { return bds; }
    public void setBds(BdsInfo bds) { this.bds = bds; }

    public McxboxInfo getMcxboxbroadcast() { return mcxboxbroadcast; }
    public void setMcxboxbroadcast(McxboxInfo mcxboxbroadcast) { this.mcxboxbroadcast = mcxboxbroadcast; }

    public String getLastUpdateCheck() { return lastUpdateCheck; }
    public void setLastUpdateCheck(String lastUpdateCheck) { this.lastUpdateCheck = lastUpdateCheck; }

    public TailscaleConfig getTailscale() { return tailscale; }
    public void setTailscale(TailscaleConfig tailscale) { this.tailscale = tailscale; }

    public CleanupConfig getCleanup() { return cleanup; }
    public void setCleanup(CleanupConfig cleanup) { this.cleanup = cleanup; }

    public static class BdsInfo {

        @JsonProperty("installed")
        private boolean installed = false;

        @JsonProperty("version")
        private String version;

        @JsonProperty("platform")
        private String platform = "linux";

        @JsonProperty("installedAt")
        private String installedAt;

        public boolean isInstalled() { return installed; }
        public void setInstalled(boolean installed) { this.installed = installed; }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }

        public String getInstalledAt() { return installedAt; }
        public void setInstalledAt(String installedAt) { this.installedAt = installedAt; }
    }

    public static class McxboxInfo {

        @JsonProperty("installed")
        private boolean installed = false;

        @JsonProperty("version")
        private String version = "latest";

        public boolean isInstalled() { return installed; }
        public void setInstalled(boolean installed) { this.installed = installed; }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }

    public static class TailscaleConfig {

        @JsonProperty("enabled")
        private boolean enabled = false;

        @JsonProperty("version")
        private String version = "1.101.162";

        @JsonProperty("authKey")
        private String authKey;

        @JsonProperty("userspace")
        private boolean userspace = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public String getAuthKey() { return authKey; }
        public void setAuthKey(String authKey) { this.authKey = authKey; }

        public boolean isUserspace() { return userspace; }
        public void setUserspace(boolean userspace) { this.userspace = userspace; }
    }

    public static class CleanupConfig {

        @JsonProperty("deleteMCXboxBroadcastLog")
        private boolean deleteMCXboxBroadcastLog = true;

        public boolean isDeleteMCXboxBroadcastLog() { return deleteMCXboxBroadcastLog; }
        public void setDeleteMCXboxBroadcastLog(boolean deleteMCXboxBroadcastLog) { this.deleteMCXboxBroadcastLog = deleteMCXboxBroadcastLog; }
    }
}
