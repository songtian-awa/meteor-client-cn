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

public class AirControl extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> controlStrength = sgGeneral.add(new DoubleSetting.Builder()
        .name("control-strength")
        .description("How strongly you can steer in mid-air with the movement keys.")
        .defaultValue(0.25)
        .min(0.05)
        .max(1.0)
        .sliderMin(0.05)
        .sliderMax(1.0)
        .build()
    );

    private final Setting<Double> maxSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-speed")
        .description("Maximum horizontal speed while airborne.")
        .defaultValue(0.4)
        .min(0.1)
        .max(1.0)
        .sliderMin(0.1)
        .sliderMax(1.0)
        .build()
    );

    public AirControl() {
        super(Categories.Movement, "air-control", "Gives you some control over your movement while in the air, similar to creative mode steering.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        // Only apply when airborne
        if (mc.player.isOnGround() || mc.player.isTouchingWater() || mc.player.isInLava()) return;

        double strength = controlStrength.get();

        // Horizontal input in the direction the player is facing (yaw only)
        float yaw = mc.player.getYaw();
        double forward = 0, strafe = 0;
        if (mc.options.forwardKey.isPressed()) forward += 1;
        if (mc.options.backKey.isPressed()) forward -= 1;
        if (mc.options.rightKey.isPressed()) strafe += 1;
        if (mc.options.leftKey.isPressed()) strafe -= 1;

        if (forward == 0 && strafe == 0) return;

        double yawRad = Math.toRadians(yaw);
        double sin = Math.sin(yawRad);
        double cos = Math.cos(yawRad);

        double targetX = (-sin * forward + cos * strafe) * strength;
        double targetZ = (cos * forward + sin * strafe) * strength;

        Vec3d vel = mc.player.getVelocity();
        double newX = vel.x + targetX;
        double newZ = vel.z + targetZ;

        // Clamp to max horizontal speed
        double horizontal = Math.sqrt(newX * newX + newZ * newZ);
        double max = maxSpeed.get();
        if (horizontal > max) {
            double scale = max / horizontal;
            newX *= scale;
            newZ *= scale;
        }

        mc.player.setVelocity(newX, vel.y, newZ);
    }
}
