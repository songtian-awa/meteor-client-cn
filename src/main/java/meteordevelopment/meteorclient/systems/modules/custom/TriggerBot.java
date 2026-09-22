/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class TriggerBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Maximum distance to trigger an attack.")
        .defaultValue(4.0)
        .min(1.0)
        .max(6.0)
        .sliderMin(1.0)
        .sliderMax(6.0)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Ticks to wait between attacks.")
        .defaultValue(2)
        .min(0)
        .max(20)
        .sliderMin(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> playersOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("players-only")
        .description("Only trigger on players.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> holdToTrigger = sgGeneral.add(new BoolSetting.Builder()
        .name("hold-to-trigger")
        .description("Only attack while holding the attack button.")
        .defaultValue(true)
        .build()
    );

    private int delayLeft;

    public TriggerBot() {
        super(Categories.Combat, "trigger-bot", "Attacks entities the moment your crosshair is on them. Subtle and non-tracking: it never moves your view.");
    }

    @Override
    public void onActivate() {
        delayLeft = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (holdToTrigger.get() && !mc.options.attackKey.isPressed()) return;

        if (delayLeft > 0) {
            delayLeft--;
            return;
        }

        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        EntityHitResult hit = (EntityHitResult) mc.crosshairTarget;
        Entity target = hit.getEntity();

        if (!(target instanceof LivingEntity living) || !living.isAlive()) return;
        if (target == mc.player) return;
        if (playersOnly.get() && !(target instanceof PlayerEntity)) return;

        if (mc.player.squaredDistanceTo(target) > range.get() * range.get()) return;

        // Trigger the attack without rotating the view
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(mc.player.getActiveHand());

        delayLeft = delay.get();
    }
}
