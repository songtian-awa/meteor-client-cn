/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class NetherCoords extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> interval = sgGeneral.add(new DoubleSetting.Builder()
        .name("interval")
        .description("How often to print the converted coordinates, in seconds.")
        .defaultValue(3.0)
        .min(1.0)
        .max(60.0)
        .sliderMin(1.0)
        .sliderMax(60.0)
        .build()
    );

    private final Setting<Boolean> onlyOnChange = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-change")
        .description("Only print when you move to a different block position.")
        .defaultValue(true)
        .build()
    );

    private int ticks;
    private Vec3d lastPrinted;

    public NetherCoords() {
        super(Categories.Player, "nether-coords", "Shows your coordinates converted between the overworld and the nether.");
    }

    @Override
    public void onActivate() {
        ticks = 0;
        lastPrinted = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        ticks++;
        int intervalTicks = (int) Math.round(interval.get() * 20);
        if (ticks < intervalTicks) return;
        ticks = 0;

        Vec3d pos = mc.player.getEntityPos();

        if (onlyOnChange.get() && lastPrinted != null) {
            if (Math.floor(pos.x) == Math.floor(lastPrinted.x) &&
                Math.floor(pos.y) == Math.floor(lastPrinted.y) &&
                Math.floor(pos.z) == Math.floor(lastPrinted.z)) {
                return;
            }
        }
        lastPrinted = pos;

        boolean inNether = mc.world != null && mc.world.getRegistryKey() == net.minecraft.world.World.NETHER;

        int x = (int) Math.floor(pos.x);
        int y = (int) Math.floor(pos.y);
        int z = (int) Math.floor(pos.z);

        if (inNether) {
            ChatUtils.infoPrefix("NetherCoords", "Nether: %d %d %d  ->  Overworld: %d %d %d", x, y, z, x * 8, y, z * 8);
        }
        else {
            ChatUtils.infoPrefix("NetherCoords", "Overworld: %d %d %d  ->  Nether: %d %d %d", x, y, z, Math.floorDiv(x, 8), y, Math.floorDiv(z, 8));
        }
    }
}
