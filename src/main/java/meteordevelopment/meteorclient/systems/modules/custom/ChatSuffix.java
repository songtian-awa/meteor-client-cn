/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class ChatSuffix extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> suffix = sgGeneral.add(new StringSetting.Builder()
        .name("suffix")
        .description("The text to append to your messages.")
        .defaultValue(" | Meteor CN")
        .build()
    );

    private final Setting<Boolean> spaceBefore = sgGeneral.add(new BoolSetting.Builder()
        .name("space-before")
        .description("Add a space before the suffix.")
        .defaultValue(true)
        .build()
    );

    public ChatSuffix() {
        super(Categories.Misc, "chat-suffix", "Automatically appends a custom suffix to every chat message you send.");
    }

    @EventHandler
    private void onSendMessage(SendMessageEvent event) {
        if (suffix.get().isEmpty()) return;
        if (event.message.startsWith("/")) return;

        String sep = spaceBefore.get() ? " " : "";
        event.message = event.message + sep + suffix.get();
    }
}
