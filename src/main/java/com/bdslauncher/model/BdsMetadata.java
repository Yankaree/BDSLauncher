package com.bdslauncher.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BdsMetadata {

    @JsonProperty("version")
    private String version;

    @JsonProperty("binary")
    private Binary binary;

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Binary getBinary() { return binary; }
    public void setBinary(Binary binary) { this.binary = binary; }

    public static class Binary {

        @JsonProperty("windows")
        private PlatformInfo windows;

        @JsonProperty("linux")
        private PlatformInfo linux;

        public PlatformInfo getWindows() { return windows; }
        public void setWindows(PlatformInfo windows) { this.windows = windows; }

        public PlatformInfo getLinux() { return linux; }
        public void setLinux(PlatformInfo linux) { this.linux = linux; }
    }

    public static class PlatformInfo {

        @JsonProperty("url")
        private String url;

        @JsonProperty("sha256")
        private String sha256;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getSha256() { return sha256; }
        public void setSha256(String sha256) { this.sha256 = sha256; }
    }
}
