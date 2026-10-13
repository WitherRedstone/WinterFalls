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
 * 在冬季世界中强制覆盖降雨渲染强度与降雨判定逻辑，
 * 使客户端始终渲染满强度的降雪，并让露天位置一律判定为降雨。
 */
@Mixin(Level.class)
public class LevelMixin {

    /**
     * 强制降雨渲染强度为满值。
     * <p>
     * 在冬季世界中固定返回 1.0F，
     * 确保客户端始终以最大强度渲染降雪效果。
     *
     * @param delta 部分 tick 插值
     * @param cir   回调信息
     */
    @Inject(method = "getRainLevel", at = @At("HEAD"), cancellable = true)
    private void winterfalls$forceRenderSnowEntry(float delta, CallbackInfoReturnable<Float> cir) {
        Level level = (Level) (Object) this;
        if (WinterFallsWorld.isWinterFallsWorld(level)) {
            cir.setReturnValue(1.0F);
        }
    }

    /**
     * 强制露天位置判定为降雨。
     * <p>
     * 在冬季世界中，只要该位置能看到天空，
     * 就返回 true 表示此处正在降水。
     *
     * @param pos 方块位置
     * @param cir 回调信息
     */
    @Inject(method = "isRainingAt", at = @At("HEAD"), cancellable = true)
    private void winterfalls$forceSnowAtOpenSky(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;
        if (!WinterFallsWorld.isWinterFallsWorld(level)) return;
        cir.setReturnValue(level.canSeeSky(pos));
    }
}