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
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoRod extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The range to reel in enemies.")
        .defaultValue(6)
        .min(1)
        .max(10)
        .sliderMin(1)
        .sliderMax(10)
        .build()
    );

    private final Setting<Boolean> onlyWhenHoldingRod = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-holding-rod")
        .description("Only reels when you are holding a fishing rod.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> autoReel = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-reel")
        .description("Automatically reels in after the hook lands.")
        .defaultValue(true)
        .build()
    );

    private int reelTimer;

    public AutoRod() {
        super(Categories.Combat, "auto-rod", "Automatically uses a fishing rod to reel in nearby players.");
    }

    @Override
    public void onActivate() {
        reelTimer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null || mc.world == null) return;

        ItemStack main = mc.player.getMainHandStack();
        if (onlyWhenHoldingRod.get() && !(main.getItem() instanceof FishingRodItem)) return;

        boolean enemyNear = false;
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity p && p != mc.player && p.isAlive() && mc.player.distanceTo(p) <= range.get()) {
                enemyNear = true;
                break;
            }
        }

        if (!enemyNear) {
            reelTimer = 0;
            return;
        }

        if (reelTimer > 0) {
            reelTimer--;
            if (reelTimer == 0 && autoReel.get()) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
            return;
        }

        // Cast the rod
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        reelTimer = 10;
    }
}
