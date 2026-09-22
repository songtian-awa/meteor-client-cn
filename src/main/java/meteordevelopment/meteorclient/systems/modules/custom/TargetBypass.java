/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TargetBypass extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> maxRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-range")
        .description("Maximum distance at which a target is considered valid.")
        .defaultValue(6.0)
        .min(2.0)
        .max(10.0)
        .sliderMin(2.0)
        .sliderMax(10.0)
        .build()
    );

    private final Setting<Boolean> playersFirst = sgGeneral.add(new BoolSetting.Builder()
        .name("players-first")
        .description("Prefer targeting players over other living entities.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Skip entities that are marked as friends by Meteor's friend system.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignoreInvisible = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-invisible")
        .description("Skip invisible entities.")
        .defaultValue(false)
        .build()
    );

    public TargetBypass() {
        super(Categories.Combat, "target-bypass", "Filters and prioritizes combat targets: players over mobs, friends ignored, closest first.");
    }

    public Entity getBestTarget() {
        if (mc.world == null || mc.player == null) return null;

        List<Entity> candidates = new ArrayList<>();
        double rangeSq = maxRange.get() * maxRange.get();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
            if (entity == mc.player) continue;

            if (ignoreInvisible.get() && entity.isInvisible()) continue;
            if (ignoreFriends.get() && entity instanceof PlayerEntity && meteordevelopment.meteorclient.systems.friends.Friends.get().isFriend((PlayerEntity) entity)) continue;

            if (mc.player.squaredDistanceTo(entity) > rangeSq) continue;

            candidates.add(entity);
        }

        if (candidates.isEmpty()) return null;

        // Sort: players first (if enabled), then by distance
        candidates.sort(Comparator
            .comparingInt((Entity e) -> (playersFirst.get() && e instanceof PlayerEntity) ? 0 : 1)
            .thenComparingDouble(e -> mc.player.squaredDistanceTo(e)));

        return candidates.get(0);
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (mc.player == null || mc.world == null) return;

        if (!(event.packet instanceof PlayerInteractEntityC2SPacket packet)) return;
        if (!(packet instanceof IPlayerInteractEntityC2SPacket iPacket)) return;
        if (iPacket.meteor$getType() != PlayerInteractEntityC2SPacket.InteractType.ATTACK) return;

        Entity entity = iPacket.meteor$getEntity();
        if (entity == null) return;

        // Block attacks on ignored targets
        if (ignoreFriends.get() && entity instanceof PlayerEntity && meteordevelopment.meteorclient.systems.friends.Friends.get().isFriend((PlayerEntity) entity)) {
            event.cancel();
        }
    }
}
