/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class TargetHud extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum range to track a target.")
        .defaultValue(6)
        .min(1)
        .max(20)
        .sliderMin(1)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> showHealth = sgGeneral.add(new BoolSetting.Builder()
        .name("show-health")
        .description("Shows the target's health.")
        .defaultValue(true)
        .build()
    );

    private LivingEntity target;

    public TargetHud() {
        super(Categories.Combat, "target-hud", "Displays information about the entity you are currently targeting.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null || mc.world == null) {
            target = null;
            return;
        }

        target = null;
        double best = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || entity == mc.player || !living.isAlive()) continue;
            if (!(entity instanceof PlayerEntity) && !entity.isPlayer()) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist <= range.get() && dist < best) {
                best = dist;
                target = living;
            }
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (target == null || mc.player == null) return;

        int x = 10;
        int y = 10;

        String name = target.getName().getString();
        double dist = mc.player.distanceTo(target);

        if (target instanceof PlayerEntity p) {
            float health = p.getHealth() + p.getAbsorptionAmount();
            float max = p.getMaxHealth();

            int hpColor = health > 15 ? 0x50C878 : health > 8 ? 0xFFA500 : 0xFF4040;
            int barWidth = 120;
            int barHeight = 8;

            if (showHealth.get()) {
                String hpText = String.format("%.1f / %.1f", health, max);
                event.drawContext.drawTextWithShadow(mc.textRenderer, name + "  " + hpText, x, y, hpColor);
            }
            else {
                event.drawContext.drawTextWithShadow(mc.textRenderer, name, x, y, 0xFFFFFF);
            }

            // Health bar
            event.drawContext.fill(x, y + 12, x + barWidth, y + 12 + barHeight, 0x80000000);
            int fillW = (int) (barWidth * Math.min(1, health / max));
            event.drawContext.fill(x, y + 12, x + fillW, y + 12 + barHeight, hpColor);

            // Distance text
            event.drawContext.drawTextWithShadow(mc.textRenderer, String.format("%.1f blocks", dist), x, y + 12 + barHeight + 3, 0xAAAAAA);
        }
        else {
            event.drawContext.drawTextWithShadow(mc.textRenderer, name, x, y, 0xFFFFFF);
        }
    }

    @Override
    public void onDeactivate() {
        target = null;
    }
}
