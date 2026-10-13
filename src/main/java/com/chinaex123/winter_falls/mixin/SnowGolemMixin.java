package com.chinaex123.winter_falls.mixin;

import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.minecraft.world.entity.animal.SnowGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 雪傀儡 Mixin。
 * <p>
 * 在冬季世界中使雪傀儡免疫水的伤害，
 * 避免其在积雪融化、冰雪环境中因接触水而受损。
 */
@Mixin(SnowGolem.class)
public class SnowGolemMixin {

    /**
     * 使雪傀儡不再对水敏感。
     * <p>
     * 当冬季世界处于激活状态时，返回 false 表示雪傀儡不受水伤害，
     * 从而在冬季世界中正常存活。
     *
     * @param cir 回调信息
     */
    @Inject(method = "isSensitiveToWater", at = @At("HEAD"), cancellable = true)
    private void winterfalls$makeSnowGolemWaterproof(CallbackInfoReturnable<Boolean> cir) {
        if (WinterFallsWorld.isWinterWorldActive()) {
            cir.setReturnValue(false);
        }
    }
}