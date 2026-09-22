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
import net.minecraft.block.Block;
import net.minecraft.block.BedBlock;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BedESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The radius in blocks to scan for beds.")
        .defaultValue(24.0)
        .min(4.0)
        .max(64.0)
        .sliderMin(4.0)
        .sliderMax(64.0)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the bed highlight.")
        .defaultValue(new SettingColor(255, 20, 147, 100))
        .build()
    );

    private final List<BlockPos> beds = new ArrayList<>();

    public BedESP() {
        super(Categories.Render, "bed-esp", "Highlights beds around you so you can avoid or set up bed traps.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        beds.clear();

        int r = (int) Math.ceil(range.get());
        BlockPos center = mc.player.getBlockPos();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.add(x, y, z);
                    Block block = mc.world.getBlockState(pos).getBlock();
                    if (block instanceof BedBlock) {
                        beds.add(pos);
                    }
                }
            }
        }

        for (BlockPos pos : beds) {
            event.renderer.box(pos, color.get(), color.get(), ShapeMode.Both, 0);
        }
    }
}
