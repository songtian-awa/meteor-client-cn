/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class OreESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The radius in blocks to scan for ores.")
        .defaultValue(16.0)
        .min(4.0)
        .max(32.0)
        .sliderMin(4.0)
        .sliderMax(32.0)
        .build()
    );

    private final Setting<Boolean> diamond = sgGeneral.add(new BoolSetting.Builder().name("diamond").description("Highlight diamond ore.").defaultValue(true).build());
    private final Setting<Boolean> ancientDebris = sgGeneral.add(new BoolSetting.Builder().name("ancient-debris").description("Highlight ancient debris.").defaultValue(true).build());
    private final Setting<Boolean> gold = sgGeneral.add(new BoolSetting.Builder().name("gold").description("Highlight gold ore.").defaultValue(false).build());
    private final Setting<Boolean> emerald = sgGeneral.add(new BoolSetting.Builder().name("emerald").description("Highlight emerald ore.").defaultValue(true).build());
    private final Setting<Boolean> iron = sgGeneral.add(new BoolSetting.Builder().name("iron").description("Highlight iron ore.").defaultValue(false).build());
    private final Setting<Boolean> coal = sgGeneral.add(new BoolSetting.Builder().name("coal").description("Highlight coal ore.").defaultValue(false).build());
    private final Setting<Boolean> lapis = sgGeneral.add(new BoolSetting.Builder().name("lapis").description("Highlight lapis ore.").defaultValue(false).build());
    private final Setting<Boolean> redstone = sgGeneral.add(new BoolSetting.Builder().name("redstone").description("Highlight redstone ore.").defaultValue(false).build());
    private final Setting<Boolean> copper = sgGeneral.add(new BoolSetting.Builder().name("copper").description("Highlight copper ore.").defaultValue(false).build());
    private final Setting<Boolean> quartz = sgGeneral.add(new BoolSetting.Builder().name("quartz").description("Highlight nether quartz ore.").defaultValue(false).build());

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the ore highlight.")
        .defaultValue(new SettingColor(0, 255, 255, 60))
        .build()
    );

    public OreESP() {
        super(Categories.Render, "ore-esp", "Highlights valuable ores around you so you never miss diamond or ancient debris.");
    }

    private boolean isSelected(Block block) {
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return diamond.get();
        if (block == Blocks.ANCIENT_DEBRIS) return ancientDebris.get();
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE) return gold.get();
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return emerald.get();
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return iron.get();
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) return coal.get();
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) return lapis.get();
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return redstone.get();
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return copper.get();
        if (block == Blocks.NETHER_QUARTZ_ORE) return quartz.get();
        return false;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        int r = (int) Math.ceil(range.get());
        BlockPos center = mc.player.getBlockPos();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (isSelected(mc.world.getBlockState(pos).getBlock())) {
                        event.renderer.box(pos, color.get(), color.get(), ShapeMode.Both, 0);
                    }
                }
            }
        }
    }
}
