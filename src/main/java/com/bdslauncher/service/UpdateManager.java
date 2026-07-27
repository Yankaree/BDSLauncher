package com.bdslauncher.service;

import com.bdslauncher.model.InstallState;
import com.bdslauncher.util.Printer;

public class UpdateManager {

    private final BdsMetadataFetcher metadataFetcher;
    private final BdsDownloadManager downloadManager;

    public UpdateManager() {
        this.metadataFetcher = new BdsMetadataFetcher();
        this.downloadManager = new BdsDownloadManager();
    }

    public void checkAndUpdate(InstallState state) throws Exception {
        Printer.printSection("Update Check");

        if (!state.getBds().isInstalled()) {
            Printer.printInfo("BDS", "Not installed, skipping update check");
            return;
        }

        String currentVersion = state.getBds().getVersion();
        String latestVersion = metadataFetcher.getLatestVersion();

        state.setLastUpdateCheck(java.time.Instant.now().toString());
        InstallStateManager.save(state);

        if (currentVersion.equals(latestVersion)) {
            Printer.printSuccess("No update available (v" + currentVersion + ")");
            return;
        }

        Printer.printUpdate(currentVersion, latestVersion);

        if (state.isAutoUpdate()) {
            Printer.printInfo("Auto-update", "enabled, updating automatically...");
            downloadManager.updateToVersion(latestVersion, state);
        } else {
            boolean update = Printer.readConfirm("Update now?");
            if (update) {
                downloadManager.updateToVersion(latestVersion, state);
            } else {
                Printer.printInfo("Update", "Skipped, starting current version");
            }
        }
    }
}
