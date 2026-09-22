/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;

public class CrystalCounter extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Count crystals within this range.")
        .defaultValue(16.0)
        .min(4.0)
        .max(64.0)
        .sliderMin(4.0)
        .sliderMax(64.0)
        .build()
    );

    private int lastCount = -1;

    public CrystalCounter() {
        super(Categories.Combat, "crystal-counter", "Counts nearby end crystals and warns you when your opponent places a new one.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null || mc.world == null) return;

        double r = range.get();
        int count = 0;
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity && mc.player.squaredDistanceTo(entity) < r * r) {
                count++;
            }
        }

        if (lastCount != -1 && count > lastCount) {
            ChatUtils.infoPrefix("Crystals", "Opponent placed a crystal! Total: %d", count);
        }

        lastCount = count;
    }
}
