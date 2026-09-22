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
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class DurabilityWarn extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> threshold = sgGeneral.add(new IntSetting.Builder()
        .name("threshold")
        .description("Warn when durability drops below this percentage.")
        .defaultValue(10)
        .min(1)
        .max(50)
        .sliderMin(1)
        .sliderMax(50)
        .build()
    );

    private final Setting<Boolean> repeat = sgGeneral.add(new BoolSetting.Builder()
        .name("repeat")
        .description("Warn every time durability drops another 1%.")
        .defaultValue(true)
        .build()
    );

    private final Set<Integer> warnedSlots = new HashSet<>();
    private int lastPct = -1;

    public DurabilityWarn() {
        super(Categories.Player, "durability-warn", "Alerts you when your tools and armor are about to break.");
    }

    @Override
    public void onActivate() {
        warnedSlots.clear();
        lastPct = -1;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !stack.isDamageable()) continue;

            int pct = (int) Math.round((stack.getMaxDamage() - stack.getDamage()) * 100.0 / stack.getMaxDamage());
            if (pct <= threshold.get() && warnedSlots.add(i)) {
                ChatUtils.infoPrefix("Durability", "Item in slot %d is at %d%% durability!", i, pct);
            }
            if (pct > threshold.get()) warnedSlots.remove(i);
        }

        // Warn once when total armor drops below threshold
        int worstPct = 100;
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            if (stack.isEmpty() || !stack.isDamageable()) continue;
            int pct = (int) Math.round((stack.getMaxDamage() - stack.getDamage()) * 100.0 / stack.getMaxDamage());
            worstPct = Math.min(worstPct, pct);
        }

        if (worstPct <= threshold.get() && worstPct != lastPct) {
            ChatUtils.infoPrefix("Durability", "Your armor is at %d%% durability!", worstPct);
            lastPct = worstPct;
        }
    }
}
