package dev.lite.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.lite.gui.Anim;

import java.util.Locale;

public abstract class Setting {
    public final String name;
    public final Anim anim = new Anim(0);

    protected Setting(String name) { this.name = name; }

    public abstract JsonElement toJson();
    public abstract void fromJson(JsonElement e);

    public static final class Bool extends Setting {
        public boolean value;
        public Bool(String name, boolean def) { super(name); value = def; anim.value = anim.target = def ? 1 : 0; }
        @Override public JsonElement toJson() { return new JsonPrimitive(value); }
        @Override public void fromJson(JsonElement e) { value = e.getAsBoolean(); }
    }

    public static final class Num extends Setting {
        public final double min, max, step;
        public double value;
        public String text = "";
        public Num(String name, double min, double max, double step, double def) {
            super(name);
            this.min = min; this.max = max; this.step = step;
            set(def);
            anim.value = anim.target = (float) ((value - min) / (max - min));
        }
        public void set(double v) {
            v = Math.max(min, Math.min(max, v));
            v = Math.round(v / step) * step;
            value = v;
            text = step >= 1 ? Integer.toString((int) v) : String.format(Locale.ROOT, "%.1f", v);
        }
        @Override public JsonElement toJson() { return new JsonPrimitive(value); }
        @Override public void fromJson(JsonElement e) { set(e.getAsDouble()); }
    }

    public static final class Mode extends Setting {
        public final String[] modes;
        public int index;
        public Mode(String name, String[] modes, int def) { super(name); this.modes = modes; index = def; }
        public void cycle(int d) { index = Math.floorMod(index + d, modes.length); }
        public String current() { return modes[index]; }
        @Override public JsonElement toJson() { return new JsonPrimitive(current()); }
        @Override public void fromJson(JsonElement e) {
            String s = e.getAsString();
            for (int i = 0; i < modes.length; i++) if (modes[i].equals(s)) index = i;
        }
    }
}
