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
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

public class AutoSort extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> sortHotbar = sgGeneral.add(new BoolSetting.Builder()
        .name("sort-hotbar")
        .description("Keep weapon in slot 0, tools after it.")
        .defaultValue(true)
        .build()
    );

    private int tickCounter;

    public AutoSort() {
        super(Categories.Player, "auto-sort", "Automatically sorts your hotbar: weapon first, then tools, then food.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        tickCounter++;
        if (tickCounter < 40) return;
        tickCounter = 0;

        if (!sortHotbar.get()) return;

        // Weapon (sword/axe) to slot 0
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.DIAMOND_SWORD || stack.getItem() == Items.NETHERITE_SWORD
                || stack.getItem() == Items.IRON_SWORD || stack.getItem() == Items.STONE_SWORD
                || stack.getItem() == Items.WOODEN_SWORD || stack.getItem() == Items.GOLDEN_SWORD
                || stack.getItem() == Items.DIAMOND_AXE || stack.getItem() == Items.NETHERITE_AXE) {
                if (i != 0) {
                    InvUtils.move().from(i).toHotbar(0);
                }
                break;
            }
        }

        // Pickaxe to slot 1
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.DIAMOND_PICKAXE || stack.getItem() == Items.NETHERITE_PICKAXE
                || stack.getItem() == Items.IRON_PICKAXE || stack.getItem() == Items.STONE_PICKAXE) {
                if (i != 1) {
                    InvUtils.move().from(i).toHotbar(1);
                }
                break;
            }
        }
    }
}
