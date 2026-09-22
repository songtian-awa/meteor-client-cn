/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public class MobESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> hostileColor = sgGeneral.add(new ColorSetting.Builder()
        .name("hostile-color")
        .description("The color for hostile mobs.")
        .defaultValue(new SettingColor(255, 0, 0, 80))
        .build()
    );

    private final Setting<SettingColor> neutralColor = sgGeneral.add(new ColorSetting.Builder()
        .name("neutral-color")
        .description("The color for neutral mobs.")
        .defaultValue(new SettingColor(255, 255, 0, 60))
        .build()
    );

    private final Setting<Boolean> showNeutral = sgGeneral.add(new BoolSetting.Builder()
        .name("show-neutral")
        .description("Also highlight neutral mobs like zombies that are not currently hostile.")
        .defaultValue(true)
        .build()
    );

    public MobESP() {
        super(Categories.Render, "mob-esp", "Highlights all mobs around you, color-coded by hostility.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof MobEntity mob) || !mob.isAlive()) continue;
            if (mob instanceof net.minecraft.entity.passive.PassiveEntity && !showNeutral.get()) continue;

            SettingColor color = mob instanceof HostileEntity ? hostileColor.get() : neutralColor.get();

            double x = MathHelper.lerp(event.tickDelta, mob.lastRenderX, mob.getX()) - mob.getX();
            double y = MathHelper.lerp(event.tickDelta, mob.lastRenderY, mob.getY()) - mob.getY();
            double z = MathHelper.lerp(event.tickDelta, mob.lastRenderZ, mob.getZ()) - mob.getZ();

            Box box = mob.getBoundingBox();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ,
                color, color, ShapeMode.Both, 0);
        }
    }
}
