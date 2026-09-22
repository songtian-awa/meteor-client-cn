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
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;

public class TimerBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("The target tick rate multiplier.")
        .defaultValue(1.1)
        .min(0.1)
        .max(5)
        .sliderMin(0.1)
        .sliderMax(3)
        .build()
    );

    private final Setting<Double> rampSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("ramp-speed")
        .description("How fast the tick rate ramps up to the target speed. Lower values look more natural to anti-cheats.")
        .defaultValue(0.05)
        .min(0.01)
        .max(0.5)
        .sliderMin(0.01)
        .sliderMax(0.5)
        .build()
    );

    private double currentRate = 1.0;
    private long lastTickTime = 0;

    public TimerBypass() {
        super(Categories.Movement, "timer-bypass", "Gradually ramps the game tick rate to the target speed, making timer changes less obvious to anti-cheat.");
    }

    @Override
    public void onActivate() {
        currentRate = 1.0;
        lastTickTime = System.currentTimeMillis();
        Modules.get().get(Timer.class).setOverride(currentRate);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;

        long now = System.currentTimeMillis();
        if (lastTickTime == 0) {
            lastTickTime = now;
            return;
        }

        double target = speed.get();
        double step = rampSpeed.get() * Math.max(1, (now - lastTickTime) / 50.0);

        if (currentRate < target) currentRate = Math.min(target, currentRate + step);
        else if (currentRate > target) currentRate = Math.max(target, currentRate - step);

        Modules.get().get(Timer.class).setOverride(currentRate);
        lastTickTime = now;
    }

    @Override
    public void onDeactivate() {
        Modules.get().get(Timer.class).setOverride(Timer.OFF);
        currentRate = 1.0;
    }
}
