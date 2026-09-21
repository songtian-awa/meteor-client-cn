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
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class SignESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of sign highlights.")
        .defaultValue(new SettingColor(255, 200, 80, 100))
        .build()
    );

    private final Setting<Boolean> onlyWithText = sgGeneral.add(new BoolSetting.Builder()
        .name("only-with-text")
        .description("Only highlights signs that have text on them.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> renderText = sgGeneral.add(new BoolSetting.Builder()
        .name("render-text")
        .description("Renders the sign text above the sign.")
        .defaultValue(true)
        .build()
    );

    public SignESP() {
        super(Categories.Render, "sign-esp", "Highlights signs so you can find important locations easily.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        int range = 32;
        BlockPos center = mc.player.getBlockPos();

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    BlockEntity be = mc.world.getBlockEntity(pos);
                    if (!(be instanceof SignBlockEntity sign)) continue;

                    boolean hasText = false;
                    for (int i = 0; i < 4; i++) {
                        String text = sign.getText(true).getMessage(i, false).getString();
                        if (!text.isBlank()) {
                            hasText = true;
                            break;
                        }
                    }

                    if (onlyWithText.get() && !hasText) continue;

                    Box box = new Box(pos);
                    event.renderer.box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color.get(), color.get(), ShapeMode.Both, 0);

                    if (renderText.get() && hasText) {
                        String line = sign.getText(true).getMessage(0, false).getString();
                        if (!line.isBlank()) {
                            event.renderer.line(box.minX, box.maxY, box.minZ, box.minX, box.maxY + 0.5, box.minZ, color.get());
                        }
                    }
                }
            }
        }
    }
}
