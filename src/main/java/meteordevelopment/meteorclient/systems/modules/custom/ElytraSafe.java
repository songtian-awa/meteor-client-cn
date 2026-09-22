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
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class ElytraSafe extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> altitude = sgGeneral.add(new DoubleSetting.Builder()
        .name("altitude")
        .description("Start braking when you drop below this height above the ground.")
        .defaultValue(8.0)
        .min(2.0)
        .max(32.0)
        .sliderMin(2.0)
        .sliderMax(32.0)
        .build()
    );

    private final Setting<Boolean> autoDetach = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-detach")
        .description("Automatically stop flying when about to crash.")
        .defaultValue(true)
        .build()
    );

    public ElytraSafe() {
        super(Categories.Player, "elytra-safe", "Prevents you from dying to kinetic damage when flying with an elytra.");
    }

    private double getGroundHeight() {
        if (mc.world == null || mc.player == null) return Double.MAX_VALUE;
        return mc.player.getY() - mc.world.getBottomY();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (!mc.player.isGliding()) return;
        if (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) return;

        double groundDist = getGroundHeight();
        if (groundDist > altitude.get()) return;

        // Slow down: dampen the fall speed
        if (mc.player.getVelocity().y < -0.3) {
            mc.player.setVelocity(mc.player.getVelocity().x, -0.3, mc.player.getVelocity().z);
        }

        if (autoDetach.get() && groundDist < 2.0 && mc.player.getVelocity().y < -0.5) {
            // Stop flying to avoid crash damage
            mc.player.stopGliding();
        }
    }
}
