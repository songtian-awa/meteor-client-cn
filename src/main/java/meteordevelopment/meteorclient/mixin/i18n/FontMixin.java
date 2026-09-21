/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Chinese localization support (i18n) for Meteor Client.
 * Approach adapted from dingzhen-vape/Meteor-I18n-Support-plugin (GPL-3.0).
 *
 * Expands the bitmap font atlas so CJK glyphs (Han characters) can be packed and rendered.
 */

package meteordevelopment.meteorclient.mixin.i18n;

import meteordevelopment.meteorclient.renderer.text.Font;
import org.lwjgl.stb.STBTTPackRange;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.CustomBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value = Font.class, remap = false)
public abstract class FontMixin {
    /** CJK start codepoint 0x4E00, count 20976 (common + less common characters) */
    @Unique private static final int CJK_COUNT = 20976;
    @Unique private static final int CJK_START = 0x4E00;
    @Unique private static STBTTPackedchar.Buffer cjkCharData;

    @ModifyVariable(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/stb/STBTruetype;stbtt_PackBegin(Lorg/lwjgl/stb/STBTTPackContext;Ljava/nio/ByteBuffer;IIII)Z"), name = "cdata")
    private STBTTPackedchar.Buffer[] addCjkCdata(STBTTPackedchar.Buffer[] cdata) {
        cjkCharData = STBTTPackedchar.create(CJK_COUNT);
        STBTTPackedchar.Buffer[] list = new STBTTPackedchar.Buffer[cdata.length + 1];
        System.arraycopy(cdata, 0, list, 0, cdata.length);
        list[cdata.length] = cjkCharData;
        return list;
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/stb/STBTTPackRange$Buffer;flip()Lorg/lwjgl/system/CustomBuffer;"))
    private CustomBuffer flipWithCjk(STBTTPackRange.Buffer packRange) {
        // Append CJK range (position currently 6, capacity 7)
        packRange.put(STBTTPackRange.create().set(
            packRange.get(0).font_size(),
            CJK_START,
            null,
            CJK_COUNT,
            cjkCharData,
            (byte) 2, (byte) 2
        ));
        return packRange.flip();
    }

    // 1) atlas size: 2048 -> 8192
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 2048))
    private int changeSize(int value) {
        return 8192;
    }

    // 2) size*size bitmap allocation: 4194304 -> 8192*8192 = 67108864
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 4194304))
    private int changeBitmapSize(int value) {
        return 8192 * 8192;
    }

    // 3) 1f/size UV scale: 4.8828125E-4f -> 1f/8192 = 1.220703125E-4f
    @ModifyConstant(method = "<init>", constant = @Constant(floatValue = 4.8828125E-4f))
    private float changeUvScale(float value) {
        return 1f / 8192;
    }
}
