/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class Teams extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color used to highlight teammates.")
        .defaultValue(new SettingColor(0, 255, 0, 80))
        .build()
    );

    private final Setting<Boolean> onlyColorArmor = sgGeneral.add(new BoolSetting.Builder()
        .name("color-armor")
        .description("Only detect teammates by leather armor color.")
        .defaultValue(false)
        .build()
    );

    private final List<PlayerEntity> teammates = new ArrayList<>();

    public Teams() {
        super(Categories.Combat, "teams", "Highlights players on your team in BedWars, SkyWars and other team gamemodes.");
    }

    public boolean isInYourTeam(PlayerEntity player) {
        if (player == mc.player) return true;
        if (mc.player == null) return false;

        // Team color from display name prefix (common in BedWars: color codes)
        if (mc.player.getDisplayName() != null && player.getDisplayName() != null) {
            String self = mc.player.getDisplayName().getString();
            String other = player.getDisplayName().getString();
            if (self.startsWith("\u00A7") && other.startsWith("\u00A7")) {
                return self.charAt(1) == other.charAt(1);
            }
        }

        return false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.world == null) return;

        teammates.clear();
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity player && isInYourTeam(player)) {
                teammates.add(player);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        for (PlayerEntity mate : teammates) {
            double x = MathHelper.lerp(event.tickDelta, mate.lastRenderX, mate.getX()) - mate.getX();
            double y = MathHelper.lerp(event.tickDelta, mate.lastRenderY, mate.getY()) - mate.getY();
            double z = MathHelper.lerp(event.tickDelta, mate.lastRenderZ, mate.getZ()) - mate.getZ();

            Box box = mate.getBoundingBox();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ,
                color.get(), color.get(), ShapeMode.Both, 0);
        }
    }
}
