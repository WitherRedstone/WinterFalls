package com.chinaex123.winter_falls.mixin;

import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 世界 Mixin。
 * <p>
 * 在冬季世界中覆盖指定位置是否降雨的判断：
 * 可露天时视为持续降雨，被遮挡时视为不降雨，
 * 以配合永久雪天与积雪逻辑。
 */
@Mixin(Level.class)
public class LevelMixin {

    /**
     * 在判断指定位置是否降雨的头部注入。
     * <p>
     * 仅对冬季世界生效：若该位置可露天则强制返回 true，
     * 否则返回 false；非冬季世界时放行原逻辑。
     *
     * @param pos 方块位置
     * @param cir 回调信息，用于设置返回值
     */
    @Inject(method = "isRainingAt", at = @At("HEAD"), cancellable = true)
    private void forceRainingAt(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;
        if (!WinterFallsWorld.isWinterFallsWorld(level)) return;
        if (!level.canSeeSky(pos)) {
            cir.setReturnValue(false);
            return;
        }
        cir.setReturnValue(true);
    }
}