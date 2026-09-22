/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class ClickAssist extends Module {
    public enum Mode {
        Attack("Attack"),
        Use("Use");

        private final String title;

        Mode(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Whether to assist attacks or item use.")
        .defaultValue(Mode.Attack)
        .build()
    );

    private final Setting<Double> cps = sgGeneral.add(new DoubleSetting.Builder()
        .name("cps")
        .description("Clicks per second.")
        .defaultValue(8.0)
        .min(1.0)
        .max(20.0)
        .sliderMin(1.0)
        .sliderMax(20.0)
        .build()
    );

    private final Setting<Double> holdChance = sgGeneral.add(new DoubleSetting.Builder()
        .name("hold-chance")
        .description("Chance (0-1) to hold the button instead of clicking, to look more human.")
        .defaultValue(0.2)
        .min(0.0)
        .max(1.0)
        .sliderMin(0.0)
        .sliderMax(1.0)
        .build()
    );

    private final Setting<Boolean> playersOnly = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
        .name("players-only")
        .description("In Attack mode, only click when targeting players.")
        .defaultValue(false)
        .build()
    );

    private int tickCounter;

    public ClickAssist() {
        super(Categories.Player, "click-assist", "Adds human-like clicking rhythm to your manual attacks, avoiding robotic constant-rate patterns.");
    }

    @Override
    public void onActivate() {
        tickCounter = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        tickCounter++;

        // Vanilla click keys already pressed; we only add natural variance
        boolean clicking = mode.get() == Mode.Attack ? mc.options.attackKey.isPressed() : mc.options.useKey.isPressed();
        if (!clicking) return;

        // With hold chance, just keep the button pressed (nothing to do)
        if (Math.random() < holdChance.get()) return;

        // Compute click interval in ticks
        double interval = 20.0 / cps.get();
        if (tickCounter % Math.max(1, (int) Math.round(interval)) == 0) {
            if (mode.get() == Mode.Attack) {
                if (playersOnly.get()) {
                    if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;
                    if (!(((EntityHitResult) mc.crosshairTarget).getEntity() instanceof PlayerEntity)) return;
                }
                // Trigger an attack if the crosshair is on a valid entity
                if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                    net.minecraft.entity.Entity target = ((EntityHitResult) mc.crosshairTarget).getEntity();
                    if (target instanceof LivingEntity living && living.isAlive() && target != mc.player) {
                        mc.interactionManager.attackEntity(mc.player, target);
                        mc.player.swingHand(mc.player.getActiveHand());
                    }
                }
            }
            else {
                // Use assist: right click
                mc.interactionManager.interactItem(mc.player, mc.player.getActiveHand());
            }
        }
    }
}
