package com.playernotifier;

import com.playernotifier.config.RadarManager;
import com.playernotifier.config.ConfigWrapper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerNotifierClient implements ClientModInitializer {
    private final Set<PlayerEntity> loadedPlayers = new HashSet<>();
    private int ticks = 0;
    private int timesPlayed = 0;
    private boolean alarming = false;

    @Override
    public void onInitializeClient() {
        RadarBlacklistCommand.register();

        RadarManager.initialize();

        ClientTickEvents.END_WORLD_TICK.register(world -> {
            if (world != null) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.world != null) {
                    Set<PlayerEntity> currentPlayers = new HashSet<>(client.world.getPlayers());
                    for (PlayerEntity player : currentPlayers) {
                        UUID playerUUID = player.getUuid();
                        if (("blacklist".equals(RadarManager.getRadarMode()) ? RadarManager.blacklist.isPlayerListed(playerUUID) : !RadarManager.whitelist.isPlayerListed(playerUUID)) || player.equals(client.player)) {
                            continue;
                        }
                        if (!loadedPlayers.contains(player)) {
                            if (ConfigWrapper.showChat() == true) {
                                client.player.sendMessage(Text.translatable("playernotifier.playerEnteredChunks", player.getName().getString()), false);
                            }
                            if (ConfigWrapper.showHUD() == true) {
                                client.inGameHud.setOverlayMessage(
                                    Text.translatable("playernotifier.playerEnteredChunks", player.getName().getString()), false
                                );
                            }
                            if (ConfigWrapper.playSound() == true) {
                                alarm();
                            }
                        }
                    }
                    loadedPlayers.clear();
                    loadedPlayers.addAll(currentPlayers);
                }
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (alarming) {
                ticks++;
                if (ticks % ConfigWrapper.soundInterval() == 0) {
                    client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), ConfigWrapper.soundVolume() / 100.0F, ConfigWrapper.soundPitch() / 100.0F);
                    timesPlayed++;
                }
                if (timesPlayed == ConfigWrapper.timesToPlaySound()) {
                    timesPlayed = 0;
                    alarming = false;
                    ticks = 0;
                }
            }
        });
    }

    public void alarm() {
        alarming = true;
    }
}