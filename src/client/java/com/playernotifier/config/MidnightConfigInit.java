package com.playernotifier.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class MidnightConfigInit extends MidnightConfig {
    public static final String ALL = "all";

    @Comment(category = ALL, centered = true) public static Comment visuals;

    @Entry(category = ALL) public static boolean showChat = true;
    @Entry(category = ALL) public static boolean showHUD = true;

    @Comment(category = ALL, centered = true) public static Comment audio;

    @Entry(category = ALL) public static boolean playSound = false;
    @Entry(category = ALL, min = 0) public static int timesToPlaySound = 3;
    @Entry(category = ALL, min = 0, max = 100) public static int soundInterval = 4;
    @Entry(category = ALL, min = 0) public static int soundVolume = 100;
    @Entry(category = ALL, min = 0) public static int soundPitch = 80;
}