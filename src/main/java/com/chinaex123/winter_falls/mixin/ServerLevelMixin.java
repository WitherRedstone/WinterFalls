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
 * 在冬季世界中接管原版降水 tick 逻辑，
 * 改用自定义的积雪累积处理，并维护全局 tick 计数供其使用。
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    /**
     * 世界 tick 计数器。
     * <p>
     * 每个服务端 tick 自增一次，用于驱动自定义积雪累积逻辑。
     */
    @Unique
    private int winterfalls$tickCounter = 0;

    /**
     * 在世界 tick 开始时递增计数器。
     *
     * @param ci 回调信息
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        winterfalls$tickCounter++;
    }

    /**
     * 拦截原版降水 tick。
     * <p>
     * 在冬季世界中取消原版的降水处理，
     * 避免与原版降雪逻辑冲突，交由自定义积雪逻辑接管。
     *
     * @param pos 方块位置
     * @param ci  回调信息
     */
    @Inject(method = "tickPrecipitation", at = @At("HEAD"), cancellable = true)
    private void onTickPrecipitation(BlockPos pos, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (WinterFallsWorld.isWinterFallsWorld(level)) {
            ci.cancel();
        }
    }

    /**
     * 在区块 tick 结束后补充积雪。
     * <p>
     * 当冬季世界处于激活状态时，调用自定义积雪处理，
     * 根据全局 tick 计数为当前区块放置额外的积雪。
     *
     * @param chunk           当前区块
     * @param randomTickSpeed 随机 tick 速率
     * @param ci              回调信息
     */
    @Inject(method = "tickChunk", at = @At("RETURN"))
    private void onTickChunk(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (!WinterFallsWorld.isWinterFallsWorld(level)) return;
        SnowAccumulationHandler.placeExtraSnow(level, chunk, winterfalls$tickCounter);
    }
}