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
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

public class EntityHealth extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum range to render health bars.")
        .defaultValue(8)
        .min(1)
        .max(32)
        .sliderMin(1)
        .sliderMax(32)
        .build()
    );

    private final Setting<Boolean> showHostile = sgGeneral.add(new BoolSetting.Builder()
        .name("show-hostile")
        .description("Shows health bars above hostile mobs.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showPassive = sgGeneral.add(new BoolSetting.Builder()
        .name("show-passive")
        .description("Shows health bars above passive mobs.")
        .defaultValue(false)
        .build()
    );

    public EntityHealth() {
        super(Categories.Render, "entity-health", "Renders health bars above entities.");
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.world == null || mc.player == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || entity == mc.player || !living.isAlive()) continue;

            if (entity instanceof PlayerEntity) {
                // always show players
            }
            else if (entity.getType().getSpawnGroup() == net.minecraft.entity.SpawnGroup.MONSTER) {
                if (!showHostile.get()) continue;
            }
            else {
                if (!showPassive.get()) continue;
            }

            if (mc.player.distanceTo(entity) > range.get()) continue;

            float health = living.getHealth();
            float max = living.getMaxHealth();
            float percent = MathHelper.clamp(health / max, 0, 1);

            Vec3d pos = new Vec3d(entity.getX(), entity.getY(), entity.getZ()).add(0, entity.getHeight() + 0.4, 0);

            Vector3d vec = new Vector3d(pos.x, pos.y, pos.z);
            if (!NametagUtils.to2D(vec, 1)) continue;

            int barWidth = 30;
            int barHeight = 4;
            int x = (int) vec.x - barWidth / 2;
            int y = (int) vec.y;

            int hpColor = percent > 0.5 ? 0x50C878 : percent > 0.25 ? 0xFFA500 : 0xFF4040;

            event.drawContext.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xFF000000);
            int fillW = (int) (barWidth * percent);
            if (fillW > 0) event.drawContext.fill(x, y, x + fillW, y + barHeight, hpColor);
        }
    }
}
