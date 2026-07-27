package me.ngcsonsplash.bdslauncher.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class VersionRegistry {

    @JsonProperty("release")
    private Release release;

    @JsonProperty("preview")
    private Preview preview;

    public Release getRelease() { return release; }
    public void setRelease(Release release) { this.release = release; }

    public Preview getPreview() { return preview; }
    public void setPreview(Preview preview) { this.preview = preview; }

    public static class Release {

        @JsonProperty("latest")
        private String latest;

        @JsonProperty("versions")
        private List<String> versions;

        public String getLatest() { return latest; }
        public void setLatest(String latest) { this.latest = latest; }

        public List<String> getVersions() { return versions; }
        public void setVersions(List<String> versions) { this.versions = versions; }
    }

    public static class Preview {

        @JsonProperty("latest")
        private String latest;

        @JsonProperty("versions")
        private List<String> versions;

        public String getLatest() { return latest; }
        public void setLatest(String latest) { this.latest = latest; }

        public List<String> getVersions() { return versions; }
        public void setVersions(List<String> versions) { this.versions = versions; }
    }
}
