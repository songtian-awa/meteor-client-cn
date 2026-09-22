/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.custom;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;

public class KillCount extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> showEveryKill = sgGeneral.add(new BoolSetting.Builder()
        .name("show-every-kill")
        .description("Announce every kill in chat.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> resetOnDeath = sgGeneral.add(new BoolSetting.Builder()
        .name("reset-on-death")
        .description("Reset the counter when you die.")
        .defaultValue(true)
        .build()
    );

    private int kills;
    private LivingEntity lastTarget;
    private boolean lastTargetDead;

    public KillCount() {
        super(Categories.Misc, "kill-count", "Counts how many kills you get and announces each one.");
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (!Utils.canUpdate() || !(event.entity instanceof LivingEntity living)) return;

        lastTarget = living;
        lastTargetDead = living.getHealth() <= 0;
    }

    @EventHandler
    private void onTick(meteordevelopment.meteorclient.events.world.TickEvent.Pre event) {
        if (!Utils.canUpdate() || mc.player == null) return;

        if (lastTarget != null) {
            if (lastTarget.isDead() || lastTarget.getHealth() <= 0) {
                kills++;
                if (showEveryKill.get()) {
                    ChatUtils.infoPrefix("Kills", "You killed %s! Total: %d", lastTarget.getDisplayName() != null ? lastTarget.getDisplayName().getString() : "entity", kills);
                }
                lastTarget = null;
            } else if (lastTargetDead) {
                lastTarget = null;
            }
        }

        if (resetOnDeath.get() && mc.player.isDead()) {
            kills = 0;
        }
    }

    public int getKills() {
        return kills;
    }
}
