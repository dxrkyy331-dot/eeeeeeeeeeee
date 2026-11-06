package dev.lite.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;

public final class Notifications {
    private record Toast(String text, long born) { }

    private static final long LIFE = 2400, FADE = 250;
    private static final ArrayDeque<Toast> QUEUE = new ArrayDeque<>();

    public static void push(String text) {
        if (QUEUE.size() >= 5) QUEUE.pollFirst();
        QUEUE.addLast(new Toast(text, System.currentTimeMillis()));
    }

    public static void render(DrawContext c, MinecraftClient mc) {
        long now = System.currentTimeMillis();
        QUEUE.removeIf(t -> now - t.born() > LIFE);
        if (QUEUE.isEmpty() || mc.options.hudHidden) return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int y = sh - 34;
        int accent = Theme.accent();

        for (Toast t : QUEUE) {
            long age = now - t.born();
            float in = Math.min(age / (float) FADE, 1f);
            float out = age > LIFE - FADE ? (LIFE - age) / (float) FADE : 1f;
            float s = Math.min(1f - (1f - in) * (1f - in) * (1f - in), out);
            int w = mc.textRenderer.getWidth(t.text()) + 14;
            int x = (int) (sw - (w + 6) * s);
            c.fill(x, y, x + w, y + 18, Theme.argb(0x101014, 0.9f));
            c.fill(x, y, x + 2, y + 18, Theme.argb(accent, 1f));
            c.drawText(mc.textRenderer, t.text(), x + 8, y + 5, 0xFFFFFFFF, false);
            y -= 22;
        }
    }
}
