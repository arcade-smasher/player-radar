// This was made in a rush, don't expect too much.

// TODO:
// - Add config system
package com.playernotifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
// import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import java.util.HashSet;
import java.util.Set;

// Removed notifier sound as it's too annoying. I'll add it back once I add a config system.

public class PlayerNotifierClient implements ClientModInitializer {
    private final Set<PlayerEntity> loadedPlayers = new HashSet<>();
    // private int ticks = 0;
    // private int timesPlayed = 0;
    // private boolean alarming = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            if (world != null) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.world != null) {
                    Set<PlayerEntity> currentPlayers = new HashSet<>(client.world.getPlayers());
                    for (PlayerEntity player : currentPlayers) {
                        if (!loadedPlayers.contains(player) && !player.equals(client.player)) {
                            client.player.sendMessage(player.getName().copy().append(" has entered your chunks!"), false);
                            // alarm();
                            client.inGameHud.setOverlayMessage(
                                Text.of(player.getName().getString() + " has entered your chunks!"), false
                            );
                        }
                    }
                    loadedPlayers.clear();
                    loadedPlayers.addAll(currentPlayers);
                }
            }
        });
        // ClientTickEvents.END_CLIENT_TICK.register(client -> {
        //     if (alarming) {
        //         ticks++;
        //         if (ticks % 4 == 0) {
        //             client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 1.0F, 0.8F);
        //             timesPlayed++;
        //         }
        //         if (timesPlayed == 4) {
        //             timesPlayed = 0;
        //             alarming = false;
        //             ticks = 0;
        //         }
        //     }
        // });
    }

    // public void alarm() {
    //     alarming = true;
    // }
}