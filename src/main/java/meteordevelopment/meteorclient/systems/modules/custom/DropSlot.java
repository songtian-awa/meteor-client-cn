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
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;

public class DropSlot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> slot = sgGeneral.add(new IntSetting.Builder()
        .name("slot")
        .description("The hotbar slot to drop (0-8).")
        .defaultValue(0)
        .min(0)
        .max(8)
        .sliderMin(0)
        .sliderMax(8)
        .build()
    );

    private final Setting<Boolean> onlyWhenNotEmpty = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-not-empty")
        .description("Only drops when the slot has an item.")
        .defaultValue(true)
        .build()
    );

    public DropSlot() {
        super(Categories.Player, "drop-slot", "Drops the item in a chosen hotbar slot. Toggle on and off to drop.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        ItemStack stack = mc.player.getInventory().getStack(slot.get());
        if (onlyWhenNotEmpty.get() && stack.isEmpty()) return;

        InvUtils.drop().slotHotbar(slot.get());
        this.toggle();
    }
}
