/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public class VillagerESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the villager highlight.")
        .defaultValue(new SettingColor(0, 255, 170, 100))
        .build()
    );

    private final Setting<Boolean> traders = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
        .name("wandering-traders")
        .description("Also highlight wandering traders.")
        .defaultValue(true)
        .build()
    );

    public VillagerESP() {
        super(Categories.Render, "villager-esp", "Highlights villagers and wandering traders so you can find trades easily.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null) return;

        for (Entity entity : mc.world.getEntities()) {
            boolean isVillager = entity instanceof VillagerEntity;
            boolean isTrader = traders.get() && entity instanceof WanderingTraderEntity;

            if (!isVillager && !isTrader) continue;
            if (!entity.isAlive()) continue;

            double x = MathHelper.lerp(event.tickDelta, entity.lastRenderX, entity.getX()) - entity.getX();
            double y = MathHelper.lerp(event.tickDelta, entity.lastRenderY, entity.getY()) - entity.getY();
            double z = MathHelper.lerp(event.tickDelta, entity.lastRenderZ, entity.getZ()) - entity.getZ();

            Box box = entity.getBoundingBox();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ,
                color.get(), color.get(), ShapeMode.Both, 0);
        }
    }
}
