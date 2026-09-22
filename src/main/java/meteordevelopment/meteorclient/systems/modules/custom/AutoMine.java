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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

public class AutoMine extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> requireLooking = sgGeneral.add(new BoolSetting.Builder()
        .name("require-looking")
        .description("Only mine when you are looking at a block.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pauseOnGUI = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-on-gui")
        .description("Stop mining while any GUI screen is open.")
        .defaultValue(true)
        .build()
    );

    public AutoMine() {
        super(Categories.Player, "auto-mine", "Continuously mines the block you are looking at.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (pauseOnGUI.get() && mc.currentScreen != null) return;

        if (requireLooking.get() && !(mc.crosshairTarget instanceof BlockHitResult)) return;

        if (mc.crosshairTarget instanceof BlockHitResult hit) {
            mc.interactionManager.attackBlock(hit.getBlockPos(), hit.getSide());
            mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
        }
    }
}
