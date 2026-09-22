/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.List;
import java.util.regex.Pattern;

public class WordFilter extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> words = sgGeneral.add(new StringListSetting.Builder()
        .name("words")
        .description("Words to filter from your outgoing messages.")
        .defaultValue(List.of("nigger", "faggot", "retard"))
        .build()
    );

    private final Setting<String> replacement = sgGeneral.add(new StringSetting.Builder()
        .name("replacement")
        .description("What to replace filtered words with.")
        .defaultValue("****")
        .build()
    );

    public WordFilter() {
        super(Categories.Misc, "word-filter", "Automatically censors banned words in your own chat messages to keep your account safe.");
    }

    @EventHandler
    private void onSendMessage(SendMessageEvent event) {
        String message = event.message;

        for (String word : words.get()) {
            if (!word.isEmpty() && message.toLowerCase().contains(word.toLowerCase())) {
                message = message.replaceAll("(?i)" + Pattern.quote(word), replacement.get());
            }
        }

        event.message = message;
    }
}
