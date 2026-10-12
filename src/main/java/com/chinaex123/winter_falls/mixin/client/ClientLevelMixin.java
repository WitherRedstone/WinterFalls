package com.chinaex123.winter_falls.mixin.client;

import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 客户端世界 Mixin。
 * <p>
 * 在冬季世界中覆盖天空颜色，使其固定为冬季灰蓝色，
 * 避免重进存档后天空恢复为原版蓝色。
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    /**
     * 覆盖天空颜色。
     *
     * @param pos        相机位置
     * @param partialTick 部分 tick 插值
     * @param cir        回调信息
     */
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void winterfalls$winterSkyColor(Vec3 pos, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        if (WinterFallsWorld.isWinterFallsWorld((ClientLevel) (Object) this)) {
            cir.setReturnValue(new Vec3(0.75, 0.82, 0.90));
        }
    }
}