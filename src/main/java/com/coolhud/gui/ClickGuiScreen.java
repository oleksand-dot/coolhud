package com.coolhud.gui;

import com.coolhud.HudConfig;
import com.coolhud.hud.GlassPanel;
import com.coolhud.hud.HudRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Floating category panels, click a row to toggle it — the classic
 * "clickgui" layout, but every row here is a genuine cosmetic/QoL
 * setting (same ones exposed in CoolHudConfigScreen). Opened with
 * Right Shift by default; Escape or Right Shift again closes it.
 */
public class ClickGuiScreen extends Screen {

	private static final int ROW_HEIGHT = 16;
	private static final int PANEL_WIDTH = 130;
	private static final int HEADER_HEIGHT = 16;

	private final Screen parent;
	private final HudConfig cfg = HudConfig.INSTANCE;
	private final List<Category> categories = new ArrayList<>();

	public ClickGuiScreen(Screen parent) {
		super(Text.literal("CoolHUD"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		categories.clear();

		Category hud = new Category("HUD", 10, 10);
		hud.rows.add(new Row("Coordinates", () -> cfg.showCoords, v -> cfg.showCoords = v));
		hud.rows.add(new Row("FPS Counter", () -> cfg.showFps, v -> cfg.showFps = v));
		hud.rows.add(new Row("Potion Timers", () -> cfg.showPotionTimers, v -> cfg.showPotionTimers = v));
		hud.rows.add(new Row("Biome Display", () -> cfg.showBiome, v -> cfg.showBiome = v));
		hud.rows.add(new Row("Armor Bar", () -> cfg.showArmorBar, v -> cfg.showArmorBar = v));
		hud.rows.add(new Row("Hunger Bar", () -> cfg.showHungerBar, v -> cfg.showHungerBar = v));
		categories.add(hud);

		Category movement = new Category("Movement", 10 + PANEL_WIDTH + 10, 10);
		movement.rows.add(new Row("Auto Sprint", () -> cfg.autoSprint, v -> cfg.autoSprint = v));
		categories.add(movement);

		Category visuals = new Category("Visuals", 10 + (PANEL_WIDTH + 10) * 2, 10);
		visuals.rows.add(new Row("Cinematic Bars", () -> cfg.showAspectRatioBars, v -> cfg.showAspectRatioBars = v));
		visuals.actionRows.add(new ActionRow(
				() -> "Ratio: " + HudRenderer.ASPECT_RATIO_LABELS[cfg.aspectRatioIndex],
				() -> cfg.aspectRatioIndex = (cfg.aspectRatioIndex + 1) % HudRenderer.ASPECT_RATIOS.length));
		visuals.actionRows.add(new ActionRow(
				() -> "Move HUD Panels...",
				() -> { cfg.save(); this.client.setScreen(new HudEditScreen(this)); }));
		categories.add(visuals);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		ctx.drawCenteredTextWithShadow(this.textRenderer,
				"CoolHUD — click a row to toggle. Right Shift or Esc to close.",
				this.width / 2, this.height - 16, 0xFFB8C4D9);

		for (Category cat : categories) {
			int height = HEADER_HEIGHT + (cat.rows.size() + cat.actionRows.size()) * ROW_HEIGHT + 4;
			GlassPanel.draw(ctx, cat.x, cat.y, PANEL_WIDTH, height);

			ctx.drawText(this.textRenderer, Text.literal(cat.name), cat.x + 6, cat.y + 4, 0xFF7FD6FF, true);

			int rowY = cat.y + HEADER_HEIGHT;
			for (Row row : cat.rows) {
				boolean hovered = mouseX >= cat.x && mouseX <= cat.x + PANEL_WIDTH
						&& mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT;
				int bg = hovered ? 0x40FFFFFF : 0x00000000;
				ctx.fill(cat.x + 2, rowY, cat.x + PANEL_WIDTH - 2, rowY + ROW_HEIGHT, bg);

				int dotColor = row.getter.getAsBoolean() ? 0xFF8CFFA0 : 0xFFFF8C8C;
				ctx.fill(cat.x + 6, rowY + 6, cat.x + 10, rowY + 10, dotColor | 0xFF000000);
				ctx.drawText(this.textRenderer, Text.literal(row.label), cat.x + 16, rowY + 4, 0xFFFFFFFF, true);
				rowY += ROW_HEIGHT;
			}
			for (ActionRow row : cat.actionRows) {
				boolean hovered = mouseX >= cat.x && mouseX <= cat.x + PANEL_WIDTH
						&& mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT;
				int bg = hovered ? 0x40FFFFFF : 0x00000000;
				ctx.fill(cat.x + 2, rowY, cat.x + PANEL_WIDTH - 2, rowY + ROW_HEIGHT, bg);
				ctx.drawText(this.textRenderer, Text.literal(row.labelSupplier.get()), cat.x + 6, rowY + 4, 0xFFE0E6F0, true);
				rowY += ROW_HEIGHT;
			}
		}

		super.render(ctx, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			for (Category cat : categories) {
				int rowY = cat.y + HEADER_HEIGHT;
				for (Row row : cat.rows) {
					if (hit(mouseX, mouseY, cat.x, rowY)) {
						row.setter.accept(!row.getter.getAsBoolean());
						return true;
					}
					rowY += ROW_HEIGHT;
				}
				for (ActionRow row : cat.actionRows) {
					if (hit(mouseX, mouseY, cat.x, rowY)) {
						row.action.run();
						return true;
					}
					rowY += ROW_HEIGHT;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private boolean hit(double mouseX, double mouseY, int x, int y) {
		return mouseX >= x && mouseX <= x + PANEL_WIDTH && mouseY >= y && mouseY <= y + ROW_HEIGHT;
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

	private static class Category {
		final String name;
		final int x, y;
		final List<Row> rows = new ArrayList<>();
		final List<ActionRow> actionRows = new ArrayList<>();

		Category(String name, int x, int y) {
			this.name = name;
			this.x = x;
			this.y = y;
		}
	}

	private static class Row {
		final String label;
		final BooleanSupplier getter;
		final Consumer<Boolean> setter;

		Row(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
			this.label = label;
			this.getter = getter;
			this.setter = setter;
		}
	}

	private static class ActionRow {
		final java.util.function.Supplier<String> labelSupplier;
		final Runnable action;

		ActionRow(java.util.function.Supplier<String> labelSupplier, Runnable action) {
			this.labelSupplier = labelSupplier;
			this.action = action;
		}
	}
}
