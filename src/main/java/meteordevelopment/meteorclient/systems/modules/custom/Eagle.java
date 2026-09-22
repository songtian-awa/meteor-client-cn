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
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Eagle extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> onlyWhenMoving = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-moving")
        .description("Only sneak at edges while you are moving.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showDebug = sgGeneral.add(new BoolSetting.Builder()
        .name("show-debug")
        .description("Print in chat when the edge is detected.")
        .defaultValue(false)
        .build()
    );

    public Eagle() {
        super(Categories.Movement, "eagle", "Automatically sneaks when you reach the edge of a block, preventing accidental falls while bridging or building.");
    }

    private boolean atEdge() {
        if (mc.player == null) return false;

        BlockPos pos = mc.player.getBlockPos();
        double x = mc.player.getX() - pos.getX();
        double z = mc.player.getZ() - pos.getZ();

        // Check the four sides of the block the player is standing on
        BlockPos below = pos.down();
        BlockState belowState = mc.world.getBlockState(below);

        if (belowState.isAir()) return false;

        // Check if any side around the player is air (an edge)
        boolean edge = false;

        if (isAirBeside(below, Direction.NORTH, x, z, true)) edge = true;
        if (isAirBeside(below, Direction.SOUTH, x, z, false)) edge = true;
        if (isAirBeside(below, Direction.WEST, x, z, true)) edge = true;
        if (isAirBeside(below, Direction.EAST, x, z, false)) edge = true;

        return edge;
    }

    private boolean isAirBeside(BlockPos below, Direction dir, double x, double z, boolean isPositive) {
        // Approximate check based on player position within the block
        double edge = isPositive ? 0.2 : 0.8;
        boolean nearEdge;
        if (dir == Direction.NORTH) nearEdge = z < edge;
        else if (dir == Direction.SOUTH) nearEdge = z > edge;
        else if (dir == Direction.WEST) nearEdge = x < edge;
        else nearEdge = x > edge;

        if (!nearEdge) return false;

        BlockPos neighbor = below.offset(dir);
        return mc.world.getBlockState(neighbor).isAir();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (onlyWhenMoving.get() && !mc.options.forwardKey.isPressed() && !mc.options.backKey.isPressed() && !mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed()) {
            mc.options.sneakKey.setPressed(false);
            return;
        }

        boolean edge = atEdge();
        mc.options.sneakKey.setPressed(edge);
    }

    @Override
    public void onDeactivate() {
        if (mc.options != null) {
            mc.options.sneakKey.setPressed(false);
        }
    }
}
