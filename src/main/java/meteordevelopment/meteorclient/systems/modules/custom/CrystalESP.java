/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public class CrystalESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the crystal highlight.")
        .defaultValue(new SettingColor(255, 105, 180, 100))
        .build()
    );

    private final Setting<Boolean> renderTargets = sgGeneral.add(new BoolSetting.Builder()
        .name("render-own")
        .description("Also renders crystals that are yours.")
        .defaultValue(true)
        .build()
    );

    public CrystalESP() {
        super(Categories.Render, "crystal-esp", "Highlights end crystals around you.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal) || !crystal.isAlive()) continue;

            double x = MathHelper.lerp(event.tickDelta, crystal.lastRenderX, crystal.getX()) - crystal.getX();
            double y = MathHelper.lerp(event.tickDelta, crystal.lastRenderY, crystal.getY()) - crystal.getY();
            double z = MathHelper.lerp(event.tickDelta, crystal.lastRenderZ, crystal.getZ()) - crystal.getZ();

            Box box = crystal.getBoundingBox();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, color.get(), color.get(), ShapeMode.Both, 0);
        }
    }
}
