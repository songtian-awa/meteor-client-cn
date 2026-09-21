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
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class SpawnESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> bedColor = sgGeneral.add(new ColorSetting.Builder()
        .name("bed-color")
        .description("The color of beds.")
        .defaultValue(new SettingColor(255, 80, 80, 100))
        .build()
    );

    private final Setting<SettingColor> anchorColor = sgGeneral.add(new ColorSetting.Builder()
        .name("anchor-color")
        .description("The color of respawn anchors.")
        .defaultValue(new SettingColor(80, 160, 255, 100))
        .build()
    );

    public SpawnESP() {
        super(Categories.Render, "spawn-esp", "Highlights beds and respawn anchors, useful for locating spawn points.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        int range = 16;
        BlockPos center = mc.player.getBlockPos();

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    Block block = mc.world.getBlockState(pos).getBlock();

                    if (block instanceof BedBlock || block instanceof RespawnAnchorBlock) {
                        SettingColor color = block instanceof BedBlock ? bedColor.get() : anchorColor.get();
                        Box box = new Box(pos);
                        event.renderer.box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color, color, ShapeMode.Both, 0);
                    }
                }
            }
        }
    }
}
