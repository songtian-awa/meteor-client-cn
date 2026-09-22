/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;

public class AutoReply extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> trigger = sgGeneral.add(new StringSetting.Builder()
        .name("trigger")
        .description("Reply when a chat message contains this word.")
        .defaultValue("hi")
        .build()
    );

    private final Setting<String> reply = sgGeneral.add(new StringSetting.Builder()
        .name("reply")
        .description("The message to send in reply.")
        .defaultValue("Hello!")
        .build()
    );

    private final Setting<Boolean> ignoreOwn = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-own")
        .description("Do not reply to your own messages.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> cooldown = sgGeneral.add(new BoolSetting.Builder()
        .name("cooldown")
        .description("Only reply at most once every 5 seconds.")
        .defaultValue(true)
        .build()
    );

    private int cooldownLeft;

    public AutoReply() {
        super(Categories.Misc, "auto-reply", "Automatically replies to chat messages containing a keyword.");
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        if (mc.player == null) return;

        if (cooldown.get() && cooldownLeft > 0) return;

        if (event.getMessage().getString().contains(trigger.get())) {
            ChatUtils.sendPlayerMsg(reply.get());
            cooldownLeft = 100;
        }
    }
}
