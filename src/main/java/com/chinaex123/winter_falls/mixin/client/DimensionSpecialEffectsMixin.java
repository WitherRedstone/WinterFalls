package com.chinaex123.winter_falls.mixin.client;

import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 维度视觉效果 Mixin。
 * <p>
 * 在冬季世界中屏蔽日出与日落的天空染色效果，
 * 避免其干扰冬季世界自定义的雾色与整体色调。
 */
@Mixin(DimensionSpecialEffects.class)
public abstract class DimensionSpecialEffectsMixin {

    /**
     * 在获取日出颜色的返回处注入，冬季世界时返回 null 以禁用该效果。
     * <p>
     * 原返回值本就为 null 时直接返回；当前世界不是冬季世界时也放行原值。
     *
     * @param skyAngle  天空角度
     * @param tickDelta 部分 tick 插值
     * @param cir       回调信息，用于读取与设置返回值
     */
    @Inject(method = "getSunriseColor", at = @At("RETURN"), cancellable = true)
    private void noSunriseColor(float skyAngle, float tickDelta, CallbackInfoReturnable<float[]> cir) {
        final float[] original = cir.getReturnValue();
        if (original == null) return;

        final var level = Minecraft.getInstance().level;
        if (!WinterFallsWorld.isWinterFallsWorld(level)) return;

        cir.setReturnValue(null);
    }
}