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
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class WebBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> keepJump = sgGeneral.add(new BoolSetting.Builder()
        .name("keep-jump")
        .description("Keep your jump velocity when touching cobwebs.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> noSlowDown = sgGeneral.add(new BoolSetting.Builder()
        .name("no-slow-down")
        .description("Prevent the cobweb from slowing you down at all.")
        .defaultValue(true)
        .build()
    );

    public WebBypass() {
        super(Categories.Movement, "web-bypass", "Cancels the cobweb slowdown so you can move through webs at full speed.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        BlockPos pos = mc.player.getBlockPos();
        boolean inWeb = mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB
            || mc.world.getBlockState(pos.up()).getBlock() == Blocks.COBWEB;

        if (!inWeb) return;

        if (noSlowDown.get()) {
            // Restore full speed each tick while inside the web
            mc.player.setVelocity(mc.player.getVelocity().multiply(1.0 / 0.4, 1.0, 1.0 / 0.4));
        }

        if (keepJump.get() && mc.options.jumpKey.isPressed() && mc.player.getVelocity().y < 0.4) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
        }
    }
}
