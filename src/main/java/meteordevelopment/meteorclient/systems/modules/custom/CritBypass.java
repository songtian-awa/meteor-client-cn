/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.hit.HitResult;

public class CritBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> jumpHeight = sgGeneral.add(new DoubleSetting.Builder()
        .name("jump-height")
        .description("Extra height added to the jump used for criticals.")
        .defaultValue(0.0)
        .min(-0.1)
        .max(0.2)
        .sliderMin(-0.1)
        .sliderMax(0.2)
        .build()
    );

    private final Setting<Boolean> onlyOnGround = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-ground")
        .description("Only perform crit jumps when standing on the ground.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> minDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("min-distance")
        .description("Minimum distance to a target before attempting a crit jump.")
        .defaultValue(3.0)
        .min(1.0)
        .max(6.0)
        .sliderMin(1.0)
        .sliderMax(6.0)
        .build()
    );

    public CritBypass() {
        super(Categories.Combat, "crit-bypass", "Automatically performs critical hits by timing jumps before each attack, with a configurable jump height.");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (mc.player == null || mc.world == null) return;

        if (!(event.packet instanceof PlayerInteractEntityC2SPacket packet)) return;
        if (!(packet instanceof IPlayerInteractEntityC2SPacket iPacket)) return;

        // Only apply to attack packets
        if (iPacket.meteor$getType() != PlayerInteractEntityC2SPacket.InteractType.ATTACK) return;

        // Find the targeted entity
        Entity entity = iPacket.meteor$getEntity();
        if (entity == null || !entity.isAlive()) return;

        if (mc.player.squaredDistanceTo(entity) > minDistance.get() * minDistance.get()) return;

        if (onlyOnGround.get() && !mc.player.isOnGround()) return;

        // Player must not already be airborne with upward velocity for a crit
        if (mc.player.getVelocity().y < 0 || mc.player.fallDistance > 0) {
            // Jump now so the attack lands as a critical
            mc.player.jump();
        }

        double vy = mc.player.getVelocity().y;
        double extra = jumpHeight.get();
        if (extra != 0 && vy > 0) {
            mc.player.setVelocity(mc.player.getVelocity().x, vy + extra, mc.player.getVelocity().z);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        // If attack target exists and we are falling, jump early for next attack
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY && mc.player.getVelocity().y < -0.05) {
            if (onlyOnGround.get() && mc.player.isOnGround()) {
                mc.player.jump();
            }
        }
    }
}
