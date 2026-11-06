package dev.lite.gui;

import dev.lite.Config;
import dev.lite.LiteClient;
import dev.lite.module.Module;
import dev.lite.module.ModuleManager;
import dev.lite.module.Setting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ClickGuiScreen extends Screen {
    private static final int W = 112, HEAD = 18, ROW = 16, SROW = 16;
    private static final Map<Module.Category, float[]> POS = new EnumMap<>(Module.Category.class);

    private final List<Panel> panels = new ArrayList<>();
    private final Anim open = new Anim(0);
    private long last = System.nanoTime();
    private boolean closing;
    private Module binding;
    private Setting.Num dragNum;
    private float dragX;

    public ClickGuiScreen() {
        super(Text.empty());
        open.target = 1;
        for (Module.Category c : Module.Category.values()) {
            int idx = c.ordinal();
            float[] pos = POS.computeIfAbsent(c, k -> new float[]{12 + idx * (W + 8), 12});
            panels.add(new Panel(c, pos));
        }
    }

    @Override public boolean shouldPause() { return false; }

    @Override public void close() { closing = true; open.target = 0; }

    @Override public void removed() { Config.save(); }

    private void rect(DrawContext c, float x, float y, float w, float h, int rgb, float a) {
        c.fill((int) x, (int) y, (int) (x + w), (int) (y + h), Theme.argb(rgb, a));
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        long now = System.nanoTime();
        float dt = Math.min((now - last) / 1e9f, 0.1f);
        last = now;
        open.update(dt, 14f);

        if (closing && open.value <= 0.01f) {
            if (client != null) client.setScreen(null);
            return;
        }
        float a = open.value;
        if (a < 0.02f) return;

        rect(ctx, 0, 0, width, height, 0x000000, 0.45f * a);
        for (Panel p : panels) p.render(ctx, mx, my, dt, a);

        if (binding != null) {
            String s = "Press a key for " + binding.name + " (Esc = clear)";
            ctx.drawCenteredTextWithShadow(textRenderer, s, width / 2, height - 24, Theme.argb(0xFFFFFF, a));
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (binding != null) { binding = null; return true; }
        for (int i = panels.size() - 1; i >= 0; i--) {
            Panel p = panels.get(i);
            if (p.click(mx, my, button)) {
                panels.remove(i);
                panels.add(p);
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragNum = null;
        for (Panel p : panels) p.dragging = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragNum != null) setFromMouse(mx);
        for (Panel p : panels) if (p.dragging) { p.pos[0] = (float) mx - p.ox; p.pos[1] = (float) my - p.oy; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    private void setFromMouse(double mx) {
        double t = Math.max(0, Math.min(1, (mx - dragX) / (W - 12)));
        dragNum.set(dragNum.min + (dragNum.max - dragNum.min) * t);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding != null) {
            binding.setKey(keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE ? -1 : keyCode);
            binding = null;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || LiteClient.openKey.matchesKey(keyCode, scanCode)) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ------------------------------------------------------------------ panel

    private final class Panel {
        final Module.Category cat;
        final float[] pos;
        final List<Row> rows = new ArrayList<>();
        final Anim openAnim = new Anim(1);
        boolean expanded = true, dragging;
        float ox, oy, visBottom;

        Panel(Module.Category cat, float[] pos) {
            this.cat = cat;
            this.pos = pos;
            for (Module m : ModuleManager.in(cat)) rows.add(new Row(m, pos));
        }

        void render(DrawContext c, int mx, int my, float dt, float a) {
            TextRenderer tr = textRenderer;
            openAnim.update(dt, 12f);
            float x = pos[0], y = pos[1];
            int accent = Theme.accent();

            rect(c, x, y, W, HEAD, 0x101014, 0.94f * a);
            rect(c, x, y + HEAD - 1, W, 1, accent, a);
            c.drawText(tr, cat.label, (int) x + 7, (int) y + 5, Theme.argb(0xFFFFFF, a), false);
            String arrow = expanded ? "-" : "+";
            c.drawText(tr, arrow, (int) x + W - 4 - tr.getWidth(arrow), (int) y + 5, Theme.argb(0x888890, a), false);

            float content = 0;
            for (Row r : rows) { r.update(dt); content += r.height(); }
            float vis = content * openAnim.value;
            visBottom = y + HEAD + vis;
            if (vis < 0.5f) return;

            c.enableScissor((int) x, (int) (y + HEAD), (int) x + W, (int) visBottom);
            float ry = y + HEAD;
            for (Row r : rows) {
                r.top = ry;
                r.draw(c, mx, my, a);
                ry += r.height();
            }
            c.disableScissor();
        }

        boolean click(double mx, double my, int button) {
            float x = pos[0], y = pos[1];
            if (mx < x || mx >= x + W) return false;
            if (my >= y && my < y + HEAD) {
                if (button == 0) { dragging = true; ox = (float) mx - x; oy = (float) my - y; }
                else if (button == 1) { expanded = !expanded; openAnim.target = expanded ? 1 : 0; }
                return true;
            }
            if (my < y + HEAD || my >= visBottom || openAnim.value < 0.5f) return false;
            for (Row r : rows) {
                if (my >= r.top && my < r.top + r.height()) { r.click(mx, my, button); return true; }
            }
            return false;
        }
    }

    // -------------------------------------------------------------------- row

    private final class Row {
        final Module m;
        final float[] pos;
        final Anim on = new Anim(0), ex = new Anim(0), hov = new Anim(0);
        boolean expanded;
        float top;

        Row(Module m, float[] pos) {
            this.m = m;
            this.pos = pos;
            on.value = on.target = m.isEnabled() ? 1 : 0;
        }

        float height() { return ROW + ex.value * m.settings.size() * SROW; }

        void update(float dt) {
            on.target = m.isEnabled() ? 1 : 0;
            on.update(dt, 16f);
            ex.update(dt, 14f);
            hov.update(dt, 20f);
        }

        void draw(DrawContext c, int mx, int my, float a) {
            TextRenderer tr = textRenderer;
            float x = pos[0], y = top;
            c.enableScissor((int) x, (int) y, (int) x + W, (int) (y + height()));

            hov.target = (mx >= x && mx < x + W && my >= y && my < y + ROW) ? 1 : 0;
            int accent = Theme.accent();
            boolean lockedMod = m.isLocked();
            float onv = lockedMod ? 0f : on.value;

            int bg = Theme.lerp(Theme.lerp(0x15151A, 0x1D1D24, hov.value), accent, onv * 0.30f);
            rect(c, x, y, W, ROW, bg, 0.92f * a);
            rect(c, x, y, 2 * onv, ROW, accent, a);

            int txt = Theme.lerp(0xA8A8B3, 0xFFFFFF, lockedMod ? 1f : on.value);
            c.drawText(tr, m.name, (int) x + 7, (int) y + 4, Theme.argb(txt, a), false);

            String k = binding == m ? "..." : m.keyLabel;
            if (!k.isEmpty()) {
                c.drawText(tr, k, (int) x + W - 4 - tr.getWidth(k), (int) y + 4, Theme.argb(0x70707A, a), false);
            }

            float sa = a * ex.value;
            if (sa >= 0.03f) {
                float sy = y + ROW;
                for (Setting s : m.settings) {
                    drawSetting(c, s, x, sy, dtFor(), sa, accent);
                    sy += SROW;
                }
            }
            c.disableScissor();
        }

        private float dtFor() { return 1f / 60f; }

        private void drawSetting(DrawContext c, Setting s, float x, float y, float dt, float a, int accent) {
            TextRenderer tr = textRenderer;
            rect(c, x, y, W, SROW, 0x101014, 0.95f * a);
            c.drawText(tr, s.name, (int) x + 9, (int) y + 4, Theme.argb(0xC0C0CA, a), false);

            if (s instanceof Setting.Bool b) {
                s.anim.target = b.value ? 1 : 0;
                s.anim.update(dt, 18f);
                float bx = x + W - 18, by = y + 3;
                rect(c, bx, by, 10, 10, 0x2A2A33, a);
                rect(c, bx + 1, by + 1, 8, 8, accent, a * s.anim.value);
            } else if (s instanceof Setting.Num n) {
                s.anim.target = (float) ((n.value - n.min) / (n.max - n.min));
                s.anim.update(dt, 18f);
                c.drawText(tr, n.text, (int) x + W - 7 - tr.getWidth(n.text), (int) y + 3, Theme.argb(0x9A9AA5, a), false);
                rect(c, x + 6, y + SROW - 4, W - 12, 2, 0x2A2A33, a);
                rect(c, x + 6, y + SROW - 4, (W - 12) * s.anim.value, 2, accent, a);
            } else if (s instanceof Setting.Mode md) {
                String v = md.current();
                c.drawText(tr, v, (int) x + W - 7 - tr.getWidth(v), (int) y + 4, Theme.argb(accent, a), false);
            }
        }

        void click(double mx, double my, int button) {
            if (my < top + ROW) {
                if (button == 0) {
                    if (m.isLocked()) toggleExpand(); else m.toggle();
                } else if (button == 1) {
                    toggleExpand();
                } else if (button == 2 && !m.isLocked()) {
                    binding = m;
                }
                return;
            }
            if (!expanded) return;
            int i = (int) ((my - top - ROW) / SROW);
            if (i < 0 || i >= m.settings.size()) return;
            Setting s = m.settings.get(i);
            if (s instanceof Setting.Bool b) {
                b.value = !b.value;
            } else if (s instanceof Setting.Mode md) {
                md.cycle(button == 1 ? -1 : 1);
            } else if (s instanceof Setting.Num n) {
                dragNum = n;
                dragX = pos[0] + 6;
                setFromMouse(mx);
            }
        }

        private void toggleExpand() {
            if (m.settings.isEmpty()) return;
            expanded = !expanded;
            ex.target = expanded ? 1 : 0;
        }
    }
}
