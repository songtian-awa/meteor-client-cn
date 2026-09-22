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

public class WaterBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> horizontalSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal-speed")
        .description("Horizontal movement multiplier while in water.")
        .defaultValue(1.5)
        .min(1.0)
        .max(3.0)
        .sliderMin(1.0)
        .sliderMax(3.0)
        .build()
    );

    private final Setting<Double> verticalSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical-speed")
        .description("Vertical movement multiplier while in water (affects dive and surface).")
        .defaultValue(1.3)
        .min(1.0)
        .max(3.0)
        .sliderMin(1.0)
        .sliderMax(3.0)
        .build()
    );

    private final Setting<Boolean> underwaterSprint = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
        .name("underwater-sprint")
        .description("Allows sprinting while fully submerged underwater.")
        .defaultValue(true)
        .build()
    );

    public WaterBypass() {
        super(Categories.Movement, "water-bypass", "Speeds up your movement in water, letting you swim faster than vanilla physics allow.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (!mc.player.isTouchingWater()) return;

        Vec3d vel = mc.player.getVelocity();
        boolean submerged = mc.player.isSubmergedInWater();

        if (submerged && underwaterSprint.get() && mc.options.forwardKey.isPressed()) {
            mc.player.setSprinting(true);
        }

        // Boost horizontal movement in the direction the player is looking
        double h = horizontalSpeed.get();
        double v = verticalSpeed.get();

        Vec3d boost = new Vec3d(vel.x * (h - 1.0), vel.y * (v - 1.0), vel.z * (h - 1.0));

        if (boost.length() > 0.01) {
            mc.player.setVelocity(vel.add(boost));
        }
    }
}
