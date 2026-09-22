/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;

public class VelocityBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> smoothing = sgGeneral.add(new DoubleSetting.Builder()
        .name("smoothing")
        .description("How much of the knockback velocity is applied per tick. Lower values look more natural.")
        .defaultValue(0.5)
        .min(0.05)
        .max(1)
        .sliderMin(0.05)
        .sliderMax(1)
        .build()
    );

    private Vec3d pendingVelocity = Vec3d.ZERO;
    private boolean hasPending;

    public VelocityBypass() {
        super(Categories.Movement, "velocity-bypass", "Applies knockback velocity gradually over several ticks instead of instantly, avoiding abrupt movement that anti-cheats flag.");
    }

    @Override
    public void onActivate() {
        pendingVelocity = Vec3d.ZERO;
        hasPending = false;
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null) return;

        if (event.packet instanceof EntityVelocityUpdateS2CPacket packet) {
            if (packet.getEntityId() == mc.player.getId()) {
                Vec3d vel = packet.getVelocity();

                // Only smooth horizontal knockback, keep vertical for gravity authenticity
                pendingVelocity = new Vec3d(vel.x, 0, vel.z);
                hasPending = true;

                if (vel.y > 0) mc.player.setVelocity(mc.player.getVelocity().add(0, vel.y, 0));
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;

        if (hasPending) {
            double factor = smoothing.get();
            Vec3d current = mc.player.getVelocity();
            mc.player.setVelocity(current.x + pendingVelocity.x * factor, current.y, current.z + pendingVelocity.z * factor);

            pendingVelocity = pendingVelocity.multiply(1 - factor);
            if (pendingVelocity.length() < 0.001) {
                pendingVelocity = Vec3d.ZERO;
                hasPending = false;
            }
        }
    }
}
