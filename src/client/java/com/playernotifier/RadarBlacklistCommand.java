package com.playernotifier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.playernotifier.config.RadarManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class RadarBlacklistCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("radarlist")
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("player", StringArgumentType.string())
                        .executes(context -> {
                            String radarMode = RadarManager.getRadarMode();
                            String playerName = StringArgumentType.getString(context, "player");
                            PlayerUtils.getPlayerUUID(playerName).thenAccept(playerUUID -> {
                                if (playerUUID == null) {
                                    sendError("playernotifier." + radarMode + ".not_found", playerName);
                                    return;
                                }
                                if (RadarManager.currentList.isPlayerListed(playerUUID)) {
                                    sendFeedback("playernotifier." + radarMode + ".already", playerName);
                                } else {
                                    RadarManager.currentList.addPlayer(playerUUID);
                                    sendFeedback("playernotifier." + radarMode + ".added", playerName);
                                }
                            });
                            return Command.SINGLE_SUCCESS;
                        })

                    )
                )
                .then(ClientCommandManager.literal("remove")
                    .then(ClientCommandManager.argument("player", StringArgumentType.string())
                        .executes(context -> {
                            String radarMode = RadarManager.getRadarMode();
                            String playerName = StringArgumentType.getString(context, "player");
                            PlayerUtils.getPlayerUUID(playerName).thenAccept(playerUUID -> {
                                if (playerUUID == null) {
                                    sendError("playernotifier." + radarMode + ".not_found", playerName);
                                    return;
                                }
                                if (!RadarManager.currentList.isPlayerListed(playerUUID)) {
                                    sendFeedback("playernotifier." + radarMode + ".not", playerName);
                                } else {
                                    RadarManager.currentList.removePlayer(playerUUID);
                                    sendFeedback("playernotifier." + radarMode + ".removed", playerName);
                                }
                            });
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
                .then(ClientCommandManager.literal("uuid")
                    .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("playerUUID", StringArgumentType.string())
                            .executes(context -> {
                                String radarMode = RadarManager.getRadarMode();
                                String uuidString = StringArgumentType.getString(context, "playerUUID");
                                try {
                                    UUID playerUUID = UUID.fromString(uuidString);
                                    if (RadarManager.currentList.isPlayerListed(playerUUID)) {
                                        sendFeedback("playernotifier." + radarMode + ".uuid_already", playerUUID.toString());
                                    } else {
                                        RadarManager.currentList.addPlayer(playerUUID);
                                        sendFeedback("playernotifier." + radarMode + ".uuid_added", playerUUID.toString());
                                    }
                                } catch (IllegalArgumentException e) {
                                    sendError("playernotifier." + radarMode + ".invalid_uuid");
                                }
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                    )
                    .then(ClientCommandManager.literal("remove")
                        .then(ClientCommandManager.argument("playerUUID", StringArgumentType.string())
                            .executes(context -> {
                                String radarMode = RadarManager.getRadarMode();
                                String uuidString = StringArgumentType.getString(context, "playerUUID");
                                try {
                                    UUID playerUUID = UUID.fromString(uuidString);
                                    if (!RadarManager.currentList.isPlayerListed(playerUUID)) {
                                        sendFeedback("playernotifier." + radarMode + ".uuid_not", playerUUID.toString());
                                    } else {
                                        RadarManager.currentList.removePlayer(playerUUID);
                                        sendFeedback("playernotifier." + radarMode + ".uuid_removed", playerUUID.toString());
                                    }
                                } catch (IllegalArgumentException e) {
                                    sendError("playernotifier." + radarMode + ".invalid_uuid");
                                }
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                    )
                )
                .then(ClientCommandManager.literal("mode")
                    .then(ClientCommandManager.literal("blacklist")
                        .executes(context -> {
                            String radarMode = RadarManager.getRadarMode();
                            if (radarMode == "blacklist") {
                                sendFeedback("playernotifier.mode.already_blacklist");
                            } else {
                                RadarManager.setRadarMode("blacklist");
                                sendFeedback("playernotifier.mode.set.blacklist");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                    .then(ClientCommandManager.literal("whitelist")
                        .executes(context -> {
                            String radarMode = RadarManager.getRadarMode();
                            if (radarMode == "whitelist") {
                                sendFeedback("playernotifier.mode.already_whitelist");
                            } else {
                                RadarManager.setRadarMode("whitelist");
                                sendFeedback("playernotifier.mode.set.whitelist");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                    .executes(context -> {
                        String radarMode = RadarManager.getRadarMode();
                        sendFeedback("playernotifier.mode." + radarMode);
                        return Command.SINGLE_SUCCESS;
                    })
                )
            );
        });
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