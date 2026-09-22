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

public class AntiCactus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Steer away from cacti within this range.")
        .defaultValue(2.0)
        .min(0.5)
        .max(4.0)
        .sliderMin(0.5)
        .sliderMax(4.0)
        .build()
    );

    private final Setting<Boolean> warn = sgGeneral.add(new BoolSetting.Builder()
        .name("warn")
        .description("Warn when standing next to a cactus.")
        .defaultValue(true)
        .build()
    );

    private boolean warned;

    public AntiCactus() {
        super(Categories.Movement, "anti-cactus", "Steers you away from cacti so you never take cactus damage again.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        BlockPos pos = mc.player.getBlockPos();
        int r = (int) Math.ceil(range.get());
        boolean nearCactus = false;

        for (int x = -r; x <= r; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -r; z <= r; z++) {
                    if (mc.world.getBlockState(pos.add(x, y, z)).getBlock() == Blocks.CACTUS) {
                        nearCactus = true;
                        break;
                    }
                }
            }
        }

        if (nearCactus) {
            if (warn.get() && !warned) {
                meteordevelopment.meteorclient.utils.player.ChatUtils.infoPrefix("AntiCactus", "Cactus nearby! Steer away.");
                warned = true;
            }

            // Push away from the cactus by moving in the opposite direction
            var vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x * -0.5, vel.y, vel.z * -0.5);
        } else {
            warned = false;
        }
    }
}
