package com.coolhud;

import com.coolhud.gui.CoolHudConfigScreen;
import com.coolhud.hud.HudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class CoolHudClient implements ClientModInitializer {

	private static KeyBinding openConfigKey;

	@Override
	public void onInitializeClient() {
		HudConfig.INSTANCE = HudConfig.INSTANCE; // ensure loaded

		openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.coolhud.open_config",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_BRACKET,
				"category.coolhud.general"
		));

		HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.options.hudHidden) return;
			HudRenderer.render(drawContext, client);
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfigKey.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new CoolHudConfigScreen(null));
				}
			}
		});
	}
}
