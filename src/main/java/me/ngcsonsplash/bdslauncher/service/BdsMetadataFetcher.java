package me.ngcsonsplash.bdslauncher.service;

import me.ngcsonsplash.bdslauncher.model.BdsMetadata;
import me.ngcsonsplash.bdslauncher.model.VersionRegistry;
import me.ngcsonsplash.bdslauncher.util.CurlDownload;
import me.ngcsonsplash.bdslauncher.util.Printer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class BdsMetadataFetcher {

    private static final String VERSIONS_URL =
            "https://raw.githubusercontent.com/EndstoneMC/bedrock-server-data/v2/versions.json";
    private static final String METADATA_BASE =
            "https://raw.githubusercontent.com/EndstoneMC/bedrock-server-data/v2/release";

    private final ObjectMapper mapper;

    public BdsMetadataFetcher() {
        this.mapper = new ObjectMapper();
    }

    public VersionRegistry fetchVersions() throws Exception {
        String json = CurlDownload.fetch(VERSIONS_URL);
        return mapper.readValue(json, VersionRegistry.class);
    }

    public BdsMetadata fetchMetadata(String version) throws Exception {
        String url = METADATA_BASE + "/" + version + "/metadata.json";
        String json = CurlDownload.fetch(url);
        return mapper.readValue(json, BdsMetadata.class);
    }

    public String getLatestVersion() throws Exception {
        VersionRegistry registry = fetchVersions();
        return registry.getRelease().getLatest();
    }

    public List<String> getAvailableVersions() throws Exception {
        VersionRegistry registry = fetchVersions();
        return registry.getRelease().getVersions();
    }
}
