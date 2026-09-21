/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public class AntiLevitation extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> onlyWhenRising = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
        .name("only-when-rising")
        .description("Only counteracts levitation while you are moving upwards.")
        .defaultValue(true)
        .build()
    );

    public AntiLevitation() {
        super(Categories.Movement, "anti-levitation", "Cancels the levitation effect, keeping you on the ground.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;
        if (!mc.player.hasStatusEffect(StatusEffects.LEVITATION)) return;

        Vec3d vel = mc.player.getVelocity();
        if (vel.y > 0 || !onlyWhenRising.get()) {
            mc.player.setVelocity(vel.x, Math.min(vel.y, 0), vel.z);
        }
    }
}
