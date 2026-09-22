/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;

import java.util.List;

public class ItemFilter extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Items to automatically drop from your inventory.")
        .build()
    );

    private final Setting<Boolean> dropHotbar = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
        .name("drop-hotbar")
        .description("Also check the hotbar.")
        .defaultValue(true)
        .build()
    );

    public ItemFilter() {
        super(Categories.Player, "item-filter", "Automatically drops configured junk items from your inventory.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;
        if (items.get().isEmpty()) return;

        int start = dropHotbar.get() ? 0 : 9;
        for (int i = start; i < mc.player.getInventory().size(); i++) {
            if (items.get().contains(mc.player.getInventory().getStack(i).getItem())) {
                InvUtils.drop().slot(i);
            }
        }
    }
}
