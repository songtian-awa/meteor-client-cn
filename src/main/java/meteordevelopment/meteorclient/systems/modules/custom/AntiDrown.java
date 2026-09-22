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
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class AntiDrown extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> oxygenThreshold = sgGeneral.add(new DoubleSetting.Builder()
        .name("oxygen-threshold")
        .description("The oxygen level at which to start surfacing (1 = full).")
        .defaultValue(0.5)
        .min(0.1)
        .max(1)
        .sliderMin(0.1)
        .sliderMax(1)
        .build()
    );

    private final Setting<Boolean> holdSpaceToSurface = sgGeneral.add(new BoolSetting.Builder()
        .name("hold-space-to-surface")
        .description("Only surfaces while you hold space.")
        .defaultValue(false)
        .build()
    );

    public AntiDrown() {
        super(Categories.Player, "anti-drown", "Automatically keeps your head above water so you never drown.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null || mc.world == null) return;
        if (holdSpaceToSurface.get() && !mc.options.jumpKey.isPressed()) return;

        if (!mc.player.isTouchingWater()) return;

        int air = mc.player.getAir();
        int maxAir = mc.player.getMaxAir();
        double ratio = air / (double) maxAir;

        if (ratio <= oxygenThreshold.get()) {
            // Jump to reach the surface
            mc.player.jump();

            // Check if the block above is water; if so swim up harder
            BlockPos headPos = mc.player.getBlockPos().up();
            if (mc.world.getBlockState(headPos).getBlock() == Blocks.WATER) {
                var vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x, 0.3, vel.z);
            }
        }
    }
}
