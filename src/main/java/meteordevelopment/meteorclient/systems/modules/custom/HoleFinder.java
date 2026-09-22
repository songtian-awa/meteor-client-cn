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

public class HoleFinder extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
        .name("range")
        .description("Scan range around you.")
        .defaultValue(8.0)
        .min(2.0)
        .max(16.0)
        .sliderMin(2.0)
        .sliderMax(16.0)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the hole highlight.")
        .defaultValue(new SettingColor(0, 255, 0, 80))
        .build()
    );

    private final java.util.List<BlockPos> holes = new java.util.ArrayList<>();

    public HoleFinder() {
        super(Categories.Render, "hole-finder", "Finds 1x1 holes in the ground you can fall into or trap enemies in.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        // Scan for holes around the player and cache them for the renderer
        holes.clear();
        int r = (int) Math.ceil(range.get());
        BlockPos center = mc.player.getBlockPos();

        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                BlockPos ground = center.add(x, 0, z);
                // Find the top solid block in this column (down from player Y - 1)
                BlockPos pos = ground;
                for (int y = center.getY() - 1; y > mc.world.getBottomY(); y--) {
                    BlockPos check = ground.withY(y);
                    if (isSolid(check)) {
                        pos = check.up();
                        break;
                    }
                }

                // Check if this position is a 1x1 hole (air above, solid on all 4 sides)
                if (isHole(pos)) {
                    holes.add(pos);
                }
            }
        }
    }

    private boolean isSolid(BlockPos pos) {
        return !mc.world.getBlockState(pos).isAir();
    }

    private boolean isHole(BlockPos pos) {
        if (!mc.world.getBlockState(pos).isAir()) return false;
        if (!mc.world.getBlockState(pos.up()).isAir()) return false;

        // All four sides must be solid
        return isSolid(pos.north()) && isSolid(pos.south()) && isSolid(pos.east()) && isSolid(pos.west());
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        for (BlockPos pos : holes) {
            event.renderer.box(pos, color.get(), color.get(), ShapeMode.Both, 0);
        }
    }

    public java.util.List<BlockPos> getHoles() {
        return holes;
    }
}
