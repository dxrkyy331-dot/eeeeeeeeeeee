package dev.lite.module;

import dev.lite.gui.Notifications;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public enum Category {
        RENDER("Render"), MOVEMENT("Movement"), MISC("Misc");
        public final String label;
        Category(String label) { this.label = label; }
    }

    public final String name;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();
    public int key = -1;
    public String keyLabel = "";
    boolean keyDown;
    private boolean enabled;
    private boolean locked;

    protected Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    /** Module that is always on (only holds settings). */
    protected void lock() { locked = true; enabled = true; }
    public boolean isLocked() { return locked; }
    public boolean isEnabled() { return enabled; }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean e) {
        if (locked || e == enabled) return;
        enabled = e;
        if (e) onEnable(); else onDisable();
        Notifications.push(name + (e ? " enabled" : " disabled"));
    }

    /** Used by config loading: no notification, state is applied lazily in onTick. */
    public void loadState(boolean e, int k) {
        enabled = e || locked;
        setKey(k);
    }

    public void setKey(int k) {
        key = k;
        keyLabel = k < 0 ? "" : "[" + InputUtil.fromKeyCode(k, 0).getLocalizedText().getString() + "]";
    }

    public void onEnable() { }
    public void onDisable() { }
    public void onTick(MinecraftClient mc) { }
    public void onHud(DrawContext ctx, MinecraftClient mc) { }
}
