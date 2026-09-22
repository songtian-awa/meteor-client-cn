/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;

public class TotemTimer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> warnAt = sgGeneral.add(new IntSetting.Builder()
        .name("warn-at")
        .description("Warn when this many totems are left in your inventory.")
        .defaultValue(2)
        .min(0)
        .max(36)
        .sliderMin(0)
        .sliderMax(36)
        .build()
    );

    private final Setting<Boolean> chatMessage = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-message")
        .description("Send a chat message when a totem pops.")
        .defaultValue(true)
        .build()
    );

    private int lastTotemCount = -1;

    public TotemTimer() {
        super(Categories.Misc, "totem-timer", "Tracks your totem count and warns you when you are running low or one pops.");
    }

    private int countTotems() {
        if (mc.player == null) return 0;
        PlayerInventory inv = mc.player.getInventory();

        int count = 0;
        for (int i = 0; i < inv.size(); i++) {
            if (inv.getStack(i).getItem() == Items.TOTEM_OF_UNDYING) count++;
        }
        return count;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        int count = countTotems();
        if (lastTotemCount == -1) {
            lastTotemCount = count;
            return;
        }

        // Totem popped
        if (count < lastTotemCount && chatMessage.get()) {
            int popped = lastTotemCount - count;
            ChatUtils.infoPrefix("Totems", "Popped %d totem(s)! %d left.", popped, count);
        }

        // Low warning
        if (warnAt.get() > 0 && count <= warnAt.get() && count != lastTotemCount && count > 0) {
            ChatUtils.infoPrefix("Totems", "Only %d totem(s) left!", count);
        }

        lastTotemCount = count;
    }
}
