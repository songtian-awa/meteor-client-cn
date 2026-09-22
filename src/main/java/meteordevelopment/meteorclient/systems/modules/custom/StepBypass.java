/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
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

public class StepBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> onlyWhenMoving = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-moving")
        .description("Only steps up while you are actively moving.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> jumpTiming = sgGeneral.add(new BoolSetting.Builder()
        .name("jump-timing")
        .description("Times the jump to look like a real player jumping, reducing anti-cheat suspicion.")
        .defaultValue(true)
        .build()
    );

    private int jumpCooldown;

    public StepBypass() {
        super(Categories.Movement, "step-bypass", "Auto-jumps when a full block blocks your path, letting you climb steps using natural jumps instead of modified step height.");
    }

    @Override
    public void onActivate() {
        jumpCooldown = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;
        if (jumpCooldown > 0) {
            jumpCooldown--;
            return;
        }

        boolean moving = mc.player.input.playerInput.forward() || mc.player.input.playerInput.backward()
            || mc.player.input.playerInput.left() || mc.player.input.playerInput.right();
        if (onlyWhenMoving.get() && !moving) return;

        // Check the block directly in front of the player
        Direction facing = mc.player.getHorizontalFacing();
        BlockPos frontPos = mc.player.getBlockPos().offset(facing);

        Block frontBlock = mc.world.getBlockState(frontPos).getBlock();
        Block frontUpper = mc.world.getBlockState(frontPos.up()).getBlock();
        Block headBlock = mc.world.getBlockState(mc.player.getBlockPos().up()).getBlock();

        boolean frontSolid = isSolid(frontBlock) && !isSolid(frontUpper);
        boolean headClear = !isSolid(headBlock);

        if (frontSolid && headClear) {
            mc.player.jump();
            jumpCooldown = jumpTiming.get() ? 4 : 1;
        }
    }

    private static boolean isSolid(Block block) {
        return block != Blocks.AIR && block != Blocks.CAVE_AIR && block != Blocks.VOID_AIR
            && block != Blocks.WATER && block != Blocks.LAVA && block != Blocks.TALL_GRASS
            && block != Blocks.SHORT_GRASS;
    }
}
