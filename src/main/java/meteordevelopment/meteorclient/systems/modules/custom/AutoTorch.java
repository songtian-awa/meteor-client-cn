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
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoTorch extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> lightLevel = sgGeneral.add(new IntSetting.Builder()
        .name("light-level")
        .description("Place a torch when the block light is at or below this level.")
        .defaultValue(7)
        .min(0)
        .max(15)
        .sliderMin(0)
        .sliderMax(15)
        .build()
    );

    private final Setting<Boolean> onlyCave = sgGeneral.add(new BoolSetting.Builder()
        .name("only-cave")
        .description("Only place torches when you cannot see the sky.")
        .defaultValue(true)
        .build()
    );

    private int cooldown;

    public AutoTorch() {
        super(Categories.Player, "auto-torch", "Automatically places torches in dark areas so you never get lost in caves.");
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

        BlockPos pos = mc.player.getBlockPos();

        if (onlyCave.get() && mc.world.isSkyVisible(pos)) return;

        int light = mc.world.getLightLevel(pos);
        if (light > lightLevel.get()) return;

        // Find a torch in the hotbar
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TORCH || mc.player.getInventory().getStack(i).getItem() == Items.SOUL_TORCH) {
                slot = i;
                break;
            }
        }

        if (slot == -1) return;

        // Place on the wall if possible, otherwise on the floor
        BlockPos placePos = null;
        for (Direction dir : Direction.values()) {
            if (dir == Direction.DOWN) continue;
            BlockPos neighbor = pos.offset(dir);
            if (!mc.world.getBlockState(neighbor).isAir()) {
                placePos = neighbor;
                break;
            }
        }

        if (placePos == null) return;

        InvUtils.swap(slot, true);

        Vec3d hitPos = Vec3d.ofCenter(placePos);
        BlockHitResult hit = new BlockHitResult(hitPos, Direction.UP, placePos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);

        cooldown = 20;
    }
}
