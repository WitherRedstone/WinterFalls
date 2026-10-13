package com.chinaex123.winter_falls.mixin;

import com.chinaex123.winter_falls.config.WFConfig;
import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 生物群系 Mixin。
 * <p>
 * 在冬季世界中强制覆盖降水、温度与植被颜色逻辑，
 * 使所有生物群系都表现为寒冷雪原环境，
 * 避免重进存档后恢复为原版数值。
 */
@Mixin(Biome.class)
public class BiomeMixin {

    /**
     * 强制生物群系拥有降水。
     * <p>
     * 当冬季世界处于激活状态时，无论原版生物群系是否拥有降水，均返回 true。
     *
     * @param cir 回调信息
     */
    @Inject(method = "hasPrecipitation", at = @At("HEAD"), cancellable = true)
    private void winterfalls$forcePrecipitation(CallbackInfoReturnable<Boolean> cir) {
        if (WinterFallsWorld.isWinterWorldActive()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * 强制指定位置的降水类型为雪。
     * <p>
     * 当冬季世界处于激活状态时，将降水类型固定为 SNOW，
     * 避免出现降雨。
     *
     * @param pos 方块位置
     * @param cir 回调信息
     */
    @Inject(method = "getPrecipitationAt", at = @At("HEAD"), cancellable = true)
    private void winterfalls$forceSnowPrecipitation(BlockPos pos, CallbackInfoReturnable<Biome.Precipitation> cir) {
        if (WinterFallsWorld.isWinterWorldActive()) {
            cir.setReturnValue(Biome.Precipitation.SNOW);
        }
    }

    /**
     * 强制生物群系基础温度为寒冷值。
     * <p>
     * 当冬季世界处于激活状态时，将基础温度固定为 0.0F，
     * 避免重进存档后温度恢复为原版数值。
     *
     * @param cir 回调信息
     */
    @Inject(method = "getBaseTemperature", at = @At("HEAD"), cancellable = true)
    private void winterfalls$forceColdTemperature(CallbackInfoReturnable<Float> cir) {
        if (WinterFallsWorld.isWinterWorldActive()) {
            cir.setReturnValue(-0.5F);
        }
    }

    /**
     * 强制生物群系寒冷到无法降雨。
     * <p>
     * 当冬季世界处于激活状态时，返回 false 表示温度不足以降雨，
     * 从而配合温度逻辑使降水表现为雪。
     *
     * @param pos 方块位置
     * @param cir 回调信息
     */
    @Inject(method = "warmEnoughToRain", at = @At("HEAD"), cancellable = true)
    private void winterfalls$forceColdEnoughToRain(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (WinterFallsWorld.isWinterWorldActive()) {
            cir.setReturnValue(false);
        }
    }

    /**
     * 强制生物群系允许降雪。
     * <p>
     * 当配置开启降水覆盖且冬季世界处于激活状态时，
     * 无论原版生物群系是否允许下雪，均返回 true。
     *
     * @param level 世界读取器
     * @param pos   方块位置
     * @param cir   回调信息
     */
    @Inject(method = "shouldSnow", at = @At("HEAD"), cancellable = true)
    private void winterfalls$forceShouldSnow(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (WFConfig.OVERRIDE_PRECIPITATION.get() && WinterFallsWorld.isWinterWorldActive()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * 覆盖草方块颜色为寒冷色调。
     * <p>
     * 当冬季世界处于激活状态时，使用冷色温参数获取草方块颜色，
     * 使植被呈现冬季灰绿色。
     *
     * @param posX 方块 X 坐标
     * @param posZ 方块 Z 坐标
     * @param cir  回调信息
     */
    @Inject(method = "getGrassColor", at = @At("HEAD"), cancellable = true)
    private void winterfalls$coldGrass(double posX, double posZ, CallbackInfoReturnable<Integer> cir) {
        if (WinterFallsWorld.isWinterWorldActive()) {
            cir.setReturnValue(GrassColor.get(0.0, 0.5));
        }
    }

    /**
     * 覆盖树叶颜色为寒冷色调。
     * <p>
     * 当冬季世界处于激活状态时，使用冷色温参数获取树叶颜色，
     * 使植被呈现冬季灰绿色。
     *
     * @param cir 回调信息
     */
    @Inject(method = "getFoliageColor", at = @At("HEAD"), cancellable = true)
    private void winterfalls$coldFoliage(CallbackInfoReturnable<Integer> cir) {
        if (WinterFallsWorld.isWinterWorldActive()) {
            cir.setReturnValue(FoliageColor.get(0.0, 0.5));
        }
    }

//    @Inject(method = "shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
//    private void winterfalls$forceFreeze(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
//        if (WinterFallsWorld.isWinterWorldActive()) {
//            cir.setReturnValue(true);
//        }
//    }
//
//    @Inject(method = "shouldMeltFrozenOceanIcebergSlightly", at = @At("HEAD"), cancellable = true)
//    private void winterfalls$noIcebergMelt(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
//        if (WinterFallsWorld.isWinterWorldActive()) {
//            cir.setReturnValue(false);
//        }
//    }

}