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

public class SwordStop extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> swingCooldown = sgGeneral.add(new IntSetting.Builder()
        .name("swing-cooldown")
        .description("The minimum ticks between sword swings.")
        .defaultValue(3)
        .min(0)
        .max(20)
        .sliderMin(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> requireSword = sgGeneral.add(new BoolSetting.Builder()
        .name("require-sword")
        .description("Only applies the swing cooldown while holding a sword.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onlyOnHit = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-hit")
        .description("Only starts the cooldown after a successful hit.")
        .defaultValue(false)
        .build()
    );

    private int cooldown;

    public SwordStop() {
        super(Categories.Combat, "sword-stop", "Prevents over-swings by enforcing a delay between sword attacks, keeping your hits clean.");
    }

    @Override
    public void onActivate() {
        cooldown = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null || mc.world == null) return;

        if (cooldown > 0) {
            cooldown--;
            mc.options.attackKey.setPressed(false);
            return;
        }

        if (onlyOnHit.get()) return;

        if (requireSword.get()) {
            var stack = mc.player.getMainHandStack();
            boolean isSword = stack.getItem() == net.minecraft.item.Items.NETHERITE_SWORD
                || stack.getItem() == net.minecraft.item.Items.DIAMOND_SWORD
                || stack.getItem() == net.minecraft.item.Items.IRON_SWORD
                || stack.getItem() == net.minecraft.item.Items.GOLDEN_SWORD
                || stack.getItem() == net.minecraft.item.Items.STONE_SWORD
                || stack.getItem() == net.minecraft.item.Items.WOODEN_SWORD;
            if (!isSword) return;
        }

        if (mc.options.attackKey.isPressed() && cooldown == 0) {
            // Let the vanilla click through; the cooldown below prevents rapid re-swings
            cooldown = swingCooldown.get();
        }
    }
}
