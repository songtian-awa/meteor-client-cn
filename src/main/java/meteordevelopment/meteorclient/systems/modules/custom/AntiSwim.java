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

public class AntiSwim extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> surfaceSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("surface-speed")
        .description("Upward velocity applied to keep you on the surface of the water.")
        .defaultValue(0.12)
        .min(0.02)
        .max(0.4)
        .sliderMin(0.02)
        .sliderMax(0.4)
        .build()
    );

    private final Setting<Boolean> autoSurface = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-surface")
        .description("Automatically rise to the water surface when not pressing anything.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onlyWhenMoving = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-moving")
        .description("Only apply surface velocity while you are holding a movement key.")
        .defaultValue(false)
        .build()
    );

    public AntiSwim() {
        super(Categories.Movement, "anti-swim", "Keeps you floating on the water surface and prevents accidentally drowning.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (!mc.player.isTouchingWater()) return;

        boolean moving = mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed() || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();

        if (onlyWhenMoving.get() && !moving) return;

        double speed = surfaceSpeed.get();

        // If underwater, add upward velocity to surface
        if (mc.player.isSubmergedInWater() || mc.player.getY() < mc.player.getBlockY() + 0.9) {
            if (mc.player.getVelocity().y < speed) {
                mc.player.setVelocity(mc.player.getVelocity().x, speed, mc.player.getVelocity().z);
            }
        }
    }
}
