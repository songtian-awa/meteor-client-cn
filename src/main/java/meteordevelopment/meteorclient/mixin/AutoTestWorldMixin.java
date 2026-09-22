/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Exposes CreateWorldScreen.createLevel() (private) for the automated self-test hook.
 */

package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CreateWorldScreen.class)
public interface AutoTestWorldMixin {
    @Invoker("createLevel")
    void invokeCreateLevel();
}
