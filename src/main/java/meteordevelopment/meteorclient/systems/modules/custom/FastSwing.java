/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.DoAttackEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;

public class FastSwing extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> extraSwings = sgGeneral.add(new IntSetting.Builder()
        .name("extra-swings")
        .description("How many extra swings to play per attack.")
        .defaultValue(1)
        .min(1)
        .max(3)
        .sliderMin(1)
        .sliderMax(3)
        .build()
    );

    public FastSwing() {
        super(Categories.Player, "fast-swing", "Plays extra swing animations after each attack so your hits look faster and more aggressive.");
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        for (int i = 0; i < extraSwings.get(); i++) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
