package com.opti.account.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public final class SignInGate {
	private SignInGate() {}

	private static boolean bad(Minecraft mc) {
		return "SignIn".equals(mc.getUser().getName());
	}

	public static void init() {
		ScreenEvents.AFTER_INIT.register((mc, screen, w, h) -> {
			if (!bad(mc)) return;

			if (screen instanceof TitleScreen) {
				// Delete every widget on the title screen (vanilla, Realms, IAS icon, other mods)
				var widgets = Screens.getWidgets(screen);
				for (var widget : new ArrayList<>(widgets)) {
					widgets.remove(widget);
				}
				// Add the single Sign In button
				widgets.add(Button.builder(Component.literal("Sign In"), b -> openIas(mc, screen))
						.bounds(screen.width / 2 - 100, screen.height / 4 + 48, 200, 20)
						.build());
				return;
			}

			// Block world / server screens if reached another way (keybinds, other mods)
			String name = screen.getClass().getSimpleName();
			if (name.equals("SelectWorldScreen") || name.equals("JoinMultiplayerScreen")
					|| name.equals("RealmsMainScreen")) {
				mc.execute(() -> mc.gui.setScreen(null));
			}
		});

		// Last resort: a SignIn player must never stay inside a world
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (mc.level != null && bad(mc)) mc.stop();
		});
	}

	private static void openIas(Minecraft mc, Screen parent) {
		try {
			Class<?> k = Class.forName("ru.vidtu.ias.screen.AccountScreen");
			mc.gui.setScreen((Screen) k.getConstructor(Screen.class).newInstance(parent));
		} catch (Throwable t) {
			System.err.println("[SignInGate] Could not open IAS: " + t);
		}
	}
}
