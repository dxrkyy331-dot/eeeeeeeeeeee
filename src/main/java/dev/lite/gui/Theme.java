package dev.lite.gui;

import dev.lite.module.ModuleManager;

public final class Theme {
    public static final String[] NAMES = {"Violet", "Ocean", "Mint", "Ember"};
    private static final int[] COLORS = {0x8B5CF6, 0x3B82F6, 0x34D399, 0xF97316};

    public static int accent() { return COLORS[ModuleManager.THEME.index]; }

    /** Alpha is clamped to >= 4/255, because vanilla text treats alpha 0 as fully opaque. */
    public static int argb(int rgb, float a) {
        int al = Math.max(4, Math.min(255, (int) (a * 255)));
        return (al << 24) | (rgb & 0xFFFFFF);
    }

    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
