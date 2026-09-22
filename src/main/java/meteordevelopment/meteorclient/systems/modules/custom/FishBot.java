/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.FishingBobberEntityAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class FishBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> catchDelay = sgGeneral.add(new DoubleSetting.Builder()
        .name("catch-delay")
        .description("How many ticks to wait after a catch before re-casting.")
        .defaultValue(10.0)
        .min(1.0)
        .max(60.0)
        .sliderMin(1.0)
        .sliderMax(60.0)
        .build()
    );

    private final Setting<Boolean> autoReel = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-reel")
        .description("Automatically reel in when a fish bites.")
        .defaultValue(true)
        .build()
    );

    private int delayTimer;

    public FishBot() {
        super(Categories.Player, "fish-bot", "Automatically casts and reels the fishing rod, catching fish hands-free.");
    }

    @Override
    public void onActivate() {
        delayTimer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        // Check if the player is holding a fishing rod
        boolean hasRod = mc.player.getMainHandStack().getItem() == Items.FISHING_ROD || mc.player.getOffHandStack().getItem() == Items.FISHING_ROD;
        if (!hasRod) return;

        if (delayTimer > 0) {
            delayTimer--;
            return;
        }

        FishingBobberEntity bobber = mc.player.fishHook;

        if (bobber == null) {
            // Cast the rod
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
        else if (autoReel.get() && ((FishingBobberEntityAccessor) bobber).meteor$hasCaughtFish()) {
            // Fish bit - reel in, then wait before re-casting
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            delayTimer = (int) Math.round(catchDelay.get());
        }
    }
}
