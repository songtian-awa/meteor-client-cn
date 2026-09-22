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
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;

public class ScaffoldBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Smoothly rotate toward the block being placed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onlyOnClick = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-click")
        .description("Only place blocks while holding right click.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> toggleOnFall = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-fall")
        .description("Temporarily stop placing when falling fast to avoid look-bot behavior.")
        .defaultValue(true)
        .build()
    );

    public ScaffoldBypass() {
        super(Categories.Movement, "scaffold-bypass", "Subtly places a block under you when falling, bridging style. Low-speed, no teleporting, human-like.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (onlyOnClick.get() && !mc.options.useKey.isPressed()) return;

        // Only assist when falling, not while standing still
        if (toggleOnFall.get() && mc.player.isOnGround()) return;
        if (toggleOnFall.get() && mc.player.getVelocity().y >= 0) return;

        // The block position under the player's feet
        BlockPos feet = mc.player.getBlockPos().down();

        // Only place if the block below is air
        if (!mc.world.getBlockState(feet).isAir()) return;

        FindItemResult item = InvUtils.findInHotbar(stack -> stack.getItem() instanceof BlockItem);
        if (!item.found()) return;

        BlockUtils.place(feet, item, rotate.get(), 100, false);
    }
}
