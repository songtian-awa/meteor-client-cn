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
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SpiderBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> climbSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("climb-speed")
        .description("The vertical speed while climbing walls.")
        .defaultValue(0.2)
        .min(0.05)
        .max(0.5)
        .sliderMin(0.05)
        .sliderMax(0.5)
        .build()
    );

    private final Setting<Double> rampStep = sgGeneral.add(new DoubleSetting.Builder()
        .name("ramp-step")
        .description("How fast the climb speed ramps up. Lower values look more natural.")
        .defaultValue(0.02)
        .min(0.005)
        .max(0.1)
        .sliderMin(0.005)
        .sliderMax(0.1)
        .build()
    );

    private double currentSpeed;

    public SpiderBypass() {
        super(Categories.Movement, "spider-bypass", "Climbs walls by gradually ramping up vertical velocity, looking like natural movement instead of an instant climb.");
    }

    @Override
    public void onActivate() {
        currentSpeed = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null || mc.world == null) return;

        boolean againstWall = mc.player.horizontalCollision && !mc.player.isOnGround();

        if (againstWall) {
            currentSpeed = Math.min(climbSpeed.get(), currentSpeed + rampStep.get());
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, currentSpeed, vel.z);
        }
        else {
            currentSpeed = Math.max(0, currentSpeed - rampStep.get());
        }
    }
}
