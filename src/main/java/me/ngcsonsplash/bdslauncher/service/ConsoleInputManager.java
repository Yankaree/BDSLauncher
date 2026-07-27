package me.ngcsonsplash.bdslauncher.service;

import me.ngcsonsplash.bdslauncher.util.Printer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsoleInputManager {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread inputThread;
    private OutputStream bdsStdin;

    public void attachBDSInput(Process bdsProcess) {
        if (bdsProcess == null || !bdsProcess.isAlive()) {
            Printer.printWarning("Cannot attach console input: BDS not running");
            return;
        }

        this.bdsStdin = bdsProcess.getOutputStream();
        running.set(true);

        inputThread = Thread.ofVirtual().name("console-input").start(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                while (running.get()) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    if (running.get() && bdsStdin != null) {
                        synchronized (bdsStdin) {
                            bdsStdin.write((line + "\n").getBytes());
                            bdsStdin.flush();
                        }
                    }
                }
            } catch (IOException e) {
                if (running.get()) {
                    Printer.printError("Console input error: " + e.getMessage());
                }
            }
        });
    }

    public void forwardCommand(String command) {
        sendCommand(command);
    }

    public void sendCommand(String command) {
        if (bdsStdin != null) {
            try {
                synchronized (bdsStdin) {
                    bdsStdin.write((command + "\n").getBytes());
                    bdsStdin.flush();
                }
            } catch (IOException e) {
                if (running.get()) {
                    Printer.printError("Failed to send command: " + e.getMessage());
                }
            }
        }
    }

    public void detach() {
        running.set(false);
        if (inputThread != null && inputThread.isAlive()) {
            inputThread.interrupt();
        }
        bdsStdin = null;
    }
}
