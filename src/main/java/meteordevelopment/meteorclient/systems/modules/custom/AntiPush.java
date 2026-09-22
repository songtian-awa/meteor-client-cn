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

public class AntiPush extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> horizontalResistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal-resistance")
        .description("How much of the external horizontal push to cancel. 1 = fully prevent pushing.")
        .defaultValue(1)
        .min(0)
        .max(1)
        .sliderMin(0)
        .sliderMax(1)
        .build()
    );

    private final Setting<Double> verticalResistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical-resistance")
        .description("How much of the external vertical push to cancel.")
        .defaultValue(0)
        .min(0)
        .max(1)
        .sliderMin(0)
        .sliderMax(1)
        .build()
    );

    public AntiPush() {
        super(Categories.Movement, "anti-push", "Prevents you from being pushed around by entities and external forces.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        Vec3d vel = mc.player.getVelocity();

        // Only cancel velocity that isn't caused by the player's own input
        boolean moving = mc.player.input.playerInput.forward() || mc.player.input.playerInput.backward()
            || mc.player.input.playerInput.left() || mc.player.input.playerInput.right();

        if (!moving) {
            double hx = vel.x * (1 - horizontalResistance.get());
            double hz = vel.z * (1 - horizontalResistance.get());
            double vy = vel.y * (1 - verticalResistance.get());

            // Preserve downward gravity a bit so the player still falls normally
            if (vy > 0) vy = vel.y * (1 - verticalResistance.get());

            mc.player.setVelocity(hx, vy, hz);
        }
    }
}
