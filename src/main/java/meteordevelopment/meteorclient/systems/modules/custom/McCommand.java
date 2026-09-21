/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;

public class McCommand extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> command = sgGeneral.add(new StringSetting.Builder()
        .name("command")
        .description("The command to run when this module is toggled on.")
        .defaultValue("say Hi from Meteor!")
        .build()
    );

    public McCommand() {
        super(Categories.Player, "mc-command", "Runs a chat or command when toggled on. Toggle off to run again.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;

        String cmd = command.get();
        if (cmd == null || cmd.isBlank()) {
            toggle();
            return;
        }

        ChatUtils.sendPlayerMsg(cmd);
        toggle();
    }
}
