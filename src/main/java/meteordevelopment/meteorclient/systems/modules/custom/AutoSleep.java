/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoSleep extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> minPhantomRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("min-phantom-range")
        .description("Only sleep when a phantom is within this range, or always at night.")
        .defaultValue(0.0)
        .min(0.0)
        .max(64.0)
        .sliderMin(0.0)
        .sliderMax(64.0)
        .build()
    );

    private final Setting<Boolean> onlyNight = sgGeneral.add(new BoolSetting.Builder()
        .name("only-night")
        .description("Only sleep when it is night time.")
        .defaultValue(true)
        .build()
    );

    private int cooldown;

    public AutoSleep() {
        super(Categories.Player, "auto-sleep", "Automatically gets into a nearby bed when phantoms approach or at night.");
    }

    @Override
    public void onActivate() {
        cooldown = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (mc.player.isSleeping()) return;
        if (mc.player.hasStatusEffect(StatusEffects.BAD_OMEN)) return;

        if (onlyNight.get() && mc.world.getTimeOfDay() % 24000 < 13000) return;

        boolean phantomNear = false;
        if (minPhantomRange.get() > 0) {
            double r = minPhantomRange.get();
            for (var entity : mc.world.getEntities()) {
                if (entity.getType() == net.minecraft.entity.EntityType.PHANTOM && mc.player.squaredDistanceTo(entity) < r * r) {
                    phantomNear = true;
                    break;
                }
            }
        }

        if (minPhantomRange.get() > 0 && !phantomNear) return;

        // Find the nearest bed within 6 blocks
        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos bedPos = null;
        double bestDist = Double.MAX_VALUE;

        for (int x = -6; x <= 6; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -6; z <= 6; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() instanceof BedBlock) {
                        double dist = playerPos.getSquaredDistance(pos);
                        if (dist < bestDist) {
                            bestDist = dist;
                            bedPos = pos;
                        }
                    }
                }
            }
        }

        if (bedPos == null) return;

        // Right-click the bed
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
            new BlockHitResult(bedPos.toCenterPos(), Direction.UP, bedPos, false));

        cooldown = 40;
    }
}
