package com.playernotifier;

import com.mojang.authlib.GameProfile;

import java.util.UUID;
import java.util.function.Function;
import java.lang.reflect.Method;

public class GameProfileCompat {
    private static Function<GameProfile, UUID> idGetter;
    private static Function<GameProfile, String> nameGetter;

    @SuppressWarnings("unchecked")
    private static <T> Function<GameProfile, T> attemptGetter(String primary, String fallback) {
        try {
            Method m = GameProfile.class.getMethod(primary);
            return profile -> {
                try { return (T) m.invoke(profile); } catch (Exception e) { return null; }
            };
        } catch (NoSuchMethodException e) {
            try {
                Method m1 = GameProfile.class.getMethod(fallback);
                return profile -> {
                    try { return (T) m1.invoke(profile); } catch (Exception ex) { return null; }
                };
            } catch (Exception ex) {
                return profile -> null;
            }
        }
    }

    static {
        idGetter = attemptGetter("id", "getId");
        nameGetter = attemptGetter("name", "getName");
    }

    public static UUID getId(GameProfile profile) {
        return idGetter.apply(profile);
    }

    public static String getName(GameProfile profile) {
        return nameGetter.apply(profile);
    }
}