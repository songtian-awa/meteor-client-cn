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
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffectInstance;

public class PotionTimer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> warnBelow = sgGeneral.add(new IntSetting.Builder()
        .name("warn-below")
        .description("Warn when a potion effect has this many seconds left.")
        .defaultValue(5)
        .min(1)
        .max(60)
        .sliderMin(1)
        .sliderMax(60)
        .build()
    );

    private final Setting<Boolean> announcePickup = sgGeneral.add(new BoolSetting.Builder()
        .name("announce-pickup")
        .description("Announce when you gain a potion effect.")
        .defaultValue(true)
        .build()
    );

    public PotionTimer() {
        super(Categories.Misc, "potion-timer", "Alerts you when your potion effects are about to run out.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        for (StatusEffectInstance effect : mc.player.getStatusEffects()) {
            int secondsLeft = effect.getDuration() / 20;
            int ticksLeft = effect.getDuration() % 20;

            if (secondsLeft <= warnBelow.get() && secondsLeft >= 1 && ticksLeft == 0) {
                ChatUtils.infoPrefix("Potion", "%s runs out in %ds", effect.getEffectType().value().getName().getString(), secondsLeft);
            }
        }
    }
}
