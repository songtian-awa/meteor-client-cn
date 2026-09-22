/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Chinese localization support (i18n) for Meteor Client.
 * Dictionary and font adapted from dingzhen-vape/Meteor-I18n-Support-plugin (GPL-3.0).
 */

package meteordevelopment.meteorclient.i18n;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Translator {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DICT_PATH = "assets/meteor-client/i18n/zh_cn.json";
    private static final Identifier DICT_ID = Identifier.of("meteor-client", "i18n/zh_cn.json");

    private static Map<String, String> strings = Collections.emptyMap();
    private static boolean loaded = false;

    private Translator() {
    }

    public static String translate(String key, String fallback) {
        if (!loaded || strings.isEmpty()) {
            try {
                reload(MinecraftClient.getInstance().getResourceManager());
            }
            catch (Exception ignored) {
            }
        }

        String value = strings.get(key);
        return value != null ? value : fallback;
    }

    public static void reload(ResourceManager manager) {
        Map<String, String> map = new HashMap<>();

        // 1) Prefer the resource manager (allows resource packs to override the dictionary)
        try {
            manager.getResource(DICT_ID).ifPresent(resource -> {
                try (InputStream stream = resource.getInputStream()) {
                    readInto(map, stream);
                }
                catch (Exception ignored) {
                }
            });
        }
        catch (Exception ignored) {
        }

        // 2) Fallback: read directly from the classpath (always available, independent of reload timing)
        if (map.isEmpty()) {
            try (InputStream stream = Translator.class.getClassLoader().getResourceAsStream(DICT_PATH)) {
                if (stream != null) readInto(map, stream);
            }
            catch (Exception ignored) {
            }
        }

        strings = Collections.unmodifiableMap(map);
        loaded = !strings.isEmpty();
    }

    private static void readInto(Map<String, String> map, InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            JsonObject obj = GSON.fromJson(reader, JsonObject.class);
            if (obj != null) {
                obj.entrySet().forEach(entry -> map.put(entry.getKey(), entry.getValue().getAsString()));
            }
        }
        catch (Exception ignored) {
        }
    }
}
