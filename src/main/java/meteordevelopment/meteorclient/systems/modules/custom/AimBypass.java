/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimBypass extends Module {
    public enum AimMode {
        Nearest("Nearest"),
        Look("Look Target");

        private final String title;

        AimMode(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<AimMode> mode = sgGeneral.add(new EnumSetting.Builder<AimMode>()
        .name("mode")
        .description("Which entity to aim at.")
        .defaultValue(AimMode.Nearest)
        .build()
    );

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("How fast the aim rotates toward the target (degrees per tick).")
        .defaultValue(8.0)
        .min(1.0)
        .max(30.0)
        .sliderMin(1.0)
        .sliderMax(30.0)
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Maximum distance to look for a target.")
        .defaultValue(8.0)
        .min(2.0)
        .max(20.0)
        .sliderMin(2.0)
        .sliderMax(20.0)
        .build()
    );

    private final Setting<Boolean> playersOnly = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
        .name("players-only")
        .description("Only aim at players.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> holdToAim = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
        .name("hold-to-aim")
        .description("Only aim while holding the attack button.")
        .defaultValue(false)
        .build()
    );

    public AimBypass() {
        super(Categories.Combat, "aim-bypass", "Smoothly rotates your view toward a nearby target, simulating a human aim that anti-cheats are less likely to flag.");
    }

    private Entity findTarget() {
        if (mc.world == null || mc.player == null) return null;

        Entity best = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
            if (entity == mc.player) continue;

            if (playersOnly.get() && !(entity instanceof PlayerEntity)) continue;

            double dist = mc.player.squaredDistanceTo(entity);
            if (dist > range.get() * range.get()) continue;

            if (dist < bestDist) {
                bestDist = dist;
                best = entity;
            }
        }

        return best;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (holdToAim.get() && !mc.options.attackKey.isPressed()) return;

        Entity target = findTarget();
        if (target == null) return;

        Vec3d targetPos = target.getEyePos();
        Vec3d playerPos = mc.player.getEyePos();

        double dx = targetPos.x - playerPos.x;
        double dy = targetPos.y - playerPos.y;
        double dz = targetPos.z - playerPos.z;

        double dist = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        // Smoothly interpolate toward target angles
        float yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;

        float step = speed.get().floatValue();
        float newYaw = currentYaw + MathHelper.clamp(yawDiff, -step, step);
        float newPitch = currentPitch + MathHelper.clamp(pitchDiff, -step, step);

        mc.player.setYaw(newYaw);
        mc.player.setPitch(newPitch);
    }
}
