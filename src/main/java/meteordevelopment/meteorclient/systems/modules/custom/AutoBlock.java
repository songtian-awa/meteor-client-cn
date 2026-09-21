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
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoBlock extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("The range at which to start blocking.")
        .defaultValue(5)
        .min(1)
        .max(10)
        .sliderMin(1)
        .sliderMax(10)
        .build()
    );

    private final Setting<Boolean> requireSword = sgGeneral.add(new BoolSetting.Builder()
        .name("require-sword")
        .description("Only blocks when holding a sword.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> clickBlockToActivate = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-clicking")
        .description("Only blocks while you are holding right click.")
        .defaultValue(false)
        .build()
    );

    public AutoBlock() {
        super(Categories.Combat, "auto-block", "Automatically holds up your shield (or sword block) when an enemy is near.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null || mc.world == null) return;

        boolean holdingShield = mc.player.getOffHandStack().getItem() == Items.SHIELD;
        boolean holdingSword = mc.player.getMainHandStack().getItem() == Items.NETHERITE_SWORD
            || mc.player.getMainHandStack().getItem() == Items.DIAMOND_SWORD
            || mc.player.getMainHandStack().getItem() == Items.IRON_SWORD
            || mc.player.getMainHandStack().getItem() == Items.GOLDEN_SWORD
            || mc.player.getMainHandStack().getItem() == Items.STONE_SWORD
            || mc.player.getMainHandStack().getItem() == Items.WOODEN_SWORD;

        if (requireSword.get() && !holdingSword && !holdingShield) return;
        if (clickBlockToActivate.get() && !mc.options.useKey.isPressed()) return;

        boolean enemyNear = false;
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity p && p != mc.player && p.isAlive() && !p.isRemoved()) {
                if (mc.player.distanceTo(p) <= range.get()) {
                    enemyNear = true;
                    break;
                }
            }
        }

        if (enemyNear && !mc.player.isUsingItem()) {
            mc.options.useKey.setPressed(true);
            mc.interactionManager.interactItem(mc.player, holdingShield ? Hand.OFF_HAND : Hand.MAIN_HAND);
        }
        else if (!enemyNear && mc.player.isUsingItem() && !mc.options.useKey.isPressed()) {
            mc.player.stopUsingItem();
        }
    }

    @Override
    public void onDeactivate() {
        mc.options.useKey.setPressed(false);
    }
}
