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

public class SprintBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> ignoreHunger = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-hunger")
        .description("Sprint even when your hunger bar is below the vanilla threshold.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onlyWhenMoving = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-moving")
        .description("Only sprint when you are pressing a movement key.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onlyForward = sgGeneral.add(new BoolSetting.Builder()
        .name("only-forward")
        .description("Only sprint when looking straight ahead while moving forward.")
        .defaultValue(false)
        .build()
    );

    public SprintBypass() {
        super(Categories.Movement, "sprint-bypass", "Lets you sprint even when hungry, bypassing the vanilla food-level check servers rely on.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (ignoreHunger.get() && mc.player.getHungerManager().getFoodLevel() < 6) {
            // Vanilla blocks sprinting below 6 hunger; force the sprint flag client-side.
            if (mc.player.isOnGround() && !mc.player.isSneaking()) {
                boolean moving = mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed() || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
                if (!onlyWhenMoving.get() || moving) {
                    if (!onlyForward.get() || (mc.options.forwardKey.isPressed() && mc.player.getMovementSpeed() > 0)) {
                        mc.player.setSprinting(true);
                    }
                }
            }
        }
    }
}
