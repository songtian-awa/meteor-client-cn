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
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoPlant extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Plant seeds on farmland within this range.")
        .defaultValue(4)
        .min(1)
        .max(6)
        .sliderMin(1)
        .sliderMax(6)
        .build()
    );

    private final Setting<Boolean> onlyEmpty = sgGeneral.add(new BoolSetting.Builder()
        .name("only-empty")
        .description("Only plant on empty farmland.")
        .defaultValue(true)
        .build()
    );

    private int cooldown;

    public AutoPlant() {
        super(Categories.Player, "auto-plant", "Automatically plants seeds on empty farmland around you.");
    }

    @Override
    public void onActivate() {
        cooldown = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        // Find seeds in hotbar
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.WHEAT_SEEDS || stack.getItem() == Items.POTATO
                || stack.getItem() == Items.CARROT || stack.getItem() == Items.BEETROOT_SEEDS) {
                slot = i;
                break;
            }
        }

        if (slot == -1) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int r = range.get();

        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                BlockPos pos = playerPos.add(x, 0, z);
                Block block = mc.world.getBlockState(pos).getBlock();
                if (block == Blocks.FARMLAND || block == Blocks.DIRT) {
                    if (onlyEmpty.get() && !mc.world.getBlockState(pos.up()).isAir()) continue;

                    meteordevelopment.meteorclient.utils.player.InvUtils.swap(slot, false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                        new BlockHitResult(pos.up().toCenterPos(), Direction.UP, pos.up(), false));
                    cooldown = 10;
                    return;
                }
            }
        }
    }
}
