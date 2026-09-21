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

public class Glide extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> glideSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("glide-speed")
        .description("How slow you fall while gliding.")
        .defaultValue(0.05)
        .min(0.01)
        .max(0.3)
        .sliderMin(0.01)
        .sliderMax(0.3)
        .build()
    );

    private final Setting<Boolean> holdSpaceToGlide = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
        .name("hold-space-to-glide")
        .description("Only glides while you hold the space bar.")
        .defaultValue(false)
        .build()
    );

    public Glide() {
        super(Categories.Movement, "glide", "Slows down your fall speed, letting you glide through the air.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (holdSpaceToGlide.get() && !mc.options.jumpKey.isPressed()) return;

        Vec3d velocity = mc.player.getVelocity();
        if (velocity.y < 0) {
            mc.player.setVelocity(velocity.x, Math.max(velocity.y, -glideSpeed.get()), velocity.z);
        }
    }
}
