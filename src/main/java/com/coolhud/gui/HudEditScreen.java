package com.coolhud.gui;

import com.coolhud.HudConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets the user drag each HUD panel to a new spot on screen.
 * Panels are represented as simple labeled boxes matching their real
 * on-screen size; dragging one updates HudConfig's stored x/y directly,
 * and the real HUD (drawn behind this screen's dim background) reflects
 * the change immediately since it reads the same config fields.
 */
public class HudEditScreen extends Screen {

	private final Screen parent;
	private final HudConfig cfg = HudConfig.INSTANCE;
	private final List<DragBox> boxes = new ArrayList<>();
	private DragBox dragging = null;
	private int dragOffsetX, dragOffsetY;

	public HudEditScreen(Screen parent) {
		super(Text.literal("Move HUD Panels"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		boxes.clear();

		if (cfg.showCoords || cfg.showBiome) {
			boxes.add(new DragBox("Coords", 130, 22,
					() -> cfg.coordsX, () -> cfg.coordsY,
					(x, y) -> { cfg.coordsX = x; cfg.coordsY = y; }));
		}
		if (cfg.showFps) {
			boxes.add(new DragBox("FPS", 80, 16,
					() -> cfg.fpsX, () -> cfg.fpsY,
					(x, y) -> { cfg.fpsX = x; cfg.fpsY = y; }));
		}
		if (cfg.showPotionTimers) {
			boxes.add(new DragBox("Potion Timers", 130, 16,
					() -> cfg.potionX, () -> cfg.potionY,
					(x, y) -> { cfg.potionX = x; cfg.potionY = y; }));
		}
		if (cfg.showArmorBar || cfg.showHungerBar) {
			boxes.add(new DragBox("Armor / Hunger", 140, 30,
					() -> cfg.statusX, () -> cfg.statusY,
					(x, y) -> { cfg.statusX = x; cfg.statusY = y; }));
		}

		// Snap any not-yet-placed panels to a sane default so they're visible to drag.
		for (DragBox box : boxes) {
			if (box.getX() == Integer.MIN_VALUE) box.setPos(6, 6);
		}

		this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> {
			cfg.save();
			this.close();
		}).dimensions(this.width / 2 - 60, this.height - 28, 120, 20).build());
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		// dim background so boxes stand out, but keep it translucent so the
		// user can still see roughly where the real HUD elements sit
		ctx.fill(0, 0, this.width, this.height, 0x90000000);

		ctx.drawCenteredTextWithShadow(this.textRenderer,
				"Drag panels to reposition. Press Done when finished.",
				this.width / 2, 12, 0xFFFFFFFF);

		for (DragBox box : boxes) {
			boolean hovered = box == dragging || box.contains(mouseX, mouseY);
			int color = hovered ? 0xAA3FA9F5 : 0x883FA9F5;
			ctx.fill(box.getX(), box.getY(), box.getX() + box.width, box.getY() + box.height, color);
			ctx.drawBorder(box.getX(), box.getY(), box.width, box.height, 0xFFFFFFFF);
			ctx.drawCenteredTextWithShadow(this.textRenderer, box.label,
					box.getX() + box.width / 2, box.getY() + box.height / 2 - 4, 0xFFFFFFFF);
		}

		super.render(ctx, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			for (DragBox box : boxes) {
				if (box.contains((int) mouseX, (int) mouseY)) {
					dragging = box;
					dragOffsetX = (int) mouseX - box.getX();
					dragOffsetY = (int) mouseY - box.getY();
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (dragging != null) {
			int newX = MathHelper.clamp((int) mouseX - dragOffsetX, 0, this.width - dragging.width);
			int newY = MathHelper.clamp((int) mouseY - dragOffsetY, 0, this.height - dragging.height);
			dragging.setPos(newX, newY);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0 && dragging != null) {
			dragging = null;
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public void close() {
		this.client.setScreen(parent);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	/** A draggable rectangle bound to a pair of int fields in HudConfig. */
	private static class DragBox {
		final String label;
		final int width, height;
		private final java.util.function.Supplier<Integer> getX;
		private final java.util.function.Supplier<Integer> getY;
		private final java.util.function.BiConsumer<Integer, Integer> setPos;

		DragBox(String label, int width, int height,
				java.util.function.Supplier<Integer> getX,
				java.util.function.Supplier<Integer> getY,
				java.util.function.BiConsumer<Integer, Integer> setPos) {
			this.label = label;
			this.width = width;
			this.height = height;
			this.getX = getX;
			this.getY = getY;
			this.setPos = setPos;
		}

		int getX() { return getX.get(); }
		int getY() { return getY.get(); }
		void setPos(int x, int y) { setPos.accept(x, y); }

		boolean contains(int mx, int my) {
			return mx >= getX() && mx <= getX() + width && my >= getY() && my <= getY() + height;
		}
	}
}
