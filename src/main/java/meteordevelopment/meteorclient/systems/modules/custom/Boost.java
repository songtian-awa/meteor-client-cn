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

public class Boost extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Horizontal speed multiplier while moving forward.")
        .defaultValue(1.35)
        .min(1.0)
        .max(3.0)
        .sliderMin(1.0)
        .sliderMax(3.0)
        .build()
    );

    private final Setting<Double> airMultiplier = sgGeneral.add(new DoubleSetting.Builder()
        .name("air-multiplier")
        .description("Extra multiplier applied when airborne.")
        .defaultValue(0.0)
        .min(0.0)
        .max(1.0)
        .sliderMin(0.0)
        .sliderMax(1.0)
        .build()
    );

    public Boost() {
        super(Categories.Movement, "boost", "Horizontal speed boost in the direction you are moving.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (mc.player.isGliding() || mc.player.isTouchingWater() || mc.player.isClimbing()) return;

        // Only boost when holding forward or sideways movement keys
        boolean moving = mc.player.input.playerInput.forward() || mc.player.input.playerInput.backward()
            || mc.player.input.playerInput.left() || mc.player.input.playerInput.right();
        if (!moving) return;

        Vec3d vel = mc.player.getVelocity();
        double mult = speed.get();

        if (!mc.player.isOnGround()) {
            mult += airMultiplier.get();
        }

        mc.player.setVelocity(vel.x * mult, vel.y, vel.z * mult);
    }
}
