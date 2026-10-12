package com.chinaex123.winter_falls.mixin;

import com.chinaex123.winter_falls.config.WFConfig;
import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 生物群系 Mixin。
 * <p>
 * 在冬季世界中强制覆盖降水、温度与颜色逻辑，
 * 使所有生物群系都能下雪、积雪、结冰并呈现冷色调。
 * <p>
 * 所有注入均使用 {@link WinterFallsWorld#isWinterWorldActive()} 这一
 * 内存标记进行判断，避免在渲染与游戏刻热路径中触发存档查询与网络发包。
 */
@Mixin(Biome.class)
public class BiomeMixin {

    @Shadow
    @Final
    private BiomeSpecialEffects specialEffects;

    /**
     * 强制生物群系拥有降水，使沙漠等原版无降水的群系也能进入雨雪渲染分支。
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
     * 强制降水类型为 SNOW，使原版雨雪渲染绘制雪片。
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
     * 强制基础温度为寒冷值，使所有生物群系被视为寒冷群系。
     *
     * @param cir 回调信息
     */
    @Inject(method = "getBaseTemperature", at = @At("HEAD"), cancellable = true)
    private void winterfalls$forceColdTemperature(CallbackInfoReturnable<Float> cir) {
        if (WinterFallsWorld.isWinterWorldActive()) {
            cir.setReturnValue(0.0F);
        }
    }

    /**
     * 强制 {@code warmEnoughToRain} 返回 false。
     * <p>
     * 该值被 {@code coldEnoughToSnow}、{@code shouldFreeze} 等方法使用，
     * 覆盖后即可让水面结冰、积雪等逻辑按寒冷群系运行，
     * 同时保留原版的光照与边缘判断。
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
     * 仅当配置开启降水覆盖且当前为冬季世界时生效。
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
     * 覆盖草方块颜色为雪原色。
     */
    @Inject(method = "getGrassColor", at = @At("HEAD"), cancellable = true)
    private void winterfalls$coldGrass(double posX, double posZ, CallbackInfoReturnable<Integer> cir) {
        if (WinterFallsWorld.isWinterWorldActive()) {
            cir.setReturnValue(GrassColor.get(0.0, 0.5));
        }
    }

    /**
     * 覆盖树叶颜色为雪原色。
     */
    @Inject(method = "getFoliageColor", at = @At("HEAD"), cancellable = true)
    private void winterfalls$coldFoliage(CallbackInfoReturnable<Integer> cir) {
        if (WinterFallsWorld.isWinterWorldActive()) {
            cir.setReturnValue(FoliageColor.get(0.0, 0.5));
        }
    }
}