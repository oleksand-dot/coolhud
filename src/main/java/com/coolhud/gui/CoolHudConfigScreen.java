package com.coolhud.gui;

import com.coolhud.HudConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Simple vanilla-widget config screen for toggling HUD modules on/off.
 * Opened with the configurable "CoolHUD config" keybind (default: ']').
 */
public class CoolHudConfigScreen extends Screen {

	private final Screen parent;
	private final HudConfig cfg = HudConfig.INSTANCE;

	public CoolHudConfigScreen(Screen parent) {
		super(Text.literal("CoolHUD Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;
		int startY = this.height / 2 - 90;
		int rowHeight = 22;
		int buttonWidth = 200;

		addToggle(centerX, startY, buttonWidth, "Coordinates", () -> cfg.showCoords,
				v -> cfg.showCoords = v);
		addToggle(centerX, startY + rowHeight, buttonWidth, "FPS Counter", () -> cfg.showFps,
				v -> cfg.showFps = v);
		addToggle(centerX, startY + rowHeight * 2, buttonWidth, "Potion Timers", () -> cfg.showPotionTimers,
				v -> cfg.showPotionTimers = v);
		addToggle(centerX, startY + rowHeight * 3, buttonWidth, "Armor Bar", () -> cfg.showArmorBar,
				v -> cfg.showArmorBar = v);
		addToggle(centerX, startY + rowHeight * 4, buttonWidth, "Hunger Bar", () -> cfg.showHungerBar,
				v -> cfg.showHungerBar = v);
		addToggle(centerX, startY + rowHeight * 5, buttonWidth, "Biome Display", () -> cfg.showBiome,
				v -> cfg.showBiome = v);

		this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
			cfg.save();
			this.close();
		}).dimensions(centerX - buttonWidth / 2, startY + rowHeight * 6 + 10, buttonWidth, 20).build());
	}

	private void addToggle(int centerX, int y, int width, String label,
							java.util.function.Supplier<Boolean> getter,
							java.util.function.Consumer<Boolean> setter) {
		ButtonWidget[] holder = new ButtonWidget[1];
		holder[0] = ButtonWidget.builder(
				toggleText(label, getter.get()),
				button -> {
					boolean newValue = !getter.get();
					setter.accept(newValue);
					button.setMessage(toggleText(label, newValue));
				}
		).dimensions(centerX - width / 2, y, width, 20).build();
		this.addDrawableChild(holder[0]);
	}

	private Text toggleText(String label, boolean on) {
		String state = on ? "ON" : "OFF";
		return Text.literal(label + ": " + state);
	}

	@Override
	public void close() {
		this.client.setScreen(parent);
	}

	@Override
	public boolean shouldPauseGame() {
		return false;
	}
}

