/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Chinese localization support (i18n) for Meteor Client.
 * Approach adapted from dingzhen-vape/Meteor-I18n-Support-plugin (GPL-3.0).
 */

package meteordevelopment.meteorclient.mixin.i18n;

import meteordevelopment.meteorclient.addons.AddonManager;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.i18n.Translator;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(value = Setting.class, remap = false)
public abstract class SettingMixin {
    @Shadow @Final @Mutable public String title;
    @Shadow @Final @Mutable public String description;
    @Unique private MeteorAddon addon;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(String name, String description, Object defaultValue, Consumer onChanged, Consumer onModuleActivated, IVisible visible, CallbackInfo ci) {
        String classname = this.getClass().getName();
        for (MeteorAddon addon : AddonManager.ADDONS) {
            if (classname.startsWith(addon.getPackage())) {
                this.addon = addon;
                break;
            }
        }
        if (this.addon == null) return;

        String packageName = this.addon.name.replace(" ", "-");
        if (packageName.equals("Meteor-Client")) packageName = "Meteor"; // legacy key compatibility

        String key = "Setting." + packageName + "." + name;
        title = Translator.translate(key, title);
        description = Translator.translate(key + ".Description", description);
    }
}
