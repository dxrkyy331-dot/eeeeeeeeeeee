package dev.lite.gui;

/** Frame-rate independent exponential smoothing. */
public final class Anim {
    public float value, target;

    public Anim(float v) { value = target = v; }

    public void update(float dt, float speed) {
        value += (target - value) * (1f - (float) Math.exp(-speed * dt));
        if (Math.abs(target - value) < 0.001f) value = target;
    }
}
