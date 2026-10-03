package com.chinaex123.winter_falls.mixin;

import com.chinaex123.winter_falls.config.WFConfig;
import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 生物群系 Mixin。
 * <p>
 * 在冬季世界且配置启用时，强制生物群系允许降雪，
 * 使原本不降雪的生物群系也能形成积雪。
 */
@Mixin(Biome.class)
public class BiomeMixin {

    /**
     * 在判断是否降雪的头部注入，冬季世界时强制返回 true。
     * <p>
     * 仅当配置启用了降水覆盖、目标世界为冬季世界时生效。
     *
     * @param level 世界读取器
     * @param pos   方块位置
     * @param cir   回调信息，用于设置返回值
     */
    @Inject(method = "shouldSnow", at = @At("HEAD"), cancellable = true)
    private void forceShouldSnow(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (WFConfig.OVERRIDE_PRECIPITATION.get() && level instanceof Level lvl
                && WinterFallsWorld.isWinterFallsWorld(lvl)) {
            cir.setReturnValue(true);
        }
    }
}