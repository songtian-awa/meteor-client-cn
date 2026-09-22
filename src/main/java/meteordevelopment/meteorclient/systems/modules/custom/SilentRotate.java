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
import net.minecraft.util.math.MathHelper;

public class SilentRotate extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("How fast the camera rotates in degrees per tick.")
        .defaultValue(10.0)
        .min(1.0)
        .max(90.0)
        .sliderMin(1.0)
        .sliderMax(90.0)
        .build()
    );

    private final Setting<Boolean> resetOnDeactivate = sgGeneral.add(new BoolSetting.Builder()
        .name("reset-on-deactivate")
        .description("Reset rotation when the module is turned off.")
        .defaultValue(true)
        .build()
    );

    private float startYaw;

    public SilentRotate() {
        super(Categories.Movement, "silent-rotate", "Smoothly rotates your view to the original angle over time, hiding sudden flicks.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        float targetYaw = startYaw;
        float diff = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());

        if (Math.abs(diff) < speed.get()) {
            mc.player.setYaw(targetYaw);
        } else {
            mc.player.setYaw(mc.player.getYaw() + Math.signum(diff) * speed.get().floatValue());
        }
    }

    @Override
    public void onActivate() {
        if (mc.player != null) startYaw = mc.player.getYaw();
    }

    @Override
    public void onDeactivate() {
        if (resetOnDeactivate.get() && mc.player != null) {
            mc.player.setYaw(startYaw);
        }
    }
}
