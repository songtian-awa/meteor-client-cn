/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;

public class ChatFilter extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> blockedWord = sgGeneral.add(new StringSetting.Builder()
        .name("blocked-word")
        .description("Messages containing this word will be hidden.")
        .defaultValue("")
        .build()
    );

    public ChatFilter() {
        super(Categories.Misc, "chat-filter", "Hides chat messages that contain a specific word.");
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        String word = blockedWord.get();
        if (word == null || word.isBlank()) return;

        String message = event.getMessage().getString();
        if (message.contains(word)) {
            event.cancel();
        }
    }
}
