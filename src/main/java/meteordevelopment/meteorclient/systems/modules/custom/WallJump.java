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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class WallJump extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> boost = sgGeneral.add(new DoubleSetting.Builder()
        .name("boost")
        .description("Vertical boost applied when jumping off a wall.")
        .defaultValue(0.42)
        .min(0.1)
        .max(1.0)
        .sliderMin(0.1)
        .sliderMax(1.0)
        .build()
    );

    private final Setting<Boolean> requireSneak = sgGeneral.add(new BoolSetting.Builder()
        .name("require-sneak")
        .description("Only wall jump while sneaking against a wall.")
        .defaultValue(false)
        .build()
    );

    private boolean lastJumpPressed;

    public WallJump() {
        super(Categories.Movement, "wall-jump", "Jump off walls to climb up vertical surfaces, parkour-style.");
    }

    private BlockPos wallInFront() {
        if (mc.player == null) return null;

        float yaw = mc.player.getYaw();
        Vec3d dir = new Vec3d(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));

        for (int i = 1; i <= 2; i++) {
            BlockPos pos = mc.player.getBlockPos().add((int) Math.round(dir.x * i), 0, (int) Math.round(dir.z * i));
            if (!mc.world.getBlockState(pos).isAir()) return pos;
        }

        return null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        boolean jumpPressed = mc.options.jumpKey.isPressed();

        if (requireSneak.get() && !mc.player.isSneaking()) {
            lastJumpPressed = jumpPressed;
            return;
        }

        BlockPos wall = wallInFront();
        if (wall != null && !mc.player.isOnGround() && jumpPressed && !lastJumpPressed) {
            Vec3d vel = mc.player.getVelocity();
            double y = Math.max(vel.y, boost.get());
            mc.player.setVelocity(vel.x * 0.6, y, vel.z * 0.6);
        }

        lastJumpPressed = jumpPressed;
    }
}
