/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;

import java.util.List;

public class AutoShout extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> interval = sgGeneral.add(new DoubleSetting.Builder()
        .name("interval")
        .description("How often to send a message, in seconds.")
        .defaultValue(10.0)
        .min(1.0)
        .max(600.0)
        .sliderMin(1.0)
        .sliderMax(600.0)
        .build()
    );

    private final Setting<Boolean> randomOrder = sgGeneral.add(new BoolSetting.Builder()
        .name("random-order")
        .description("Send the messages in a random order each cycle.")
        .defaultValue(true)
        .build()
    );

    private int ticks;

    public AutoShout() {
        super(Categories.Misc, "auto-shout", "Automatically sends a configured list of messages at a set interval.");
    }

    @Override
    public void onActivate() {
        ticks = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        ticks++;
        int intervalTicks = (int) Math.round(interval.get() * 20);
        if (ticks < intervalTicks) return;
        ticks = 0;

        List<String> messages = List.of(
            "Hi!",
            "Hello everyone!",
            "Meteor Client rocks!"
        );

        String message;
        if (randomOrder.get()) {
            message = messages.get((int) (Math.random() * messages.size()));
        }
        else {
            message = messages.get((ticks / intervalTicks) % messages.size());
        }

        ChatUtils.sendPlayerMsg(message);
    }
}
