package com.coolhud.hud;

import com.coolhud.HudConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class HudRenderer {

	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int MUTED_COLOR = 0xFFB8C4D9;

	public static void render(DrawContext ctx, MinecraftClient client) {
		if (client.player == null) return;
		HudConfig cfg = HudConfig.INSTANCE;

		if (cfg.showCoords || cfg.showBiome) {
			renderInfoPanel(ctx, client, cfg);
		}
		if (cfg.showFps) {
			renderFpsPanel(ctx, client, cfg);
		}
		if (cfg.showPotionTimers) {
			renderPotionTimers(ctx, client);
		}
		if (cfg.showArmorBar || cfg.showHungerBar) {
			renderStatusBars(ctx, client, cfg);
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
		int x = 6, y = 6;

		GlassPanel.draw(ctx, x, y, width, height);
		int ty = y + 4;
		for (String line : lines) {
			ctx.drawText(client.textRenderer, Text.literal(line), x + 6, ty, TEXT_COLOR, true);
			ty += 10;
		}
	}

	private static void renderFpsPanel(DrawContext ctx, MinecraftClient client, HudConfig cfg) {
		int fps = client.getCurrentFps();
		String label = fps + " FPS";

		int width = 10 + client.textRenderer.getWidth(label);
		int height = 16;
		int x = client.getWindow().getScaledWidth() - width - 6;
		int y = 6;

		GlassPanel.draw(ctx, x, y, width, height);

		int color = fps >= 60 ? 0xFF8CFFA0 : (fps >= 30 ? 0xFFFFE18C : 0xFFFF8C8C);
		ctx.drawText(client.textRenderer, Text.literal(label), x + 6, y + 4, color, true);
	}

	private static void renderPotionTimers(DrawContext ctx, MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		var effects = player.getStatusEffects();
		if (effects.isEmpty()) return;

		int x = 6;
		int y = 30;
		int rowHeight = 16;

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
		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();

		int panelWidth = 140;
		int x = (screenWidth / 2) - (panelWidth / 2);
		int y = screenHeight - 58;

		int rows = (cfg.showArmorBar ? 1 : 0) + (cfg.showHungerBar ? 1 : 0);
		if (rows == 0) return;
		int panelHeight = 8 + rows * 12;

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

		// track
		ctx.fill(barX, y, barX + barWidth, y + barHeight, 0x40000000);
		// fill
		int filled = MathHelper.clamp((int) (barWidth * (value / (float) max)), 0, barWidth);
		ctx.fill(barX, y, barX + filled, y + barHeight, barColor | 0xFF000000);
		// thin border
		ctx.fill(barX, y, barX + barWidth, y + 1, 0x33FFFFFF);
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
