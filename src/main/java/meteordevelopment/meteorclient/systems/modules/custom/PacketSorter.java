/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class PacketSorter extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> minInterval = sgGeneral.add(new IntSetting.Builder()
        .name("min-interval")
        .description("The minimum ticks between movement packets. Prevents sending too many at once.")
        .defaultValue(0)
        .min(0)
        .max(20)
        .sliderMin(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> dedupeLook = sgGeneral.add(new BoolSetting.Builder()
        .name("dedupe-look")
        .description("Skips sending look-only packets when your rotation hasn't changed.")
        .defaultValue(true)
        .build()
    );

    private int tickCounter;
    private float lastYaw, lastPitch;

    public PacketSorter() {
        super(Categories.Misc, "packet-sorter", "Rate-limits and deduplicates movement packets so you send fewer packets than vanilla.");
    }

    @Override
    public void onActivate() {
        tickCounter = 0;
        lastYaw = Float.NaN;
        lastPitch = Float.NaN;
    }

    @EventHandler
    private void onTick(meteordevelopment.meteorclient.events.world.TickEvent.Pre event) {
        tickCounter++;
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (mc.player == null) return;

        if (event.packet instanceof PlayerMoveC2SPacket.LookAndOnGround look) {
            if (dedupeLook.get() && lastYaw == look.getYaw(0) && lastPitch == look.getPitch(0)) {
                event.cancel();
                return;
            }
            lastYaw = look.getYaw(0);
            lastPitch = look.getPitch(0);
        }

        if (minInterval.get() > 0 && tickCounter % (minInterval.get() + 1) != 0) {
            if (event.packet instanceof PlayerMoveC2SPacket) {
                event.cancel();
            }
        }
    }
}
