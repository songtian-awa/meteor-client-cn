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

public class ClimbBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> climbSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("climb-speed")
        .description("Vertical speed multiplier while climbing ladders, vines or scaffolding.")
        .defaultValue(1.6)
        .min(1.0)
        .max(3.0)
        .sliderMin(1.0)
        .sliderMax(3.0)
        .build()
    );

    private final Setting<Boolean> autoClimb = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
        .name("auto-climb")
        .description("Automatically climb when holding forward against a climbable block.")
        .defaultValue(false)
        .build()
    );

    public ClimbBypass() {
        super(Categories.Movement, "climb-bypass", "Climbs ladders, vines and scaffolding faster than vanilla, with optional auto-climb.");
    }

    private boolean isClimbing() {
        return mc.player != null && mc.player.isClimbing();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (!isClimbing()) return;

        Vec3d vel = mc.player.getVelocity();
        double mult = climbSpeed.get();

        // Boost upward climb speed
        if (vel.y > 0 || mc.options.jumpKey.isPressed()) {
            mc.player.setVelocity(vel.x, Math.max(vel.y * mult, 0.1), vel.z);
        }

        if (autoClimb.get() && mc.options.forwardKey.isPressed() && !mc.options.jumpKey.isPressed()) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0.2, mc.player.getVelocity().z);
        }
    }
}
