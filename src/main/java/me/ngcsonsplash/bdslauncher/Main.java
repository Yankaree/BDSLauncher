package me.ngcsonsplash.bdslauncher;

import me.ngcsonsplash.bdslauncher.model.InstallState;
import me.ngcsonsplash.bdslauncher.service.*;
import me.ngcsonsplash.bdslauncher.util.Printer;

public class Main {

    public static void main(String[] args) {
        Printer.printBanner();

        try {
            if (!EnvironmentCheck.check()) {
                Printer.printError("Environment check failed. Exiting.");
                System.exit(1);
            }

            InstallState state = InstallStateManager.load();
            InstallStateManager.validate(state);

            TailscaleManager tailscaleManager = new TailscaleManager();
            tailscaleManager.setup(state);

            McxboxBroadcastManager mcxboxManager = new McxboxBroadcastManager();
            mcxboxManager.setup(state);

            if (!state.getBds().isInstalled()) {
                BdsDownloadManager downloadManager = new BdsDownloadManager();
                downloadManager.installFirstTime(state);
            }

            UpdateManager updateManager = new UpdateManager();
            updateManager.checkAndUpdate(state);

            WorldPackManager worldPackManager = new WorldPackManager();
            worldPackManager.synchronize();

            ProcessManager processManager = new ProcessManager();

            boolean deleteMcxboxLog = state.getCleanup().isDeleteMCXboxBroadcastLog();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                processManager.shutdown(deleteMcxboxLog);
                tailscaleManager.stop();
            }));

            ConsoleInputManager consoleInputManager = new ConsoleInputManager();

            processManager.startMcxboxBroadcast();
            processManager.startBds(consoleInputManager);

        } catch (Exception e) {
            Printer.printError("Fatal: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
