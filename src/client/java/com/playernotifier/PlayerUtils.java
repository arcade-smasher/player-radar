package com.playernotifier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerUtils {

    private static final Map<String, Optional<UUID>> uuidCache = new ConcurrentHashMap<>();

    public static CompletableFuture<UUID> getPlayerUUID(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }

        String normalized = playerName.toLowerCase();

        if (uuidCache.containsKey(normalized)) {
            return CompletableFuture.completedFuture(uuidCache.get(normalized).orElse(null));
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.getNetworkHandler() != null) {
            for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
                if (entry.getProfile().getName().equalsIgnoreCase(playerName)) {
                    UUID uuid = entry.getProfile().getId();
                    uuidCache.put(normalized, Optional.of(uuid));
                    return CompletableFuture.completedFuture(uuid);
                }
            }
        }

        return CompletableFuture.supplyAsync(() -> {
            UUID fetched = fetchUUIDFromMojang(playerName);
            uuidCache.put(normalized, Optional.ofNullable(fetched));
            return fetched;
        });
    }

    private static UUID fetchUUIDFromMojang(String playerName) {
        try {
            String encodedName = URLEncoder.encode(playerName, StandardCharsets.UTF_8);
            URL url = URI.create("https://api.mojang.com/users/profiles/minecraft/" + encodedName).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            int status = conn.getResponseCode();
            if (status == 200) {
                try (InputStream input = conn.getInputStream();
                     InputStreamReader isr = new InputStreamReader(input);
                     BufferedReader reader = new BufferedReader(isr)) {

                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    String id = json.get("id").getAsString();
                    return fromUndashedUUID(id);
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static UUID fromUndashedUUID(String id) {
        return UUID.fromString(id.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"
        ));
    }
}