/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Chinese localization support (i18n) for Meteor Client.
 * Approach adapted from dingzhen-vape/Meteor-I18n-Support-plugin (GPL-3.0).
 *
 * Registers the built-in CJK font (WenQuanWeiMiHei) and makes it the default UI font.
 */

package meteordevelopment.meteorclient.mixin.i18n;

import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFamily;
import meteordevelopment.meteorclient.utils.render.FontUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = Fonts.class, remap = false)
public abstract class FontsMixin {
    @Shadow @Final public static List<FontFamily> FONT_FAMILIES;
    @Shadow @Final public static String[] BUILTIN_FONTS;

    @Inject(method = "refresh", at = @At("HEAD"))
    private static void changeDefaultFont(CallbackInfo ci) {
        BUILTIN_FONTS[1] = "WenQuanWeiMiHei";
    }

    @Inject(method = "refresh", at = @At(value = "INVOKE", target = "Ljava/util/List;sort(Ljava/util/Comparator;)V"))
    private static void refresh(CallbackInfo ci) {
        FontUtils.loadBuiltin(FONT_FAMILIES, "WenQuanWeiMiHei");
    }
}
