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
import net.minecraft.util.math.Vec3d;

public class SlimeLaunch extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> bounceMultiplier = sgGeneral.add(new DoubleSetting.Builder()
        .name("bounce-multiplier")
        .description("The vertical velocity multiplier when bouncing on slime blocks.")
        .defaultValue(1.5)
        .min(0.5)
        .max(3)
        .sliderMin(0.5)
        .sliderMax(3)
        .build()
    );

    private final Setting<Boolean> requireJumpKey = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
        .name("require-jump-key")
        .description("Only launches when you are holding the jump key.")
        .defaultValue(false)
        .build()
    );

    public SlimeLaunch() {
        super(Categories.Movement, "slime-launch", "Boosts your bounce on slime blocks for higher, faster launches.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null || mc.world == null) return;
        if (requireJumpKey.get() && !mc.options.jumpKey.isPressed()) return;

        var below = mc.player.getBlockPos().down();
        if (mc.world.getBlockState(below).getBlock() == Blocks.SLIME_BLOCK && mc.player.isOnGround()) {
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, 0.42 * bounceMultiplier.get(), vel.z);
        }
    }
}
