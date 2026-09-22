/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Chinese localization support (i18n) for Meteor Client.
 * Approach adapted from dingzhen-vape/Meteor-I18n-Support-plugin (GPL-3.0).
 */

package meteordevelopment.meteorclient.mixin.i18n;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.i18n.Translator;
import meteordevelopment.meteorclient.systems.modules.Module;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Module.class, remap = false, priority = 999)
public abstract class ModuleMixin {
    @Shadow @Final @Mutable public String title;
    @Shadow @Final @Mutable public String description;
    @Shadow @Final public MeteorAddon addon;
    @Shadow @Final public String name;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (addon == null) return;

        String packageName = addon.name.replace(" ", "-");
        if (packageName.equals("Meteor-Client")) packageName = "Meteor"; // legacy key compatibility

        String key = "Module." + packageName + "." + name;
        title = Translator.translate(key, title);
        description = Translator.translate(key + ".Description", description);
    }
}
