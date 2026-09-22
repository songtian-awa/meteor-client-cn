/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;

public class FpsBoost extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> renderDistance = sgGeneral.add(new IntSetting.Builder()
        .name("render-distance")
        .description("Render distance to force while this module is active.")
        .defaultValue(6)
        .min(2)
        .max(16)
        .sliderMin(2)
        .sliderMax(16)
        .build()
    );

    private int originalDistance;

    public FpsBoost() {
        super(Categories.Render, "fps-boost", "Boosts your FPS by reducing the render distance while active, restoring it when turned off.");
    }

    @Override
    public void onActivate() {
        if (mc.options != null) {
            originalDistance = mc.options.getViewDistance().getValue();
            mc.options.getViewDistance().setValue(renderDistance.get());
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.options != null && originalDistance > 0) {
            mc.options.getViewDistance().setValue(originalDistance);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.options == null) return;

        if (mc.options.getViewDistance().getValue() != renderDistance.get()) {
            mc.options.getViewDistance().setValue(renderDistance.get());
        }
    }
}
