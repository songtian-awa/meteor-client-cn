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
import net.minecraft.util.math.BlockPos;

public class DeathCoords extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> copyToClipboard = sgGeneral.add(new BoolSetting.Builder()
        .name("copy-to-clipboard")
        .description("Copy death coords to clipboard automatically.")
        .defaultValue(true)
        .build()
    );

    private BlockPos deathPos;
    private boolean wasAlive = true;

    public DeathCoords() {
        super(Categories.Player, "death-coords", "Remembers where you died and shows the coordinates when you respawn.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (mc.player.getHealth() <= 0) {
            deathPos = mc.player.getBlockPos();
            wasAlive = false;
            return;
        }

        // Respawned after death
        if (!wasAlive && deathPos != null) {
            String coords = deathPos.getX() + " " + deathPos.getY() + " " + deathPos.getZ();
            ChatUtils.infoPrefix("Death", "You died at %s", coords);

            if (copyToClipboard.get()) {
                mc.keyboard.setClipboard(coords);
            }

            deathPos = null;
            wasAlive = true;
        }
    }
}
