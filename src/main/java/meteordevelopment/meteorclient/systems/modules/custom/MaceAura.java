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
import net.minecraft.item.MaceItem;
import net.minecraft.util.Hand;

public class MaceAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Attack range in blocks.")
        .defaultValue(3.0)
        .min(1.0)
        .max(5.0)
        .sliderMin(1.0)
        .sliderMax(5.0)
        .build()
    );

    private final Setting<Double> minFallDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("min-fall-distance")
        .description("Minimum fall distance to trigger the smash attack.")
        .defaultValue(1.5)
        .min(0.0)
        .max(5.0)
        .sliderMin(0.0)
        .sliderMax(5.0)
        .build()
    );

    private final Setting<BoolSetting> requireHolding = sgGeneral.add(new BoolSetting.Builder()
        .name("require-holding")
        .description("Only attack when holding a mace.")
        .defaultValue(true)
        .build()
    );

    public MaceAura() {
        super(Categories.Combat, "mace-aura", "Automatically smashes nearby players with the mace when you have fall momentum.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (requireHolding.get() && !(mc.player.getMainHandStack().getItem() instanceof MaceItem)) return;

        if (mc.player.fallDistance < minFallDistance.get()) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity instanceof PlayerEntity && entity == mc.player) continue;
            if (!living.isAlive()) continue;

            double dist = mc.player.squaredDistanceTo(entity);
            if (dist > range.get() * range.get()) continue;

            mc.interactionManager.attackEntity(mc.player, entity);
            mc.player.swingHand(Hand.MAIN_HAND);
            break;
        }
    }
}
