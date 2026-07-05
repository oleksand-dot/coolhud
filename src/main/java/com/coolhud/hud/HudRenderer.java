package com.coolhud.hud;

import com.coolhud.HudConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class HudRenderer {

	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int MUTED_COLOR = 0xFFB8C4D9;

	/** width / height ratios cycled through by the aspect-ratio keybind / config screen. */
	public static final float[] ASPECT_RATIOS = {16f / 9f, 21f / 9f, 2.35f, 4f / 3f};
	public static final String[] ASPECT_RATIO_LABELS = {"16:9", "21:9", "2.35:1", "4:3"};

	public static void render(DrawContext ctx, MinecraftClient client) {
		if (client.player == null) return;
		HudConfig cfg = HudConfig.INSTANCE;

		if (cfg.showAspectRatioBars) {
			renderAspectRatioBars(ctx, client, cfg);
		}
		if (cfg.showCoords || cfg.showBiome) {
			renderInfoPanel(ctx, client, cfg);
		}
		if (cfg.showFps) {
			renderFpsPanel(ctx, client, cfg);
		}
		if (cfg.showPotionTimers) {
			renderPotionTimers(ctx, client, cfg);
		}
		if (cfg.showArmorBar || cfg.showHungerBar) {
			renderStatusBars(ctx, client, cfg);
		}
	}

	private static void renderAspectRatioBars(DrawContext ctx, MinecraftClient client, HudConfig cfg) {
		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();
		float targetRatio = ASPECT_RATIOS[MathHelper.clamp(cfg.aspectRatioIndex, 0, ASPECT_RATIOS.length - 1)];
		float currentRatio = (float) screenWidth / (float) screenHeight;

		if (currentRatio > targetRatio) {
			// screen is wider than target -> letterbox top & bottom
			int targetHeight = Math.round(screenWidth / targetRatio);
			int barHeight = Math.max(0, (screenHeight - targetHeight) / 2);
			ctx.fill(0, 0, screenWidth, barHeight, 0xFF000000);
			ctx.fill(0, screenHeight - barHeight, screenWidth, screenHeight, 0xFF000000);
		} else if (currentRatio < targetRatio) {
			// screen is narrower than target -> pillarbox left & right
			int targetWidth = Math.round(screenHeight * targetRatio);
			int barWidth = Math.max(0, (screenWidth - targetWidth) / 2);
			ctx.fill(0, 0, barWidth, screenHeight, 0xFF000000);
			ctx.fill(screenWidth - barWidth, 0, screenWidth, screenHeight, 0xFF000000);
		}
	}

	private static void renderInfoPanel(DrawContext ctx, MinecraftClient client, HudConfig cfg) {
		ClientPlayerEntity player = client.player;
		List<String> lines = new ArrayList<>();

		if (cfg.showCoords) {
			BlockPos pos = player.getBlockPos();
			lines.add(String.format("X %d  Y %d  Z %d", pos.getX(), pos.getY(), pos.getZ()));
		}
		if (cfg.showBiome && client.world != null) {
			var biomeKey = client.world.getBiome(player.getBlockPos()).getKey();
			String biomeName = biomeKey.map(k -> k.getValue().getPath()).orElse("unknown");
			lines.add(prettifyName(biomeName));
		}

		int width = 10 + maxTextWidth(client, lines);
		int height = 8 + lines.size() * 10;

		if (cfg.coordsX == Integer.MIN_VALUE) { cfg.coordsX = 6; cfg.coordsY = 6; }
		int[] pos = clamp(client, cfg.coordsX, cfg.coordsY, width, height);

		GlassPanel.draw(ctx, pos[0], pos[1], width, height);
		int ty = pos[1] + 4;
		for (String line : lines) {
			ctx.drawText(client.textRenderer, Text.literal(line), pos[0] + 6, ty, TEXT_COLOR, true);
			ty += 10;
		}
	}

	private static void renderFpsPanel(DrawContext ctx, MinecraftClient client, HudConfig cfg) {
		int fps = client.getCurrentFps();
		String label = fps + " FPS";

		int width = 10 + client.textRenderer.getWidth(label);
		int height = 16;

		if (cfg.fpsX == Integer.MIN_VALUE) {
			cfg.fpsX = client.getWindow().getScaledWidth() - width - 6;
			cfg.fpsY = 6;
		}
		int[] pos = clamp(client, cfg.fpsX, cfg.fpsY, width, height);

		GlassPanel.draw(ctx, pos[0], pos[1], width, height);
		int color = fps >= 60 ? 0xFF8CFFA0 : (fps >= 30 ? 0xFFFFE18C : 0xFFFF8C8C);
		ctx.drawText(client.textRenderer, Text.literal(label), pos[0] + 6, pos[1] + 4, color, true);
	}

	private static void renderPotionTimers(DrawContext ctx, MinecraftClient client, HudConfig cfg) {
		ClientPlayerEntity player = client.player;
		var effects = player.getStatusEffects();
		if (effects.isEmpty()) return;

		if (cfg.potionX == Integer.MIN_VALUE) { cfg.potionX = 6; cfg.potionY = 30; }
		int rowHeight = 16;
		int[] anchor = clamp(client, cfg.potionX, cfg.potionY, 120, rowHeight);
		int x = anchor[0];
		int y = anchor[1];

		for (StatusEffectInstance instance : effects) {
			String name = prettifyName(instance.getEffectType().value().getTranslationKey()
					.replace("effect.minecraft.", ""));
			int amplifier = instance.getAmplifier();
			String suffix = amplifier > 0 ? " " + toRoman(amplifier + 1) : "";
			int seconds = instance.getDuration() / 20;
			String time = formatTime(seconds);

			String line = name + suffix + "  " + time;
			int width = 10 + client.textRenderer.getWidth(line);

			GlassPanel.draw(ctx, x, y, width, rowHeight);
			ctx.drawText(client.textRenderer, Text.literal(line), x + 6, y + 4, TEXT_COLOR, true);

			y += rowHeight + 3;
		}
	}

	private static void renderStatusBars(DrawContext ctx, MinecraftClient client, HudConfig cfg) {
		ClientPlayerEntity player = client.player;
		int panelWidth = 140;

		int rows = (cfg.showArmorBar ? 1 : 0) + (cfg.showHungerBar ? 1 : 0);
		if (rows == 0) return;
		int panelHeight = 8 + rows * 12;

		if (cfg.statusX == Integer.MIN_VALUE) {
			cfg.statusX = (client.getWindow().getScaledWidth() / 2) - (panelWidth / 2);
			cfg.statusY = client.getWindow().getScaledHeight() - 58;
		}
		int[] pos = clamp(client, cfg.statusX, cfg.statusY, panelWidth, panelHeight);
		int x = pos[0], y = pos[1];

		GlassPanel.draw(ctx, x, y, panelWidth, panelHeight);
		int rowY = y + 5;

		if (cfg.showArmorBar) {
			int armor = player.getArmor();
			drawStatBar(ctx, client, x + 6, rowY, panelWidth - 12, armor, 20, "Armor", 0xFFB9C4CC);
			rowY += 12;
		}
		if (cfg.showHungerBar) {
			int hunger = player.getHungerManager().getFoodLevel();
			drawStatBar(ctx, client, x + 6, rowY, panelWidth - 12, hunger, 20, "Hunger", 0xFFE0A45C);
		}
	}

	private static void drawStatBar(DrawContext ctx, MinecraftClient client, int x, int y, int width,
									 int value, int max, String label, int barColor) {
		int labelWidth = client.textRenderer.getWidth(label) + 6;
		int barX = x + labelWidth;
		int barWidth = width - labelWidth;
		int barHeight = 6;

		ctx.drawText(client.textRenderer, Text.literal(label), x, y - 1, MUTED_COLOR, true);

		ctx.fill(barX, y, barX + barWidth, y + barHeight, 0x40000000);
		int filled = MathHelper.clamp((int) (barWidth * (value / (float) max)), 0, barWidth);
		ctx.fill(barX, y, barX + filled, y + barHeight, barColor | 0xFF000000);
		ctx.fill(barX, y, barX + barWidth, y + 1, 0x33FFFFFF);
	}

	/** Keeps a panel fully on-screen even after a resize or resolution change. */
	private static int[] clamp(MinecraftClient client, int x, int y, int width, int height) {
		int maxX = Math.max(0, client.getWindow().getScaledWidth() - width);
		int maxY = Math.max(0, client.getWindow().getScaledHeight() - height);
		return new int[]{ MathHelper.clamp(x, 0, maxX), MathHelper.clamp(y, 0, maxY) };
	}

	private static int maxTextWidth(MinecraftClient client, List<String> lines) {
		int max = 0;
		for (String line : lines) {
			max = Math.max(max, client.textRenderer.getWidth(line));
		}
		return max;
	}

	private static String prettifyName(String raw) {
		String[] parts = raw.replace('_', ' ').split(" ");
		StringBuilder sb = new StringBuilder();
		for (String part : parts) {
			if (part.isEmpty()) continue;
			sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
		}
		return sb.toString().trim();
	}

	private static String formatTime(int totalSeconds) {
		int m = totalSeconds / 60;
		int s = totalSeconds % 60;
		return String.format("%d:%02d", m, s);
	}

	private static String toRoman(int number) {
		String[] romans = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
		if (number >= 1 && number <= romans.length) return romans[number - 1];
		return String.valueOf(number);
	}
}
