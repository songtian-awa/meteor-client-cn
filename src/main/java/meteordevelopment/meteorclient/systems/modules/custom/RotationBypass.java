/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.MathHelper;

public class RotationBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> maxStep = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-step")
        .description("The maximum rotation change per tick sent to the server.")
        .defaultValue(20)
        .min(1)
        .max(180)
        .sliderMin(1)
        .sliderMax(180)
        .build()
    );

    private final Setting<Boolean> smoothLook = sgGeneral.add(new BoolSetting.Builder()
        .name("smooth-look")
        .description("Smoothly interpolates look direction on the client side as well.")
        .defaultValue(true)
        .build()
    );

    private float prevYaw, prevPitch;
    private float clientYaw, clientPitch;
    private boolean initialized;

    public RotationBypass() {
        super(Categories.Movement, "rotation-bypass", "Smooths out rotations sent to the server, reducing the chance of anti-cheat flagging rapid aim changes.");
    }

    @Override
    public void onActivate() {
        initialized = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;

        if (!initialized) {
            prevYaw = mc.player.getYaw();
            prevPitch = mc.player.getPitch();
            clientYaw = prevYaw;
            clientPitch = prevPitch;
            initialized = true;
            return;
        }

        float targetYaw = mc.player.getYaw();
        float targetPitch = mc.player.getPitch();

        float dYaw = MathHelper.wrapDegrees(targetYaw - prevYaw);
        float dPitch = MathHelper.wrapDegrees(targetPitch - prevPitch);

        float step = maxStep.get().floatValue();
        float newYaw = prevYaw + MathHelper.clamp(dYaw, -step, step);
        float newPitch = prevPitch + MathHelper.clamp(dPitch, -step, step);

        if (smoothLook.get()) {
            clientYaw += MathHelper.wrapDegrees(newYaw - clientYaw) * 0.35f;
            clientPitch += (newPitch - clientPitch) * 0.35f;
            mc.player.setYaw(clientYaw);
            mc.player.setPitch(clientPitch);
        }
        else {
            mc.player.setYaw(newYaw);
            mc.player.setPitch(newPitch);
        }

        prevYaw = newYaw;
        prevPitch = newPitch;

        Rotations.serverYaw = newYaw;
        Rotations.serverPitch = newPitch;
    }

    @EventHandler
    private void onSendMovementPacketsPre(SendMovementPacketsEvent.Pre event) {
        mc.player.setYaw(prevYaw);
        mc.player.setPitch(prevPitch);
    }

    @EventHandler
    private void onSendMovementPacketsPost(SendMovementPacketsEvent.Post event) {
        mc.player.setYaw(clientYaw);
        mc.player.setPitch(clientPitch);
    }
}
