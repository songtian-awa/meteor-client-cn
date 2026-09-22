/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class AutoSoup extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> healthThreshold = sgGeneral.add(new DoubleSetting.Builder()
        .name("health-threshold")
        .description("Eat soup when health drops below this value.")
        .defaultValue(10.0)
        .min(2.0)
        .max(20.0)
        .sliderMin(2.0)
        .sliderMax(20.0)
        .build()
    );

    private final Setting<Boolean> fireResistance = sgGeneral.add(new BoolSetting.Builder()
        .name("eat-on-fire")
        .description("Also eat when on fire, regardless of health.")
        .defaultValue(true)
        .build()
    );

    public AutoSoup() {
        super(Categories.Player, "auto-soup", "Automatically consumes mushroom stew when your health is low, a classic practice server strategy.");
    }

    private boolean isSoup(Item item) {
        return item == Items.MUSHROOM_STEW || item == Items.SUSPICIOUS_STEW || item == Items.RABBIT_STEW || item == Items.BEETROOT_SOUP;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        double health = mc.player.getHealth();
        boolean low = health <= healthThreshold.get();
        boolean onFire = fireResistance.get() && mc.player.isOnFire() && !mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE);

        if (!low && !onFire) return;
        if (mc.player.isUsingItem()) return;

        // Look for soup in the hotbar first, then the rest of the inventory
        int soupSlot = -1;
        int bestSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (isSoup(stack.getItem())) {
                soupSlot = i;
                break;
            }
        }

        if (soupSlot == -1) {
            for (int i = 9; i < 36; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (isSoup(stack.getItem())) {
                    bestSlot = i;
                    break;
                }
            }
            if (bestSlot != -1) {
                InvUtils.move().from(bestSlot).toHotbar(0);
                soupSlot = 0;
            }
        }

        if (soupSlot != -1) {
            int finalSlot = soupSlot;
            InvUtils.swap(finalSlot, true);
            mc.interactionManager.interactItem(mc.player, mc.player.getActiveHand());
        }
    }
}
