package com.coolhud.hud;

import net.minecraft.client.gui.DrawContext;

/**
 * Draws a soft "glassmorphism" style panel: a translucent fill,
 * a subtle top-highlight, and a thin outer border. Pure vanilla
 * drawing calls, so it stays cheap on low-end / Pojav devices.
 */
public final class GlassPanel {

	private GlassPanel() {}

	// Base glass tones
	private static final int FILL = 0x66101014;      // translucent dark fill
	private static final int BORDER = 0x33FFFFFF;     // faint white border
	private static final int HIGHLIGHT = 0x22FFFFFF;  // top sheen
	private static final int ACCENT = 0xFF7FD6FF;     // soft cyan accent line

	public static void draw(DrawContext ctx, int x, int y, int width, int height) {
		// main fill
		ctx.fill(x, y, x + width, y + height, FILL);

		// top sheen (first couple rows lighter, gives a glossy feel)
		ctx.fill(x, y, x + width, y + Math.max(2, height / 6), HIGHLIGHT);

		// border
		ctx.fill(x, y, x + width, y + 1, BORDER);
		ctx.fill(x, y + height - 1, x + width, y + height, BORDER);
		ctx.fill(x, y, x + 1, y + height, BORDER);
		ctx.fill(x + width - 1, y, x + width, y + height, BORDER);

		// left accent strip, the mod's little signature touch
		ctx.fill(x, y, x + 2, y + height, ACCENT);
	}

	public static int accentColor() {
		return ACCENT;
	}
}
