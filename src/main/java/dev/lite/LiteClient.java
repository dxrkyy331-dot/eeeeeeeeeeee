package dev.lite;

import dev.lite.gui.ClickGuiScreen;
import dev.lite.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LiteClient implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("lite");
    public static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();

        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lite.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.categories.lite"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new ClickGuiScreen());
            }
            ModuleManager.tick(mc);
        });

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> ModuleManager.renderHud(ctx));

        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
            ModuleManager.shutdown();
            Config.save();
        });
    }
}
