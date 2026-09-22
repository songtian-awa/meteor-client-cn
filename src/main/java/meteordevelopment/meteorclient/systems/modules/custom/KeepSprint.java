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

public class KeepSprint extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> onlyOnGround = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-ground")
        .description("Only keep sprint while on the ground.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> requireFood = sgGeneral.add(new BoolSetting.Builder()
        .name("require-food")
        .description("Only keep sprint when you have enough hunger to sprint anyway.")
        .defaultValue(false)
        .build()
    );

    private boolean attacking;

    public KeepSprint() {
        super(Categories.Movement, "keep-sprint", "Keeps your sprint after attacking instead of the vanilla sprint reset, a classic pvp technique.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        boolean wasAttacking = attacking;
        attacking = mc.options.attackKey.isPressed();

        // Only act on the tick after an attack release
        if (!wasAttacking || attacking) return;

        if (onlyOnGround.get() && !mc.player.isOnGround()) return;
        if (requireFood.get() && mc.player.getHungerManager().getFoodLevel() < 6) return;

        if (mc.player.isSneaking()) return;

        mc.player.setSprinting(true);
    }
}
