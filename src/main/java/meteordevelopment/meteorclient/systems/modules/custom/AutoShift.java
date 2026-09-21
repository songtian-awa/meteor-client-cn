/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;

public class AutoShift extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> onlyWhenOnEdge = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-on-edge")
        .description("Only sneaks when you are near the edge of a block.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> holdShiftToOverride = sgGeneral.add(new BoolSetting.Builder()
        .name("hold-shift-to-override")
        .description("Stops auto sneaking while you manually hold shift.")
        .defaultValue(true)
        .build()
    );

    public AutoShift() {
        super(Categories.Player, "auto-shift", "Automatically sneaks when you are close to the edge of a block, preventing falls.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null || mc.world == null) return;

        if (holdShiftToOverride.get() && mc.options.sneakKey.isPressed()) return;

        boolean nearEdge = !mc.player.isOnGround();

        if (onlyWhenOnEdge.get() && nearEdge) {
            mc.options.sneakKey.setPressed(true);
        }
        else if (!onlyWhenOnEdge.get()) {
            mc.options.sneakKey.setPressed(true);
        }
        else {
            mc.options.sneakKey.setPressed(false);
        }
    }

    @Override
    public void onDeactivate() {
        mc.options.sneakKey.setPressed(false);
    }
}
