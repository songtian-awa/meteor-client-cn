/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class PearlAim extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum range to predict pearl landings.")
        .defaultValue(50)
        .min(5)
        .max(200)
        .sliderMin(5)
        .sliderMax(200)
        .build()
    );

    private final Setting<Boolean> renderLanding = sgGeneral.add(new BoolSetting.Builder()
        .name("render-landing")
        .description("Renders a box at the predicted landing spot.")
        .defaultValue(true)
        .build()
    );

    private Entity tracked;

    public PearlAim() {
        super(Categories.Combat, "pearl-aim", "Predicts and highlights where a thrown ender pearl will land, helping you aim or track.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null || mc.world == null) {
            tracked = null;
            return;
        }

        tracked = null;
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity p && p != mc.player && p.isAlive()) {
                if (p.getMainHandStack().getItem() instanceof EnderPearlItem && mc.player.distanceTo(p) <= range.get()) {
                    tracked = p;
                    break;
                }
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (tracked == null || !renderLanding.get()) return;

        Vec3d pos = new Vec3d(tracked.getX(), tracked.getY(), tracked.getZ());
        Vec3d vel = tracked.getVelocity();

        // Simple parabolic prediction: ender pearl speed ~= 1.5 * eye velocity
        double vx = vel.x * 1.5;
        double vy = vel.y * 1.5;
        double vz = vel.z * 1.5;

        double px = pos.x, py = pos.y + tracked.getEyeHeight(tracked.getPose()), pz = pos.z;
        for (int i = 0; i < 40; i++) {
            px += vx;
            py += vy;
            pz += vz;
            vy -= 0.03; // gravity

            if (py <= mc.world.getTopYInclusive()) {
                var blockPos = net.minecraft.util.math.BlockPos.ofFloored(px, py, pz);
                if (!mc.world.isAir(blockPos)) break;
                if (i > 20) {
                    Box box = new Box(px - 0.3, py - 0.3, pz - 0.3, px + 0.3, py + 0.3, pz + 0.3);
                    event.renderer.box(box, new Color(80, 200, 120, 60), new Color(80, 200, 120), ShapeMode.Both, 0);
                    break;
                }
            }
        }
    }
}
