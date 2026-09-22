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
import meteordevelopment.orbit.EventHandler;

public class FastSwim extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> horizontal = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal")
        .description("Horizontal speed multiplier while swimming.")
        .defaultValue(1.5)
        .min(1.0)
        .max(4.0)
        .sliderMin(1.0)
        .sliderMax(4.0)
        .build()
    );

    private final Setting<Double> vertical = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical")
        .description("Vertical speed multiplier while swimming.")
        .defaultValue(1.5)
        .min(1.0)
        .max(4.0)
        .sliderMin(1.0)
        .sliderMax(4.0)
        .build()
    );

    private final Setting<Boolean> requireSpace = sgGeneral.add(new BoolSetting.Builder()
        .name("require-space")
        .description("Only boost when the jump key is held.")
        .defaultValue(false)
        .build()
    );

    public FastSwim() {
        super(Categories.Movement, "fast-swim", "Swim faster through water with a gentle speed boost.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (!mc.player.isTouchingWater()) return;

        if (requireSpace.get() && !mc.options.jumpKey.isPressed()) return;

        var vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x * horizontal.get(), vel.y * vertical.get(), vel.z * horizontal.get());
    }
}
