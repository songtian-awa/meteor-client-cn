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
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;

public class FastPlace extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> onlyBlocks = sgGeneral.add(new BoolSetting.Builder()
        .name("only-blocks")
        .description("Only speed up block placement, not other items.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pauseWhenLooking = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-when-looking")
        .description("Pause speed-up when the item cooldown is already short.")
        .defaultValue(true)
        .build()
    );

    public FastPlace() {
        super(Categories.Player, "fast-place", "Reduces the placement cooldown between blocks so you can build faster and more smoothly.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (!mc.options.useKey.isPressed()) return;

        // Only affect block items
        if (onlyBlocks.get() && !(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;

        if (pauseWhenLooking.get() && mc.player.getItemCooldownManager().isCoolingDown(mc.player.getMainHandStack())) return;

        // If the player is looking at a block and not placing, do nothing extra.
        // The cooldown is shortened by resetting the item cooldown each tick.
        if (mc.crosshairTarget instanceof BlockHitResult) {
            BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
            BlockState state = mc.world.getBlockState(pos);

            // Don't interact with special blocks like chests, etc.
            if (state.getBlock() == Blocks.CHEST || state.getBlock() == Blocks.ENDER_CHEST || state.getBlock() == Blocks.CRAFTING_TABLE) return;

            // Reset the 4-tick placement cooldown
            mc.player.getItemCooldownManager().set(mc.player.getMainHandStack(), 0);
        }
    }
}
