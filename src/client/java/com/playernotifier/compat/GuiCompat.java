package com.playernotifier.compat;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

// In newer versions, gui.setOverlayMessage was moved to gui.hud.setOverlayMessage
@SuppressWarnings("JavaReflectionMemberAccess")
public final class GuiCompat {

	private static final MethodHandle SET_OVERLAY_MESSAGE;

	static {
		MethodHandle handle = null;

		try {
			Class<?> hudClass = Class.forName("net.minecraft.client.gui.Hud");
			Method setOverlay = hudClass.getMethod("setOverlayMessage", Component.class, boolean.class);

			MethodHandles.Lookup lookup = MethodHandles.lookup();
			Field hudField = Gui.class.getField("hud");
			MethodHandle getHudHandle = lookup.unreflectGetter(hudField);
			MethodHandle setOverlayHandle = lookup.unreflect(setOverlay);

			handle = MethodHandles.collectArguments(setOverlayHandle, 0, getHudHandle);
		} catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
			try {
				Method setOverlay = Gui.class.getMethod("setOverlayMessage", Component.class, boolean.class);
				handle = MethodHandles.lookup().unreflect(setOverlay);
			} catch (NoSuchMethodException | IllegalAccessException ex) {
				throw new ExceptionInInitializerError(ex);
			}
		}

		SET_OVERLAY_MESSAGE = handle;
	}

	public static void setOverlayMessage(Gui gui, Component message, boolean animate) {
		try {
			SET_OVERLAY_MESSAGE.invokeExact(gui, message, animate);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to invoke setOverlayMessage via compat layer", t);
		}
	}
}