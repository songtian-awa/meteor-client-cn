/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class TreeAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("The range in blocks to scan for logs.")
        .defaultValue(5)
        .min(1)
        .max(10)
        .sliderMin(1)
        .sliderMax(10)
        .build()
    );

    private final Setting<Integer> maxBlocks = sgGeneral.add(new IntSetting.Builder()
        .name("max-blocks")
        .description("How many logs to mine per tree.")
        .defaultValue(32)
        .min(1)
        .max(64)
        .sliderMin(1)
        .sliderMax(64)
        .build()
    );

    private final Setting<Boolean> autoTool = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-tool")
        .description("Automatically switch to an axe.")
        .defaultValue(true)
        .build()
    );

    private final Set<BlockPos> queued = new HashSet<>();
    private final Queue<BlockPos> queue = new ArrayDeque<>();

    public TreeAura() {
        super(Categories.Player, "tree-aura", "Automatically mines entire trees for you, log by log.");
    }

    @Override
    public void onActivate() {
        queued.clear();
        queue.clear();
    }

    private boolean isLog(Block block) {
        return block == Blocks.OAK_LOG || block == Blocks.SPRUCE_LOG || block == Blocks.BIRCH_LOG
            || block == Blocks.JUNGLE_LOG || block == Blocks.ACACIA_LOG || block == Blocks.DARK_OAK_LOG
            || block == Blocks.MANGROVE_LOG || block == Blocks.CHERRY_LOG || block == Blocks.CRIMSON_STEM
            || block == Blocks.WARPED_STEM;
    }

    private void search(BlockPos start) {
        queued.add(start);
        queue.add(start);

        while (!queue.isEmpty() && queued.size() < maxBlocks.get()) {
            BlockPos pos = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos next = pos.offset(dir);
                if (queued.contains(next)) continue;
                if (mc.world.getBlockState(next).getBlock() instanceof net.minecraft.block.PillarBlock
                    && isLog(mc.world.getBlockState(next).getBlock())) {
                    queued.add(next);
                    queue.add(next);
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (queued.isEmpty()) {
            // Find the nearest log
            BlockPos playerPos = mc.player.getBlockPos();
            int r = range.get();
            BlockPos target = null;
            double best = Double.MAX_VALUE;

            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = playerPos.add(x, y, z);
                        if (isLog(mc.world.getBlockState(pos).getBlock())) {
                            double dist = pos.getSquaredDistance(playerPos);
                            if (dist < best) {
                                best = dist;
                                target = pos;
                            }
                        }
                    }
                }
            }

            if (target != null) search(target);
            else return;
        }

        // Mine the next queued log
        BlockPos next = queue.peek();
        if (next == null) return;

        if (mc.player.squaredDistanceTo(next.toCenterPos()) > (range.get() * range.get())) {
            queue.clear();
            queued.clear();
            return;
        }

        mc.interactionManager.attackBlock(next, Direction.UP);
        queue.poll();
    }
}
