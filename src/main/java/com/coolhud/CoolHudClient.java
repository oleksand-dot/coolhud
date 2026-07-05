package com.coolhud;

import com.coolhud.gui.ClickGuiScreen;
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
	private static KeyBinding openClickGuiKey;

	@Override
	public void onInitializeClient() {
		openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.coolhud.open_config",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_BRACKET,
				"category.coolhud.general"
		));

		openClickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.coolhud.open_clickgui",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				"category.coolhud.general"
		));

		HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.options.hudHidden) return;
			HudRenderer.render(drawContext, client);
		});

		ClientTickEvents.END_CLIENT_TICK.register(CoolHudClient::onTick);
	}

	private static void onTick(MinecraftClient client) {
		while (openConfigKey.wasPressed()) {
			if (client.currentScreen == null) {
				client.setScreen(new CoolHudConfigScreen(null));
			}
		}

		while (openClickGuiKey.wasPressed()) {
			if (client.currentScreen == null) {
				client.setScreen(new ClickGuiScreen(null));
			} else if (client.currentScreen instanceof ClickGuiScreen) {
				client.setScreen(null);
			}
		}

		applyAutoSprint(client);
	}

	/**
	 * Simulates holding the vanilla sprint key while moving forward, the
	 * same mechanism behind Minecraft's own "Toggle Sprint" option — so
	 * hunger cost, sneaking, and swimming all behave exactly as normal.
	 */
	private static void applyAutoSprint(MinecraftClient client) {
		if (!HudConfig.INSTANCE.autoSprint) return;
		if (client.player == null) return;

		boolean movingForward = client.options.forwardKey.isPressed();
		boolean canSprint = !client.player.isSneaking()
				&& !client.player.isSpectator()
				&& client.player.getHungerManager().getFoodLevel() > 6
				&& !client.player.hasVehicle();

		if (movingForward && canSprint) {
			client.options.sprintKey.setPressed(true);
		} else {
			// Let real input take back over; if the player is physically
			// holding sprint, the normal input handler re-presses it next frame.
			client.options.sprintKey.setPressed(false);
		}
	}
}
