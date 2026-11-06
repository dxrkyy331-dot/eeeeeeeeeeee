package dev.lite.module;

import dev.lite.gui.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.List;
import java.util.Locale;

final class Hud extends Module {
    private final Setting.Bool fps = new Setting.Bool("FPS", true);
    private final Setting.Bool coords = new Setting.Bool("Coords", true);
    private final Setting.Bool facing = new Setting.Bool("Facing", true);
    private String l1 = "", l2 = "", l3 = "";

    Hud() {
        super("HUD", Category.RENDER);
        settings.addAll(List.of(fps, coords, facing));
    }

    @Override public void onTick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        l1 = fps.value ? "FPS " + mc.getCurrentFps() : "";
        l2 = coords.value ? String.format(Locale.ROOT, "XYZ %.1f %.1f %.1f", p.getX(), p.getY(), p.getZ()) : "";
        l3 = facing.value ? p.getHorizontalFacing().asString() : "";
    }

    @Override public void onHud(DrawContext c, MinecraftClient mc) {
        if (mc.options.hudHidden || mc.player == null) return;
        int col = 0xFF000000 | Theme.accent();
        int y = 4;
        for (String s : new String[]{l1, l2, l3}) {
            if (s.isEmpty()) continue;
            c.drawText(mc.textRenderer, s, 4, y, col, true);
            y += 11;
        }
    }
}

final class Fullbright extends Module {
    private boolean added;

    Fullbright() { super("Fullbright", Category.RENDER); }

    @Override public void onTick(MinecraftClient mc) {
        var cur = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (cur == null || cur.getDuration() < 220) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false, false));
            added = true;
        }
    }

    @Override public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (added && mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        added = false;
    }
}

final class Zoom extends Module {
    private final Setting.Num fov = new Setting.Num("FOV", 30, 70, 1, 30);
    private boolean applied;
    private int saved;

    Zoom() {
        super("Zoom", Category.RENDER);
        settings.add(fov);
    }

    @Override public void onTick(MinecraftClient mc) {
        SimpleOption<Integer> opt = mc.options.getFov();
        if (!applied) { saved = opt.getValue(); applied = true; }
        opt.setValue((int) fov.value);
    }

    @Override public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (applied && mc.options != null) mc.options.getFov().setValue(saved);
        applied = false;
    }
}

final class SprintToggle extends Module {
    SprintToggle() { super("Sprint", Category.MOVEMENT); }

    @Override public void onTick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (mc.options.forwardKey.isPressed() && !p.isSneaking() && !p.horizontalCollision
                && !p.isUsingItem() && p.getHungerManager().getFoodLevel() > 6) {
            p.setSprinting(true);
        }
    }
}

final class Interface extends Module {
    Interface() {
        super("Interface", Category.MISC);
        lock();
        settings.add(ModuleManager.THEME);
    }
}
