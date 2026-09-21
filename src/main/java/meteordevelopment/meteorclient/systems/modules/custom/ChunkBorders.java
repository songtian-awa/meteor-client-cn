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
import net.minecraft.util.math.BlockPos;

public class ChunkBorders extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of chunk borders.")
        .defaultValue(new SettingColor(80, 220, 255, 150))
        .build()
    );

    private final Setting<Boolean> showY = sgGeneral.add(new BoolSetting.Builder()
        .name("show-y")
        .description("Renders vertical lines at chunk corners.")
        .defaultValue(true)
        .build()
    );

    public ChunkBorders() {
        super(Categories.Render, "chunk-borders", "Renders the borders of the chunk you are currently in.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        BlockPos center = mc.player.getBlockPos();
        int chunkX = center.getX() >> 4;
        int chunkZ = center.getZ() >> 4;

        int x1 = chunkX << 4;
        int z1 = chunkZ << 4;
        int x2 = x1 + 16;
        int z2 = z1 + 16;

        double y = mc.player.getY();
        SettingColor c = color.get();

        // Bottom rectangle at player Y
        event.renderer.line(x1, y, z1, x2, y, z1, c);
        event.renderer.line(x2, y, z1, x2, y, z2, c);
        event.renderer.line(x2, y, z2, x1, y, z2, c);
        event.renderer.line(x1, y, z2, x1, y, z1, c);

        if (showY.get()) {
            int topY = mc.world.getTopYInclusive();
            event.renderer.line(x1, y, z1, x1, topY, z1, c);
            event.renderer.line(x2, y, z1, x2, topY, z1, c);
            event.renderer.line(x2, y, z2, x2, topY, z2, c);
            event.renderer.line(x1, y, z2, x1, topY, z2, c);
        }
    }
}
