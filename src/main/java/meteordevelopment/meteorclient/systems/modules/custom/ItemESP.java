/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.util.HashSet;
import java.util.Set;

public class ItemESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the item highlight.")
        .defaultValue(new SettingColor(255, 215, 0, 100))
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum distance to render dropped items.")
        .defaultValue(64)
        .min(5)
        .max(256)
        .sliderMin(5)
        .sliderMax(256)
        .build()
    );

    private final Setting<Boolean> onlyNamed = sgGeneral.add(new BoolSetting.Builder()
        .name("only-named")
        .description("Only renders items with a custom name.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> highlightSpecials = sgGeneral.add(new BoolSetting.Builder()
        .name("highlight-specials")
        .description("Uses a separate color for special items (diamonds, netherite, etc).")
        .defaultValue(true)
        .build()
    );

    private final Set<Item> specialItems = new HashSet<>();

    public ItemESP() {
        super(Categories.Render, "item-esp", "Highlights dropped items on the ground.");

        specialItems.add(Items.DIAMOND);
        specialItems.add(Items.DIAMOND_SWORD);
        specialItems.add(Items.DIAMOND_CHESTPLATE);
        specialItems.add(Items.DIAMOND_HELMET);
        specialItems.add(Items.DIAMOND_LEGGINGS);
        specialItems.add(Items.DIAMOND_BOOTS);
        specialItems.add(Items.NETHERITE_INGOT);
        specialItems.add(Items.NETHERITE_SWORD);
        specialItems.add(Items.NETHERITE_CHESTPLATE);
        specialItems.add(Items.NETHERITE_HELMET);
        specialItems.add(Items.NETHERITE_LEGGINGS);
        specialItems.add(Items.NETHERITE_BOOTS);
        specialItems.add(Items.ELYTRA);
        specialItems.add(Items.TOTEM_OF_UNDYING);
        specialItems.add(Items.SHULKER_BOX);
        specialItems.add(Items.ENCHANTED_GOLDEN_APPLE);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        for (net.minecraft.entity.Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity item) || !item.isAlive()) continue;
            if (mc.player.distanceTo(item) > range.get()) continue;

            boolean special = highlightSpecials.get() && specialItems.contains(item.getStack().getItem());
            SettingColor c = special ? new SettingColor(255, 80, 255, 120) : color.get();

            if (onlyNamed.get() && item.getStack().getName().getString().equals(item.getStack().getItem().getName().getString())) continue;

            double x = MathHelper.lerp(event.tickDelta, item.lastRenderX, item.getX()) - item.getX();
            double y = MathHelper.lerp(event.tickDelta, item.lastRenderY, item.getY()) - item.getY();
            double z = MathHelper.lerp(event.tickDelta, item.lastRenderZ, item.getZ()) - item.getZ();

            Box box = item.getBoundingBox();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, c, c, ShapeMode.Both, 0);
        }
    }
}
