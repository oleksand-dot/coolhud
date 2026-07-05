package com.coolhud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Holds all toggleable HUD module states and persists them to
 * .minecraft/config/coolhud.json so settings survive restarts.
 */
public class HudConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance()
			.getConfigDir()
			.resolve("coolhud.json");

	public static HudConfig INSTANCE = load();

	// --- toggleable modules ---
	public boolean showCoords = true;
	public boolean showFps = true;
	public boolean showPotionTimers = true;
	public boolean showArmorBar = true;
	public boolean showHungerBar = true;
	public boolean showBiome = false;

	// --- new features ---
	public boolean autoSprint = false;
	public boolean showAspectRatioBars = false;
	public int aspectRatioIndex = 0; // index into HudRenderer.ASPECT_RATIOS

	// --- movable HUD positions (top-left corner of each panel, in scaled GUI pixels) ---
	// Integer.MIN_VALUE means "not yet placed" -> HudRenderer computes and stores a sane default.
	public int coordsX = Integer.MIN_VALUE, coordsY = Integer.MIN_VALUE;
	public int fpsX = Integer.MIN_VALUE, fpsY = Integer.MIN_VALUE;
	public int potionX = Integer.MIN_VALUE, potionY = Integer.MIN_VALUE;
	public int statusX = Integer.MIN_VALUE, statusY = Integer.MIN_VALUE;

	// --- style ---
	public HudStyle style = HudStyle.GLASS;
	public HudPosition coordsPos = HudPosition.TOP_LEFT;
	public HudPosition fpsPos = HudPosition.TOP_RIGHT;

	public enum HudStyle { GLASS, MINIMAL, NEON }
	public enum HudPosition { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

	public void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException e) {
			System.err.println("[CoolHUD] Failed to save config: " + e.getMessage());
		}
	}

	private static HudConfig load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				HudConfig loaded = GSON.fromJson(reader, HudConfig.class);
				if (loaded != null) {
					return loaded;
				}
			} catch (IOException e) {
				System.err.println("[CoolHUD] Failed to load config, using defaults: " + e.getMessage());
			}
		}
		return new HudConfig();
	}
}
