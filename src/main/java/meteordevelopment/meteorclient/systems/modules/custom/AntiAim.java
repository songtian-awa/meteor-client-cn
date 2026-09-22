/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.MathHelper;

public class AntiAim extends Module {
    public enum Mode {
        Spin("Spin"),
        Jitter("Jitter"),
        Static("Static");

        private final String title;

        Mode(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("The anti-aim mode.")
        .defaultValue(Mode.Spin)
        .build()
    );

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Degrees per tick to rotate.")
        .defaultValue(15.0)
        .min(1.0)
        .max(180.0)
        .sliderMin(1.0)
        .sliderMax(180.0)
        .build()
    );

    private final Setting<Double> staticYaw = sgGeneral.add(new DoubleSetting.Builder()
        .name("static-yaw")
        .description("The yaw to hold in Static mode.")
        .defaultValue(90.0)
        .min(-180.0)
        .max(180.0)
        .sliderMin(-180.0)
        .sliderMax(180.0)
        .build()
    );

    private final Setting<Boolean> pitchFlip = sgGeneral.add(new BoolSetting.Builder()
        .name("pitch-flip")
        .description("Randomly flip the pitch up and down to confuse aimbots.")
        .defaultValue(false)
        .build()
    );

    private float yaw;
    private boolean pitchUp = true;

    public AntiAim() {
        super(Categories.Combat, "anti-aim", "Rotates your view in unnatural ways to confuse enemy aim assist and aimbots.");
    }

    @Override
    public void onActivate() {
        yaw = mc.player != null ? mc.player.getYaw() : 0;
        pitchUp = true;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        switch (mode.get()) {
            case Spin -> {
                yaw += speed.get().floatValue();
                mc.player.setYaw(MathHelper.wrapDegrees(yaw));
            }
            case Jitter -> {
                float s = speed.get().floatValue();
                yaw = mc.player.getYaw() + (Math.random() < 0.5 ? -s : s);
                mc.player.setYaw(MathHelper.wrapDegrees(yaw));
            }
            case Static -> {
                yaw = staticYaw.get().floatValue();
                mc.player.setYaw(yaw);
            }
        }

        if (pitchFlip.get()) {
            float p = mc.player.getPitch();
            if (pitchUp && p > 80) pitchUp = false;
            if (!pitchUp && p < -80) pitchUp = true;
            mc.player.setPitch(MathHelper.clamp(p + (pitchUp ? 2 : -2), -90, 90));
        }
    }
}
