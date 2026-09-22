/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class CoordsCopy extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> netherCoords = sgGeneral.add(new BoolSetting.Builder()
        .name("nether-coords")
        .description("Also show the nether equivalent coordinates.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> copyToClipboard = sgGeneral.add(new BoolSetting.Builder()
        .name("copy-to-clipboard")
        .description("Copy the coordinates to your clipboard as well.")
        .defaultValue(true)
        .build()
    );

    private int tickCounter;

    public CoordsCopy() {
        super(Categories.Misc, "coords-copy", "Copies your current coordinates to chat and clipboard for easy sharing.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;

        Vec3d pos = mc.player.getEntityPos();
        int x = (int) Math.floor(pos.x);
        int y = (int) Math.floor(pos.y);
        int z = (int) Math.floor(pos.z);

        String coords = x + " " + y + " " + z;

        if (copyToClipboard.get()) {
            mc.keyboard.setClipboard(coords);
        }

        ChatUtils.infoPrefix("Coords", "Position: %s", coords);

        if (netherCoords.get()) {
            ChatUtils.infoPrefix("Coords", "Nether: %d %d %d", Math.floorDiv(x, 8), y, Math.floorDiv(z, 8));
        }
    }
}
