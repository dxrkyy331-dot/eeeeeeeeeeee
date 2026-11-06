package dev.lite.module;

import dev.lite.Config;
import dev.lite.gui.Notifications;
import dev.lite.gui.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    public static final Setting.Mode THEME = new Setting.Mode("Theme", Theme.NAMES, 0);
    private static final List<Module> MODULES = new ArrayList<>();

    public static void init() {
        MODULES.add(new Hud());
        MODULES.add(new Fullbright());
        MODULES.add(new Zoom());
        MODULES.add(new SprintToggle());
        MODULES.add(new Interface());
        Config.load();
    }

    public static List<Module> all() { return MODULES; }

    public static List<Module> in(Module.Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : MODULES) if (m.category == c) out.add(m);
        return out;
    }

    public static void tick(MinecraftClient mc) {
        boolean canKey = mc.currentScreen == null;
        long handle = mc.getWindow().getHandle();
        for (Module m : MODULES) {
            if (m.key >= 0) {
                boolean down = InputUtil.isKeyPressed(handle, m.key);
                if (down && !m.keyDown && canKey) m.toggle();
                m.keyDown = down;
            }
            if (m.isEnabled() && mc.player != null) m.onTick(mc);
        }
    }

    public static void renderHud(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        for (Module m : MODULES) if (m.isEnabled()) m.onHud(ctx, mc);
        Notifications.render(ctx, mc);
    }

    /** Restores any changed vanilla state (e.g. FOV) before the game closes. */
    public static void shutdown() {
        for (Module m : MODULES) if (m.isEnabled()) m.onDisable();
    }
}
