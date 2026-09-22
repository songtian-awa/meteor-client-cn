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
import net.minecraft.util.math.Vec3d;

public class FastFall extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> fallSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("fall-speed")
        .description("The extra downward velocity applied per tick.")
        .defaultValue(0.05)
        .min(0.01)
        .max(0.3)
        .sliderMin(0.01)
        .sliderMax(0.3)
        .build()
    );

    private final Setting<Boolean> onlyWhenSneaking = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-sneaking")
        .description("Only falls faster while you are sneaking.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> requireAirAbove = sgGeneral.add(new BoolSetting.Builder()
        .name("require-air-above")
        .description("Only accelerates fall when the space above you is clear.")
        .defaultValue(true)
        .build()
    );

    public FastFall() {
        super(Categories.Movement, "fast-fall", "Falls down faster by adding downward velocity each tick.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null || mc.world == null) return;

        if (onlyWhenSneaking.get() && !mc.player.isSneaking()) return;
        if (mc.player.isOnGround() || mc.player.isTouchingWater() || mc.player.isInLava()) return;

        if (requireAirAbove.get()) {
            var pos = mc.player.getBlockPos();
            if (mc.world.getBlockState(pos.up()).isOpaqueFullCube()) return;
        }

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x, Math.max(vel.y - fallSpeed.get(), -2.5), vel.z);
    }
}
