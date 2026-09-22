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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class Combo extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> attackInterval = sgGeneral.add(new DoubleSetting.Builder()
        .name("attack-interval")
        .description("The minimum delay in ticks between attacks.")
        .defaultValue(2)
        .min(0)
        .max(20)
        .sliderMin(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> onlyWhenHoldingAttack = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-holding-attack")
        .description("Only attacks while you are holding the attack key.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> stopOnMiss = sgGeneral.add(new BoolSetting.Builder()
        .name("stop-on-miss")
        .description("Waits for the attack cooldown even after missing a swing.")
        .defaultValue(false)
        .build()
    );

    private int ticksSinceAttack;

    public Combo() {
        super(Categories.Combat, "combo", "Times your attacks to the hit cooldown for consistent, fast combos.");
    }

    @Override
    public void onActivate() {
        ticksSinceAttack = 99;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null || mc.world == null) return;

        ticksSinceAttack++;
        if (ticksSinceAttack < attackInterval.get()) return;
        if (onlyWhenHoldingAttack.get() && !mc.options.attackKey.isPressed()) return;

        HitResult hit = mc.crosshairTarget;
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof PlayerEntity target) {
            if (target != mc.player && target.isAlive() && mc.player.distanceTo(target) <= 4.5) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                ticksSinceAttack = 0;
                return;
            }
        }

        if (stopOnMiss.get()) ticksSinceAttack = 0;
    }
}
