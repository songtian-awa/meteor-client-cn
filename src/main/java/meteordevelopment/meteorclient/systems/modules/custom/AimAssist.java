/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimAssist extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum distance to aim at targets.")
        .defaultValue(6)
        .min(1)
        .max(10)
        .sliderMin(1)
        .sliderMax(10)
        .build()
    );

    private final Setting<Double> fov = sgGeneral.add(new DoubleSetting.Builder()
        .name("fov")
        .description("The field of view to search for targets in.")
        .defaultValue(90)
        .min(10)
        .max(180)
        .sliderMin(10)
        .sliderMax(180)
        .build()
    );

    private final Setting<Double> smoothness = sgGeneral.add(new DoubleSetting.Builder()
        .name("smoothness")
        .description("How smooth the aim assist is. Lower = smoother.")
        .defaultValue(6)
        .min(1)
        .max(20)
        .sliderMin(1)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> onlyWhenClicking = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-clicking")
        .description("Only assists aim while you are holding left click.")
        .defaultValue(false)
        .build()
    );

    public AimAssist() {
        super(Categories.Combat, "aim-assist", "Smoothly moves your crosshair toward the nearest enemy, making tracking easier.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null || mc.world == null) return;
        if (onlyWhenClicking.get() && !mc.options.attackKey.isPressed()) return;

        Entity target = null;
        double best = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity p) || p == mc.player || !p.isAlive() || p.isRemoved()) continue;

            double dist = mc.player.distanceTo(p);
            if (dist > range.get()) continue;

            Vec3d lookVec = mc.player.getRotationVec(1.0f);
            Vec3d toTarget = p.getPos().add(0, p.getHeight() / 2, 0).subtract(mc.player.getEyePos()).normalize();
            double angle = Math.toDegrees(Math.acos(MathHelper.clamp(lookVec.dotProduct(toTarget), -1, 1)));

            if (angle <= fov.get() / 2 && dist < best) {
                best = dist;
                target = p;
            }
        }

        if (target == null) return;

        double yaw = Rotations.getYaw(target);
        double pitch = Rotations.getPitch(target);

        double currentYaw = mc.player.getYaw();
        double currentPitch = mc.player.getPitch();

        double dYaw = MathHelper.wrapDegrees(yaw - currentYaw);
        double dPitch = pitch - currentPitch;

        double factor = Math.min(1, 1 / smoothness.get());
        mc.player.setYaw((float) (currentYaw + dYaw * factor));
        mc.player.setPitch((float) (currentPitch + dPitch * factor));
    }
}
