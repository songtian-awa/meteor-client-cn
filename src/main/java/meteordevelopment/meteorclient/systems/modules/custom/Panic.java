/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;

public class Panic extends Module {
    private boolean panicking;

    public Panic() {
        super(Categories.Misc, "panic", "Instantly disables every active module to hide your client in an emergency.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;

        if (!panicking) {
            // Disable all other modules once
            int count = 0;
            for (Module module : Modules.get().getAll()) {
                if (module != this && module.isActive()) {
                    module.toggle();
                    count++;
                }
            }

            if (count > 0) {
                ChatUtils.infoPrefix("Panic", "Disabled %d module(s).", count);
            }

            panicking = true;
            toggle();
        }
    }

    @Override
    public void onDeactivate() {
        panicking = false;
    }
}
