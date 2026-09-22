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
import net.minecraft.block.CropBlock;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoFarm extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("The range in blocks to scan for crops.")
        .defaultValue(5)
        .min(1)
        .max(8)
        .sliderMin(1)
        .sliderMax(8)
        .build()
    );

    private final Setting<Boolean> replant = sgGeneral.add(new BoolSetting.Builder()
        .name("replant")
        .description("Automatically replant seeds after harvesting.")
        .defaultValue(true)
        .build()
    );

    private int cooldown;

    public AutoFarm() {
        super(Categories.Player, "auto-farm", "Automatically harvests and replants mature crops around you.");
    }

    @Override
    public void onActivate() {
        cooldown = 0;
    }

    private boolean isMatureCrop(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return block instanceof CropBlock crop && crop.isMature(mc.world.getBlockState(pos));
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        BlockPos playerPos = mc.player.getBlockPos();
        int r = range.get();

        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                BlockPos pos = playerPos.add(x, 0, z);
                if (!isMatureCrop(pos)) continue;

                // Break the crop
                mc.interactionManager.attackBlock(pos, Direction.UP);

                // Replant if enabled
                if (replant.get()) {
                    meteordevelopment.meteorclient.utils.player.InvUtils.swap(findSeedSlot(), false);
                    mc.interactionManager.interactBlock(mc.player, net.minecraft.util.Hand.MAIN_HAND,
                        new net.minecraft.util.hit.BlockHitResult(pos.toCenterPos(), Direction.UP, pos, false));
                }

                cooldown = 10;
                return;
            }
        }
    }

    private int findSeedSlot() {
        for (int i = 0; i < 9; i++) {
            net.minecraft.item.ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.WHEAT_SEEDS || stack.getItem() == Items.POTATO
                || stack.getItem() == Items.CARROT || stack.getItem() == Items.BEETROOT_SEEDS) {
                return i;
            }
        }
        return mc.player.getInventory().getSelectedSlot();
    }
}

