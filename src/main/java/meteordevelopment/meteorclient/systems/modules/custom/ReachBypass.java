/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class ReachBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> reach = sgGeneral.add(new DoubleSetting.Builder()
        .name("reach")
        .description("Extended attack range. Higher values are more likely to be detected.")
        .defaultValue(4.5)
        .min(3.0)
        .max(6.0)
        .sliderMin(3.0)
        .sliderMax(6.0)
        .build()
    );

    private final Setting<Double> smoothing = sgGeneral.add(new DoubleSetting.Builder()
        .name("smoothing")
        .description("Moves the server-side hit position gradually toward the real position to look natural.")
        .defaultValue(0.3)
        .min(0.0)
        .max(1.0)
        .sliderMin(0.0)
        .sliderMax(1.0)
        .build()
    );

    private final Setting<Boolean> visualOnly = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
        .name("visual-only")
        .description("Only extends the reach visually without touching hit registration.")
        .defaultValue(false)
        .build()
    );

    public ReachBypass() {
        super(Categories.Combat, "reach-bypass", "Extends your attack reach beyond the vanilla limit with optional smoothing to avoid detection.");
    }

    public double getReach() {
        return reach.get();
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (mc.player == null || visualOnly.get()) return;

        Entity target = event.entity;
        if (!(target instanceof LivingEntity living) || !living.isAlive()) return;

        double dist = mc.player.squaredDistanceTo(target);
        double maxDist = reach.get() * reach.get();

        // If target is within extended reach but beyond vanilla reach, teleport player slightly closer server-side
        if (dist <= maxDist && dist > 3.0 * 3.0) {
            double smooth = smoothing.get();
            if (smooth <= 0) return;

            Vec3d playerPos = mc.player.getEntityPos();
            Vec3d targetPos = target.getEntityPos();
            Vec3d diff = targetPos.subtract(playerPos);

            double currentDist = Math.sqrt(dist);
            double desiredDist = currentDist - 0.4; // step a bit closer each attack

            if (desiredDist < 0.5) desiredDist = 0.5;

            Vec3d newPos = targetPos.subtract(diff.normalize().multiply(desiredDist));

            // Only shift horizontally to avoid vertical cheating flags
            mc.player.setPos(newPos.x, playerPos.y, newPos.z);
        }
    }
}
