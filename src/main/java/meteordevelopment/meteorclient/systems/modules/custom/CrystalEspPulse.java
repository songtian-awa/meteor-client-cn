/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
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

public class CrystalEspPulse extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The radius in blocks to highlight crystals.")
        .defaultValue(24.0)
        .min(4.0)
        .max(64.0)
        .sliderMin(4.0)
        .sliderMax(64.0)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The pulse color of the crystal highlight.")
        .defaultValue(new SettingColor(255, 0, 100, 120))
        .build()
    );

    private final Setting<Double> pulseSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("pulse-speed")
        .description("How fast the highlight pulses.")
        .defaultValue(2.0)
        .min(0.5)
        .max(8.0)
        .sliderMin(0.5)
        .sliderMax(8.0)
        .build()
    );

    private double time;

    public CrystalEspPulse() {
        super(Categories.Render, "crystal-esp-pulse", "Highlights end crystals with a pulsing outline so they stand out clearly.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        time += event.frameTime * pulseSpeed.get();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal) || !crystal.isAlive()) continue;
            if (mc.player.squaredDistanceTo(crystal) > range.get() * range.get()) continue;

            double pulse = 0.5 + 0.5 * Math.sin(time);
            int alpha = (int) (60 + pulse * 120);

            SettingColor c = new SettingColor(color.get().r, color.get().g, color.get().b, alpha);

            double x = MathHelper.lerp(event.tickDelta, crystal.lastRenderX, crystal.getX()) - crystal.getX();
            double y = MathHelper.lerp(event.tickDelta, crystal.lastRenderY, crystal.getY()) - crystal.getY();
            double z = MathHelper.lerp(event.tickDelta, crystal.lastRenderZ, crystal.getZ()) - crystal.getZ();

            Box box = crystal.getBoundingBox();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ,
                c, c, ShapeMode.Lines, 0);
        }
    }
}
