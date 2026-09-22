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
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public class ArrowESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the arrow highlight.")
        .defaultValue(new SettingColor(255, 255, 0, 120))
        .build()
    );

    public ArrowESP() {
        super(Categories.Render, "arrow-esp", "Highlights arrows and other projectiles flying around you so you can dodge them.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ProjectileEntity projectile) || !projectile.isAlive()) continue;

            double x = MathHelper.lerp(event.tickDelta, projectile.lastRenderX, projectile.getX()) - projectile.getX();
            double y = MathHelper.lerp(event.tickDelta, projectile.lastRenderY, projectile.getY()) - projectile.getY();
            double z = MathHelper.lerp(event.tickDelta, projectile.lastRenderZ, projectile.getZ()) - projectile.getZ();

            Box box = projectile.getBoundingBox();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ,
                x + box.maxX, y + box.maxY, z + box.maxZ,
                color.get(), color.get(), ShapeMode.Both, 0);
        }
    }
}
