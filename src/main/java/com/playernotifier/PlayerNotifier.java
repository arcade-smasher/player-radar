package com.playernotifier;

import com.playernotifier.config.MidnightConfigInit;

import net.fabricmc.api.ModInitializer;

import eu.midnightdust.lib.config.MidnightConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerNotifier implements ModInitializer {
	public static final String MOD_ID = "playernotifier";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        MidnightConfig.init(MOD_ID, MidnightConfigInit.class);
	}
}