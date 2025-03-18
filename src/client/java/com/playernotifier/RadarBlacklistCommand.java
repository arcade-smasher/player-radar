package com.playernotifier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.playernotifier.config.BlacklistManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class RadarBlacklistCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("radarblacklist")
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("player", StringArgumentType.string())
                        .executes(context -> {
                            String playerName = StringArgumentType.getString(context, "player");
                            UUID playerUUID = getPlayerUUID(playerName);

                            if (playerUUID != null) {
                                if (BlacklistManager.isPlayerIgnored(playerUUID)) {
                                    sendFeedback("playernotifier.blacklist.already_blacklisted", playerName);
                                } else {
                                    BlacklistManager.addPlayer(playerUUID);
                                    sendFeedback("playernotifier.blacklist.added", playerName);
                                }
                            } else {
                                sendFeedback("playernotifier.blacklist.not_found", playerName);
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
                .then(ClientCommandManager.literal("remove")
                    .then(ClientCommandManager.argument("player", StringArgumentType.string())
                        .executes(context -> {
                            String playerName = StringArgumentType.getString(context, "player");
                            UUID playerUUID = getPlayerUUID(playerName);

                            if (playerUUID != null) {
                                if (!BlacklistManager.isPlayerIgnored(playerUUID)) {
                                    sendFeedback("playernotifier.blacklist.not_blacklisted", playerName);
                                } else {
                                    BlacklistManager.removePlayer(playerUUID);
                                    sendFeedback("playernotifier.blacklist.removed", playerName);
                                }
                            } else {
                                sendFeedback("playernotifier.blacklist.not_found", playerName);
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
                .then(ClientCommandManager.literal("uuid")
                    .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("playerUUID", StringArgumentType.string())
                            .executes(context -> {
                                String uuidString = StringArgumentType.getString(context, "playerUUID");
                                try {
                                    UUID playerUUID = UUID.fromString(uuidString);
                                    if (BlacklistManager.isPlayerIgnored(playerUUID)) {
                                        sendFeedback("playernotifier.blacklist.uuid_already_blacklisted", playerUUID.toString());
                                    } else {
                                        BlacklistManager.addPlayer(playerUUID);
                                        sendFeedback("playernotifier.blacklist.uuid_added", playerUUID.toString());
                                    }
                                } catch (IllegalArgumentException e) {
                                    sendError("playernotifier.blacklist.invalid_uuid");
                                }
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                    )
                    .then(ClientCommandManager.literal("remove")
                        .then(ClientCommandManager.argument("playerUUID", StringArgumentType.string())
                            .executes(context -> {
                                String uuidString = StringArgumentType.getString(context, "playerUUID");
                                try {
                                    UUID playerUUID = UUID.fromString(uuidString);
                                    if (!BlacklistManager.isPlayerIgnored(playerUUID)) {
                                        sendFeedback("playernotifier.blacklist.uuid_not_blacklisted", playerUUID.toString());
                                    } else {
                                        BlacklistManager.removePlayer(playerUUID);
                                        sendFeedback("playernotifier.blacklist.uuid_removed", playerUUID.toString());
                                    }
                                } catch (IllegalArgumentException e) {
                                    sendError("playernotifier.blacklist.invalid_uuid");
                                }
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                    )
                )
            );
        });
    }

    private static UUID getPlayerUUID(String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.getNetworkHandler() != null) {
            for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
                if (entry.getProfile().getName().equalsIgnoreCase(playerName)) {
                    return entry.getProfile().getId();
                }
            }
        }
        return null;
    }

    private static void sendFeedback(String translationKey, Object... args) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.translatable(translationKey, args), false);
        }
    }

    private static void sendError(String translationKey, Object... args) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.translatable(translationKey, args).formatted(Formatting.RED), false);
        }
    }
}