package com.coolhud.gui;

import com.coolhud.HudConfig;
import com.coolhud.hud.HudRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Simple vanilla-widget config screen for toggling HUD modules on/off,
 * cycling the cinematic aspect-ratio bars, flipping auto-sprint, and
 * jumping into the drag-to-move HUD layout editor.
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
		int colWidth = 150;
		int gap = 10;
		int leftX = centerX - colWidth - gap / 2;
		int rightX = centerX + gap / 2;
		int rowHeight = 22;
		int startY = this.height / 2 - 110;

		// left column
		addToggle(leftX, startY, colWidth, "Coordinates", () -> cfg.showCoords, v -> cfg.showCoords = v);
		addToggle(leftX, startY + rowHeight, colWidth, "FPS Counter", () -> cfg.showFps, v -> cfg.showFps = v);
		addToggle(leftX, startY + rowHeight * 2, colWidth, "Potion Timers", () -> cfg.showPotionTimers, v -> cfg.showPotionTimers = v);
		addToggle(leftX, startY + rowHeight * 3, colWidth, "Biome Display", () -> cfg.showBiome, v -> cfg.showBiome = v);

		// right column
		addToggle(rightX, startY, colWidth, "Armor Bar", () -> cfg.showArmorBar, v -> cfg.showArmorBar = v);
		addToggle(rightX, startY + rowHeight, colWidth, "Hunger Bar", () -> cfg.showHungerBar, v -> cfg.showHungerBar = v);
		addToggle(rightX, startY + rowHeight * 2, colWidth, "Auto Sprint", () -> cfg.autoSprint, v -> cfg.autoSprint = v);
		addToggle(rightX, startY + rowHeight * 3, colWidth, "Cinematic Bars", () -> cfg.showAspectRatioBars, v -> cfg.showAspectRatioBars = v);

		int fullWidth = colWidth * 2 + gap;
		int belowToggles = startY + rowHeight * 4 + 8;

		// aspect ratio cycle button
		this.addDrawableChild(ButtonWidget.builder(
				aspectRatioText(),
				button -> {
					cfg.aspectRatioIndex = (cfg.aspectRatioIndex + 1) % HudRenderer.ASPECT_RATIOS.length;
					button.setMessage(aspectRatioText());
				}
		).dimensions(centerX - fullWidth / 2, belowToggles, fullWidth, 20).build());

		// HUD layout editor
		this.addDrawableChild(ButtonWidget.builder(Text.literal("Move HUD Panels..."), button -> {
			cfg.save();
			this.client.setScreen(new HudEditScreen(this));
		}).dimensions(centerX - fullWidth / 2, belowToggles + rowHeight, fullWidth, 20).build());

		// done
		this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
			cfg.save();
			this.close();
		}).dimensions(centerX - fullWidth / 2, belowToggles + rowHeight * 2 + 10, fullWidth, 20).build());
	}

	private Text aspectRatioText() {
		String label = HudRenderer.ASPECT_RATIO_LABELS[cfg.aspectRatioIndex];
		return Text.literal("Cinematic Ratio: " + label);
	}

	private void addToggle(int x, int y, int width, String label,
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
		).dimensions(x, y, width, 20).build();
		this.addDrawableChild(holder[0]);
	}

	private Text toggleText(String label, boolean on) {
		String state = on ? "ON" : "OFF";
		return Text.literal(label + ": " + state);
	}

	@Override
	public void close() {
		cfg.save();
		this.client.setScreen(parent);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
