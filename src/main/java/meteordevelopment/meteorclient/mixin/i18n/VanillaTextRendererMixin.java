/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Chinese localization support (i18n) for Meteor Client.
 * Approach adapted from dingzhen-vape/Meteor-I18n-Support-plugin (GPL-3.0).
 */

package meteordevelopment.meteorclient.mixin.i18n;

import meteordevelopment.meteorclient.renderer.text.VanillaTextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VanillaTextRenderer.class, remap = false)
public abstract class VanillaTextRendererMixin {
    @Shadow public boolean scaleIndividually;

    @Inject(method = "end", at = @At("RETURN"))
    private void onEnd(CallbackInfo ci) {
        this.scaleIndividually = true;
    }
}
