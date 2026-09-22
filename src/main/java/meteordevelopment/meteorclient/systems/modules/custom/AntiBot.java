/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class AntiBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> highlight = sgGeneral.add(new BoolSetting.Builder()
        .name("highlight")
        .description("Highlight detected bots.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the bot highlight.")
        .defaultValue(new SettingColor(255, 0, 0, 80))
        .build()
    );

    private final Setting<Double> minHealth = sgGeneral.add(new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
        .name("min-health")
        .description("Players below this health are considered bots.")
        .defaultValue(0.0)
        .min(0.0)
        .max(20.0)
        .sliderMin(0.0)
        .sliderMax(20.0)
        .build()
    );

    private final List<PlayerEntity> bots = new ArrayList<>();

    public AntiBot() {
        super(Categories.Combat, "anti-bot", "Detects and highlights fake players / bots so your aura and ESP ignore them.");
    }

    public boolean isBot(Entity entity) {
        if (!(entity instanceof PlayerEntity player)) return false;
        if (player == mc.player) return false;

        // Common bot indicators
        if (player.getHealth() <= minHealth.get()) return true;
        if (player.getDisplayName() != null && player.getDisplayName().getString().contains("Bot")) return true;

        return false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.world == null) return;

        bots.clear();
        for (Entity entity : mc.world.getEntities()) {
            if (isBot(entity)) bots.add((PlayerEntity) entity);
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!highlight.get()) return;

        for (PlayerEntity bot : bots) {
            double x = MathHelper.lerp(event.tickDelta, bot.lastRenderX, bot.getX()) - bot.getX();
            double y = MathHelper.lerp(event.tickDelta, bot.lastRenderY, bot.getY()) - bot.getY();
            double z = MathHelper.lerp(event.tickDelta, bot.lastRenderZ, bot.getZ()) - bot.getZ();

            Box box = bot.getBoundingBox();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ,
                color.get(), color.get(), ShapeMode.Both, 0);
        }
    }
}
