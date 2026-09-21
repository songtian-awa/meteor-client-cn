/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class NoHurtCam extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> cancelShake = sgGeneral.add(new BoolSetting.Builder()
        .name("cancel-shake")
        .description("Prevents the camera from shaking when you take damage.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> cancelFlash = sgGeneral.add(new BoolSetting.Builder()
        .name("cancel-flash")
        .description("Prevents the red damage flash overlay.")
        .defaultValue(true)
        .build()
    );

    public NoHurtCam() {
        super(Categories.Render, "no-hurt-cam", "Removes the camera shake and red flash when taking damage.");
    }

    public boolean cancelShake() {
        return isActive() && cancelShake.get();
    }

    public boolean cancelFlash() {
        return isActive() && cancelFlash.get();
    }
}
