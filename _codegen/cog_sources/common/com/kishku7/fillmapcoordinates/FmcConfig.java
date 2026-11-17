package com.kishku7.fillmapcoordinates;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * The one config value, {@code enabled}, kept in {@code config/FillMapCoordinates/config.yml}.
 * Minecraft-free on purpose: the file is plain {@code key: value} lines, so no YAML library is needed.
 * The file is re-read when it changes on disk, so hand edits apply without a restart.
 */
public final class FmcConfig {
    private static final Path FILE = Paths.get("config", "FillMapCoordinates", "config.yml");
    private static final String KEY = "enabled";
    private static final String COMMENT = "# If enabled, the mod stores coordinate data on the map item when creating a new filled map";

    private static volatile boolean enabled = true;
    private static volatile long loadedStamp = Long.MIN_VALUE;

    private FmcConfig() {}

    /** Creates the file with defaults when it is missing, then loads it. */
    public static synchronized void init() {
        if (!Files.exists(FILE)) {
            write(true);
        }
        reload();
    }

    public static boolean isEnabled() {
        long stamp = stamp();
        if (stamp != loadedStamp) {
            reload();
        }
        return enabled;
    }

    public static synchronized void setEnabled(boolean value) {
        write(value);
        reload();
    }

    private static long stamp() {
        try {
            return Files.getLastModifiedTime(FILE).toMillis();
        } catch (IOException e) {
            return -1L;
        }
    }

    private static synchronized void reload() {
        loadedStamp = stamp();
        try {
            List<String> lines = Files.readAllLines(FILE, StandardCharsets.UTF_8);
            for (String line : lines) {
                String s = line.trim();
                if (s.startsWith(KEY + ":")) {
                    enabled = Boolean.parseBoolean(s.substring(KEY.length() + 1).trim());
                    return;
                }
            }
        } catch (IOException e) {
            // missing or unreadable file: keep the last known value
        }
    }

    private static void write(boolean value) {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, COMMENT + "\n" + KEY + ": " + value + "\n", StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[fillmapcoordinates] could not write " + FILE + ": " + e.getMessage());
        }
    }
}
