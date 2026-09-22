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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;

public class AutoShield extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> triggerRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("trigger-range")
        .description("Raise the shield when an entity is within this range.")
        .defaultValue(5.0)
        .min(1.0)
        .max(10.0)
        .sliderMin(1.0)
        .sliderMax(10.0)
        .build()
    );

    private final Setting<Boolean> playersOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("players-only")
        .description("Only raise the shield when a player is nearby.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> autoSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-switch")
        .description("Automatically switch the shield to the offhand if it is in the main hand.")
        .defaultValue(false)
        .build()
    );

    public AutoShield() {
        super(Categories.Combat, "auto-shield", "Automatically holds up your shield when an enemy gets close, blocking incoming attacks.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        ItemStack offhand = mc.player.getOffHandStack();
        if (offhand.getItem() != Items.SHIELD) return;

        boolean threat = false;
        double rangeSq = triggerRange.get() * triggerRange.get();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
            if (entity == mc.player) continue;
            if (playersOnly.get() && !(entity instanceof PlayerEntity)) continue;
            if (mc.player.squaredDistanceTo(entity) <= rangeSq) {
                threat = true;
                break;
            }
        }

        if (threat) {
            // Simulate holding right click to raise the shield
            mc.options.useKey.setPressed(true);
        }
        else {
            mc.options.useKey.setPressed(false);
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.options != null) {
            mc.options.useKey.setPressed(false);
        }
    }
}
