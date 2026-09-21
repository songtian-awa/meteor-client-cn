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
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.HashSet;
import java.util.Set;

public class InventoryCleaner extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> trashSlot = sgGeneral.add(new IntSetting.Builder()
        .name("trash-slot")
        .description("The hotbar slot to drop trash items into (0-8).")
        .defaultValue(8)
        .min(0)
        .max(8)
        .sliderMin(0)
        .sliderMax(8)
        .build()
    );

    private final Setting<Boolean> enable = sgGeneral.add(new BoolSetting.Builder()
        .name("enable")
        .description("Enables the automatic cleaner.")
        .defaultValue(true)
        .build()
    );

    private final Set<Item> trashItems = new HashSet<>();

    public InventoryCleaner() {
        super(Categories.Player, "inventory-cleaner", "Automatically throws away unwanted junk items from your inventory.");

        // Common junk items
        trashItems.add(Items.ROTTEN_FLESH);
        trashItems.add(Items.SPIDER_EYE);
        trashItems.add(Items.POISONOUS_POTATO);
        trashItems.add(Items.BONE);
        trashItems.add(Items.FLINT);
        trashItems.add(Items.GRAVEL);
        trashItems.add(Items.DIRT);
        trashItems.add(Items.COBBLESTONE);
        trashItems.add(Items.POPPED_CHORUS_FRUIT);
        trashItems.add(Items.DEAD_BUSH);
        trashItems.add(Items.TALL_GRASS);
        trashItems.add(Items.FERN);
        trashItems.add(Items.SHORT_GRASS);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;
        if (!enable.get()) return;

        for (int i = 0; i < 36; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            if (trashItems.contains(stack.getItem())) {
                // Move to trash slot and drop it
                if (i < 9) {
                    InvUtils.drop().slotHotbar(i);
                }
                else {
                    InvUtils.move().from(i).toHotbar(trashSlot.get());
                }
            }
        }
    }
}
