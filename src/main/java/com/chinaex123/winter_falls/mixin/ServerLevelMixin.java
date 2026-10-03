package com.chinaex123.winter_falls.mixin;

import com.chinaex123.winter_falls.event.SnowAccumulationHandler;
import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 服务端世界 Mixin。
 * <p>
 * 在冬季世界中接管降水与积雪逻辑：
 * 屏蔽原版降水刻逻辑，改为在区块刻完成后交由积雪处理器按配置铺雪，
 * 并自行维护一个刻计数器供积雪频率控制使用。
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    /** 本 Mixin 维护的刻计数器，用于积雪频率控制 */
    @Unique
    private int winterfalls$tickCounter = 0;

    /**
     * 在区块刻完成后注入，尝试为区块铺设额外积雪。
     * <p>
     * 仅对冬季世界生效，将当前刻计数器一并传入积雪处理器。
     *
     * @param chunk     目标区块
     * @param tickSpeed 刻速度
     * @param ci        回调信息
     */
    @Inject(method = "tickChunk", at = @At(value = "RETURN"))
    private void onTickChunk(LevelChunk chunk, int tickSpeed, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (!WinterFallsWorld.isWinterFallsWorld(level)) return;
        SnowAccumulationHandler.placeExtraSnow(level, chunk, winterfalls$tickCounter);
    }

    /**
     * 在降水刻头部注入，冬季世界时取消原逻辑。
     * <p>
     * 原版降水逻辑由自定义积雪逻辑替代，仅在冬季世界中取消。
     *
     * @param pos 方块位置
     * @param ci  回调信息，用于取消原方法执行
     */
    @Inject(method = "tickPrecipitation", at = @At("HEAD"), cancellable = true)
    private void onTickPrecipitation(BlockPos pos, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (WinterFallsWorld.isWinterFallsWorld(level)) {
            ci.cancel();
        }
    }

    /**
     * 在世界刻头部注入，自增刻计数器。
     *
     * @param ci 回调信息
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        winterfalls$tickCounter++;
    }
}