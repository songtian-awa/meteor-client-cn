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
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

public class RedstoneESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> wireColor = sgGeneral.add(new ColorSetting.Builder()
        .name("wire-color")
        .description("The color of redstone wire.")
        .defaultValue(new SettingColor(255, 60, 60, 80))
        .build()
    );

    private final Setting<SettingColor> componentColor = sgGeneral.add(new ColorSetting.Builder()
        .name("component-color")
        .description("The color of redstone components.")
        .defaultValue(new SettingColor(255, 140, 60, 100))
        .build()
    );

    private final Setting<Boolean> showWire = sgGeneral.add(new BoolSetting.Builder()
        .name("show-wire")
        .description("Highlights redstone dust.")
        .defaultValue(true)
        .build()
    );

    private final List<Block> components = List.of(
        Blocks.REDSTONE_TORCH, Blocks.REDSTONE_BLOCK, Blocks.REDSTONE_LAMP,
        Blocks.REPEATER, Blocks.COMPARATOR, Blocks.OBSERVER,
        Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.TARGET,
        Blocks.DISPENSER, Blocks.DROPPER, Blocks.HOPPER,
        Blocks.TRAPPED_CHEST, Blocks.LECTERN, Blocks.LEVER,
        Blocks.STONE_BUTTON, Blocks.OAK_BUTTON, Blocks.TRIPWIRE_HOOK,
        Blocks.DAYLIGHT_DETECTOR, Blocks.NOTE_BLOCK
    );

    public RedstoneESP() {
        super(Categories.Render, "redstone-esp", "Highlights redstone wire and components to help you spot contraptions.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        int range = 24;
        BlockPos center = mc.player.getBlockPos();

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    Block block = mc.world.getBlockState(pos).getBlock();

                    SettingColor c = null;
                    if (showWire.get() && block instanceof RedstoneWireBlock) {
                        c = wireColor.get();
                    }
                    else if (components.contains(block)) {
                        c = componentColor.get();
                    }

                    if (c != null) {
                        Box box = new Box(pos);
                        event.renderer.box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, c, c, ShapeMode.Both, 0);
                    }
                }
            }
        }
    }
}
