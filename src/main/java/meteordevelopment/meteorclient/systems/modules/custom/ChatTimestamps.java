/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatTimestamps extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> time24h = sgGeneral.add(new BoolSetting.Builder()
        .name("24-hour-format")
        .description("Use a 24-hour clock instead of AM/PM.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> decorate = sgGeneral.add(new BoolSetting.Builder()
        .name("decorate")
        .description("Surround the timestamp with brackets and color it gray.")
        .defaultValue(true)
        .build()
    );

    private final DateTimeFormatter formatter12 = DateTimeFormatter.ofPattern("h:mm a");
    private final DateTimeFormatter formatter24 = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ChatTimestamps() {
        super(Categories.Misc, "chat-timestamps", "Adds the current time to every chat message you receive.");
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        String time = time24h.get() ? formatter24.format(LocalTime.now()) : formatter12.format(LocalTime.now());

        MutableText timestamp;
        if (decorate.get()) {
            timestamp = Text.literal("[" + time + "] ").formatted(Formatting.GRAY);
        }
        else {
            timestamp = Text.literal(time + " ");
        }

        event.setMessage(Text.empty().append(timestamp).append(event.getMessage()));
    }
}
