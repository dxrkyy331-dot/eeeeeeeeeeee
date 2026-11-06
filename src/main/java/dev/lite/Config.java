package dev.lite;

import com.google.gson.*;
import dev.lite.module.Module;
import dev.lite.module.ModuleManager;
import dev.lite.module.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("lite.json");
    }

    public static void save() {
        JsonObject mods = new JsonObject();
        for (Module m : ModuleManager.all()) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", m.isEnabled());
            o.addProperty("key", m.key);
            JsonObject s = new JsonObject();
            for (Setting st : m.settings) s.add(st.name, st.toJson());
            o.add("settings", s);
            mods.add(m.name, o);
        }
        JsonObject root = new JsonObject();
        root.add("modules", mods);
        try {
            Files.writeString(file(), GSON.toJson(root));
        } catch (IOException e) {
            LiteClient.LOG.warn("Could not save config", e);
        }
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (Reader r = Files.newBufferedReader(f)) {
            JsonObject mods = JsonParser.parseReader(r).getAsJsonObject().getAsJsonObject("modules");
            for (Module m : ModuleManager.all()) {
                if (!mods.has(m.name)) continue;
                JsonObject o = mods.getAsJsonObject(m.name);
                m.loadState(o.has("enabled") && o.get("enabled").getAsBoolean(),
                        o.has("key") ? o.get("key").getAsInt() : -1);
                if (!o.has("settings")) continue;
                JsonObject s = o.getAsJsonObject("settings");
                for (Setting st : m.settings) {
                    if (!s.has(st.name)) continue;
                    try { st.fromJson(s.get(st.name)); } catch (Exception ignored) { }
                }
            }
        } catch (Exception e) {
            LiteClient.LOG.warn("Could not load config", e);
        }
    }
}
