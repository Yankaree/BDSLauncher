package com.bdslauncher.util;

import java.util.Scanner;

public class Printer {

    private static final Scanner scanner = new Scanner(System.in);

    public static void printBanner() {
        System.out.println("================================");
        System.out.println("       BDS Launcher v1.0.0");
        System.out.println("================================");
        System.out.println();
    }

    public static void printStatus(String label, boolean ok) {
        System.out.printf("  %s: %s%n", label, ok ? "OK" : "FAIL");
    }

    public static void printInfo(String label, String value) {
        System.out.printf("  %s: %s%n", label, value);
    }

    public static void printSection(String title) {
        System.out.println();
        System.out.println("--- " + title + " ---");
        System.out.println();
    }

    public static void printSuccess(String message) {
        System.out.println("[OK] " + message);
    }

    public static void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    public static void printWarning(String message) {
        System.out.println("[WARN] " + message);
    }

    public static void printUpdate(String current, String latest) {
        System.out.println("[UPDATE] BDS Update Available");
        System.out.println("  Current: " + current);
        System.out.println("  New:     " + latest);
    }

    public static void println() {
        System.out.println();
    }

    public static void println(String text) {
        System.out.println(text);
    }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static boolean readConfirm(String prompt) {
        System.out.print(prompt + " (Y/N): ");
        String input = scanner.nextLine().trim().toUpperCase();
        return input.equals("Y") || input.equals("YES");
    }

    public static int readChoice(String prompt, int max) {
        System.out.print(prompt);
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice >= 1 && choice <= max) {
                return choice;
            }
        } catch (NumberFormatException ignored) {
        }
        return -1;
    }
}
