package com.chinaex123.winter_falls.mixin;

import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.minecraft.world.level.levelgen.feature.SnowAndFreezeFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 雪与冰冻地物 Mixin。
 * <p>
 * 在冬季世界中禁用原版的雪与冰冻地物生成，
 * 避免与冬季世界自身的降雪逻辑重复叠加。
 */
@Mixin(SnowAndFreezeFeature.class)
public class SnowAndFreezeFeatureMixin {

    /**
     * 在冬季世界中禁用雪与冰冻地物生成。
     * <p>
     * 当冬季世界处于激活状态时，直接返回 false 跳过原版放置逻辑，
     * 防止与冬季世界自带的积雪机制重复。
     *
     * @param pContext 地物放置上下文
     * @param ci       回调信息
     */
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void winterfalls$disableInWinterWorld(
            FeaturePlaceContext<NoneFeatureConfiguration> pContext,
            CallbackInfoReturnable<Boolean> ci) {
        if (WinterFallsWorld.isWinterWorldActive()) {
            ci.setReturnValue(false);
        }
    }
}