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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import java.util.Optional;

public class AutoPotion extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> healthThreshold = sgGeneral.add(new DoubleSetting.Builder()
        .name("health-threshold")
        .description("Drink a healing potion when health drops below this.")
        .defaultValue(8.0)
        .min(2.0)
        .max(20.0)
        .sliderMin(2.0)
        .sliderMax(20.0)
        .build()
    );

    private final Setting<Boolean> fireResistance = sgGeneral.add(new BoolSetting.Builder()
        .name("fire-resistance")
        .description("Automatically drink fire resistance when on fire.")
        .defaultValue(true)
        .build()
    );

    private int cooldown;

    public AutoPotion() {
        super(Categories.Player, "auto-potion", "Automatically drinks healing and fire resistance potions in combat.");
    }

    @Override
    public void onActivate() {
        cooldown = 0;
    }

    private boolean isHealing(ItemStack stack) {
        if (stack.getItem() != Items.POTION) return false;
        PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (contents == null) return false;
        for (var effect : contents.getEffects()) {
            if (effect.getEffectType().value() == StatusEffects.INSTANT_HEALTH) return true;
        }
        return false;
    }

    private boolean isFireResistance(ItemStack stack) {
        if (stack.getItem() != Items.POTION) return false;
        PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (contents == null) return false;
        for (var effect : contents.getEffects()) {
            if (effect.getEffectType().value() == StatusEffects.FIRE_RESISTANCE) return true;
        }
        return false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (mc.player.isUsingItem()) return;

        boolean needHeal = mc.player.getHealth() <= healthThreshold.get();
        boolean needFire = fireResistance.get() && mc.player.isOnFire() && !mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE);

        if (!needHeal && !needFire) return;

        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (needHeal && isHealing(stack)) { slot = i; break; }
            if (needFire && isFireResistance(stack)) { slot = i; break; }
        }

        if (slot != -1) {
            InvUtils.swap(slot, true);
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            cooldown = 20;
        }
    }
}
