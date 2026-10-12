package com.chinaex123.winter_falls.mixin;

import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 世界 Mixin。
 * <p>
 * 在冬季世界中覆盖与降水、日月显示相关的判断：
 * 固定降雨强度以配合永久雪天，
 * 并覆盖指定位置是否降雨的判断。
 */
@Mixin(Level.class)
public class LevelMixin {

    /**
     * 在获取降雨强度的头部注入，冬季世界时固定返回满强度。
     * <p>
     * 通过固定降雨强度，间接隐藏日月显示效果。
     *
     * @param delta 部分 tick 插值
     * @param cir   回调信息，用于设置返回值
     */
    @Inject(method = "getRainLevel", at = @At("HEAD"), cancellable = true)
    private void winterfalls$hideSunMoon(float delta, CallbackInfoReturnable<Float> cir) {
        Level level = (Level) (Object) this;
        if (WinterFallsWorld.isWinterFallsWorld(level)) {
            cir.setReturnValue(1.0F);
        }
    }

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