package com.playernotifier;

import com.playernotifier.compat.GameProfileCompat;
import com.playernotifier.compat.GuiCompat;
import com.playernotifier.config.RadarManager;

import eu.midnightdust.lib.config.MidnightConfig;

import com.playernotifier.config.ConfigWrapper;
import com.playernotifier.config.MidnightConfigInit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerNotifierClient implements ClientModInitializer {
	public static final String MOD_ID = "playernotifier";
	private final Set<Player> loadedPlayers = new HashSet<>();
	private int ticks = 0;
	private int timesPlayed = 0;
	private boolean alarming = false;

	@Override
	public void onInitializeClient() {
		MidnightConfig.init(MOD_ID, MidnightConfigInit.class);

		RadarBlacklistCommand.register();

		RadarManager.initialize();

		ClientTickEvents.END_LEVEL_TICK.register(world -> {
			if (!RadarManager.getRadarEnabled()) return;
			if (world != null) {
				Minecraft client = Minecraft.getInstance();
				if (client.level != null) {
					Set<Player> currentPlayers = new HashSet<>(client.level.players());
					for (Player player : currentPlayers) {
						UUID playerUUID = GameProfileCompat.getId(player.getGameProfile());
						if (("blacklist".equals(RadarManager.getRadarMode()) ? RadarManager.blacklist.isPlayerListed(playerUUID) : !RadarManager.whitelist.isPlayerListed(playerUUID)) || player.equals(client.player)) continue;

						if (!loadedPlayers.contains(player)) {
							String playerName = GameProfileCompat.getName(player.getGameProfile());
							Component playerDisplayName = player.getName();
							if (playerUUID == null || playerUUID.version() != 4) continue;
							if (playerName == null || playerName.replaceAll("§.", "").trim().isEmpty()) continue;
							if (ConfigWrapper.showChat()) {
								client.player.sendSystemMessage(Component.translatable("playernotifier.playerEnteredChunks", ConfigWrapper.showDisplayNames() ? playerDisplayName : playerName));
							}
							if (ConfigWrapper.showHUD()) {
								GuiCompat.setOverlayMessage(client.gui,
										Component.translatable("playernotifier.playerEnteredChunks", ConfigWrapper.showDisplayNames() ? playerDisplayName : playerName), false
								);
							}
							if (ConfigWrapper.playSound()) {
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
				if (client.player == null) {
					stopAlarm();
					return;
				}
				ticks++;
				if (ticks % ConfigWrapper.soundInterval() == 0) {
					client.player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), ConfigWrapper.soundVolume() / 100.0F, ConfigWrapper.soundPitch() / 100.0F);
					timesPlayed++;
				}
				if (timesPlayed == ConfigWrapper.timesToPlaySound()) stopAlarm();
			}
		});
	}

	public void alarm() {
		alarming = true;
	}

	public void stopAlarm() {
		alarming = false;
		timesPlayed = 0;
		ticks = 0;
	}
}