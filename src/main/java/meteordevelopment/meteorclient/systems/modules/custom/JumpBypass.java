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

public class JumpBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> jumpHeight = sgGeneral.add(new DoubleSetting.Builder()
        .name("jump-height")
        .description("Extra upward velocity added when you jump.")
        .defaultValue(0.1)
        .min(0.0)
        .max(0.5)
        .sliderMin(0.0)
        .sliderMax(0.5)
        .build()
    );

    private final Setting<Double> jumpSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("jump-speed")
        .description("Horizontal speed boost applied when jumping.")
        .defaultValue(0.05)
        .min(0.0)
        .max(0.4)
        .sliderMin(0.0)
        .sliderMax(0.4)
        .build()
    );

    public JumpBypass() {
        super(Categories.Movement, "jump-bypass", "Adds extra height and forward momentum to your jumps for easier parkour and pvp movement.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        // Detect the tick the player actually jumps (just left the ground)
        if (!mc.player.isOnGround() && mc.player.getVelocity().y > 0.2) {
            Vec3d vel = mc.player.getVelocity();

            double extraY = jumpHeight.get();
            double boost = jumpSpeed.get();

            if (extraY > 0 || boost > 0) {
                // Forward momentum in look direction
                float yaw = mc.player.getYaw();
                double yawRad = Math.toRadians(yaw);
                double fx = -Math.sin(yawRad) * boost;
                double fz = Math.cos(yawRad) * boost;

                mc.player.setVelocity(vel.x + fx, vel.y + extraY, vel.z + fz);
            }
        }
    }
}
