package com.playernotifier.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BlacklistManager {
    private static final File FILE = new File("config/playernotifier_blacklist.json");
    private static final Gson GSON = new Gson();
    private static Set<String> ignoredPlayers = new HashSet<>();

    public static void loadBlacklist() {
        if (!FILE.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(FILE)) {
            Type type = new TypeToken<Set<String>>() {}.getType();
            ignoredPlayers = GSON.fromJson(reader, type);
            if (ignoredPlayers == null) {
                ignoredPlayers = new HashSet<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveBlacklist() {
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(ignoredPlayers, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addPlayer(UUID playerUUID) {
        ignoredPlayers.add(playerUUID.toString());
        saveBlacklist();
    }

    public static void removePlayer(UUID playerUUID) {
        ignoredPlayers.remove(playerUUID.toString());
        saveBlacklist();
    }

    public static boolean isPlayerIgnored(UUID playerUUID) {
        return ignoredPlayers.contains(playerUUID.toString());
    }
}