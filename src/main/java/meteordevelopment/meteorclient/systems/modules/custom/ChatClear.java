/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;

public class ChatClear extends Module {
    private boolean cleared;

    public ChatClear() {
        super(Categories.Misc, "chat-clear", "Clears your chat instantly when toggled.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.inGameHud == null) return;

        if (!cleared) {
            mc.inGameHud.getChatHud().clear(false);
            cleared = true;
            toggle();
        }
    }

    @Override
    public void onActivate() {
        cleared = false;
    }
}
