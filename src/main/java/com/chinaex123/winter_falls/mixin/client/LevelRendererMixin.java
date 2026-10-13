package com.chinaex123.winter_falls.mixin.client;

import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 世界渲染器 Mixin。
 * <p>
 * 拦截客户端降水音效播放，在冬季世界中屏蔽原版降雨音效，
 * 避免冬季世界播放不合时宜的雨声。
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    /**
     * 拦截降雨音效播放。
     * <p>
     * 在冬季世界中屏蔽原版降雨音效，
     * 其余音效则照常调用原方法播放。
     *
     * @param level         客户端世界
     * @param pos           音效播放位置
     * @param sound         音效事件
     * @param source        音效来源
     * @param volume        音量
     * @param pitch         音调
     * @param distanceDelay 是否启用距离延迟
     */
    @Redirect(
            method = "tickRain",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;playLocalSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"
            )
    )
    private void winterfalls$noRainSound(ClientLevel level, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch, boolean distanceDelay) {
        if (WinterFallsWorld.isWinterFallsWorld(level)
                && (sound == SoundEvents.WEATHER_RAIN || sound == SoundEvents.WEATHER_RAIN_ABOVE)) {
            return;
        }
        level.playLocalSound(pos, sound, source, volume, pitch, distanceDelay);
    }
}