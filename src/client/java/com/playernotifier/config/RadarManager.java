package com.playernotifier.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RadarManager {
    private static final File SETTINGS_FILE = new File("config/playernotifier_settings.json"); // Command in-line settings, different from MidnightLib configs as they are not optional unlike MidnightLib
    private static final File BLACKLIST_FILE = new File("config/playernotifier_blacklist.json");
    private static final File WHITELIST_FILE = new File("config/playernotifier_whitelist.json");
    private static final Gson GSON = new Gson();

    private static Set<String> blacklistedPlayers = Collections.synchronizedSet(new HashSet<>());
    private static Set<String> whitelistedPlayers = Collections.synchronizedSet(new HashSet<>());

    public static final RadarList blacklist = new RadarList(BLACKLIST_FILE, blacklistedPlayers);
    public static final RadarList whitelist = new RadarList(WHITELIST_FILE, whitelistedPlayers);

    private static Settings settings = new Settings();
    public static RadarList currentList = blacklist;

    public static void initialize() {
        loadLists();
        loadSettings();
    }

    public static void loadLists() {
        blacklist.loadList();
        whitelist.loadList();
    }

    public static void saveLists() {
        blacklist.saveList();
        whitelist.saveList();
    }

    public static void loadSettings() {
        if (SETTINGS_FILE.exists()) {
            try (FileReader reader = new FileReader(SETTINGS_FILE)) {
                Settings loaded = GSON.fromJson(reader, Settings.class);
                if (loaded != null) {
                    settings = loaded;
                    currentList = "blacklist".equals(settings.radarMode) ? blacklist : whitelist;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveSettings() {
        SETTINGS_FILE.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(SETTINGS_FILE)) {
            GSON.toJson(settings, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setRadarMode(String radarMode) {
        if ("blacklist".equals(radarMode) || "whitelist".equals(radarMode)) {
            currentList = "blacklist".equals(radarMode) ? blacklist : whitelist;
            settings.radarMode = radarMode;
        }
    }

    public static String getRadarMode() {
        return settings.radarMode;
    }

    private static class Settings {
        String radarMode = "blacklist";
    }

    public static class RadarList {
        private File file;
        private Set<String> list;

        RadarList(final File file, final Set<String> list) {
            this.file = file;
            this.list = list;
        }

        public void addPlayer(UUID playerUUID) {
            list.add(playerUUID.toString());
            saveList();
        }

        public void removePlayer(UUID playerUUID) {
            list.remove(playerUUID.toString());
            saveList();
        }

        public void loadList() {
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    Type type = new TypeToken<Set<String>>() {}.getType();
                    Set<String> loaded = GSON.fromJson(reader, type);
                    list.clear();
                    if (loaded != null) {
                        list.addAll(loaded);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    list.clear();
                }
            }
        }

        public void saveList() {
            ensureDirectory();
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(list, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean isPlayerListed(UUID playerUUID) {
            return list.contains(playerUUID.toString());
        }

        private void ensureDirectory() {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
        }
    }
}