/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class LavaBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> horizontalSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal-speed")
        .description("Horizontal movement multiplier while in lava.")
        .defaultValue(1.8)
        .min(1.0)
        .max(4.0)
        .sliderMin(1.0)
        .sliderMax(4.0)
        .build()
    );

    private final Setting<Double> upwardSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("upward-speed")
        .description("Upward velocity applied to rise out of lava faster.")
        .defaultValue(0.2)
        .min(0.0)
        .max(0.5)
        .sliderMin(0.0)
        .sliderMax(0.5)
        .build()
    );

    private final Setting<Boolean> allowSprint = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
        .name("allow-sprint")
        .description("Allow sprinting while in lava (vanilla normally prevents it).")
        .defaultValue(true)
        .build()
    );

    public LavaBypass() {
        super(Categories.Movement, "lava-bypass", "Moves faster in lava and lets you sprint through it, making lava traversal practical.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (!mc.player.isInLava()) return;

        if (allowSprint.get() && mc.options.forwardKey.isPressed()) {
            mc.player.setSprinting(true);
        }

        Vec3d vel = mc.player.getVelocity();

        // Speed up horizontal movement (already in the current direction)
        double h = horizontalSpeed.get();
        double hx = vel.x * (h - 1.0);
        double hz = vel.z * (h - 1.0);

        // Assist rising out of lava
        double vy = vel.y;
        if (vy < upwardSpeed.get() && mc.options.jumpKey.isPressed()) {
            vy = upwardSpeed.get();
        }

        mc.player.setVelocity(vel.x + hx, vy, vel.z + hz);
    }
}
