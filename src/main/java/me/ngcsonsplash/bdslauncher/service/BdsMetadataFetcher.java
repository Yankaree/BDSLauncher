package me.ngcsonsplash.bdslauncher.service;

import me.ngcsonsplash.bdslauncher.model.BdsMetadata;
import me.ngcsonsplash.bdslauncher.model.VersionRegistry;
import me.ngcsonsplash.bdslauncher.util.Printer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class BdsMetadataFetcher {

    private static final String VERSIONS_URL =
            "https://raw.githubusercontent.com/EndstoneMC/bedrock-server-data/v2/versions.json";
    private static final String METADATA_BASE =
            "https://raw.githubusercontent.com/EndstoneMC/bedrock-server-data/v2/release";

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public BdsMetadataFetcher() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.mapper = new ObjectMapper();
    }

    public VersionRegistry fetchVersions() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VERSIONS_URL))
                .header("User-Agent", "BDSLauncher/1.0")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), VersionRegistry.class);
    }

    public BdsMetadata fetchMetadata(String version) throws Exception {
        String url = METADATA_BASE + "/" + version + "/metadata.json";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "BDSLauncher/1.0")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), BdsMetadata.class);
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
