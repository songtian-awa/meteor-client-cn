/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AirWalk extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the platform indicator.")
        .defaultValue(new SettingColor(255, 255, 255, 60))
        .build()
    );

    private BlockPos lastPlatform;

    public AirWalk() {
        super(Categories.Movement, "air-walk", "Shows a phantom platform under your feet while airborne, helping you bridge and walk over gaps confidently.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (!mc.player.isOnGround()) {
            lastPlatform = mc.player.getBlockPos().down();
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (lastPlatform != null && mc.world != null && mc.world.getBlockState(lastPlatform).isAir()) {
            event.renderer.box(lastPlatform, color.get(), color.get(), ShapeMode.Both, 0);
        }
    }
}
