package com.chinaex123.winter_falls.client;

import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.FogType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 客户端雾效处理器。
 * <p>
 * 仅在冬季世界且相机未处于流体中时生效：
 * 依据玩家眼位处的天空光照强度计算目标雾浓度，并以平滑方式逼近该值，
 * 据此调整地形雾的近远平面距离与雾颜色。
 */
public class ClientFogHandler {

    /** 上一帧的雾浓度，-1 表示未初始化 */
    private static float prevFogDensity = -1f;
    /** 上一帧的时间戳（毫秒），-1 表示未初始化 */
    private static long prevFogTick = -1L;

    /**
     * 处理地形雾的渲染事件。
     * <p>
     * 仅在冬季世界、相机未处于流体中时生效：以当前时间与上一帧时间的差值
     * 计算帧间隔，根据玩家眼位处天空光照得到目标雾浓度，
     * 平滑逼近后据此设置雾的近远平面距离并取消原渲染。
     * 非冬季世界或相机处于流体中时重置缓存状态。
     *
     * @param event 视口渲染雾事件
     */
    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        if (!WinterFallsWorld.isWinterFallsWorld(mc.level)) {
            prevFogDensity = -1f;
            prevFogTick = -1L;
            return;
        }

        if (event.getCamera().getFluidInCamera() != FogType.NONE) {
            prevFogDensity = -1f;
            prevFogTick = -1L;
            return;
        }

        long now = System.currentTimeMillis();
        boolean firstTick = prevFogTick == -1L;
        float deltaTick = firstTick ? 1e10f : (now - prevFogTick) * 0.00005f;
        prevFogTick = now;

        Player player = mc.player;
        float expectedFogDensity = 0f;
        if (player != null) {
            int skyLight = mc.level.getBrightness(LightLayer.SKY,
                    BlockPos.containing(player.getEyePosition()));
            expectedFogDensity = Mth.clampedMap(skyLight, 0f, 15f, 0f, 1f);
        }

        // 雾浓度向目标值平滑逼近：变浓更快，变淡较缓
        if (expectedFogDensity > prevFogDensity) {
            prevFogDensity = Math.min(prevFogDensity + 4f * deltaTick, expectedFogDensity);
        } else if (expectedFogDensity < prevFogDensity) {
            prevFogDensity = Math.max(prevFogDensity - deltaTick, expectedFogDensity);
        }

        if (prevFogDensity <= 0.01f) return;

        float scaledDelta = 1f - (1f - prevFogDensity) * (1f - prevFogDensity);
        float renderDist = mc.gameRenderer.getRenderDistance();
        float fogDensityCfg = 0.35f;

        float farPlaneDistance  = renderDist * Mth.lerp(scaledDelta, 1f, fogDensityCfg);
        float nearPlaneDistance = renderDist * Mth.lerp(scaledDelta, 1f, 0.3f * fogDensityCfg);

        event.setFarPlaneDistance(farPlaneDistance);
        event.setNearPlaneDistance(nearPlaneDistance);
        event.setCanceled(true);
    }

    /**
     * 处理雾颜色的计算事件。
     * <p>
     * 仅在冬季世界、相机未处于流体中且当前雾浓度超过阈值时生效：
     * 依据太阳角度插值得到昼夜过渡系数，
     * 在夜色与日色之间线性插值出雾的 RGB 颜色并写入事件。
     *
     * @param event 视口计算雾颜色事件
     */
    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!WinterFallsWorld.isWinterFallsWorld(mc.level)) return;

        if (mc.gameRenderer.getMainCamera().getFluidInCamera() != FogType.NONE) return;

        if (prevFogDensity <= 0.01f) return;

        float angle = mc.level.getSunAngle((float) event.getPartialTick());
        float height = Mth.cos(angle);
        float delta = Mth.clamp((height + 0.4f) / 0.8f, 0f, 1f);

        float r = (0xC0 * delta + 0x20 * (1f - delta)) / 255f;
        float g = (0xD8 * delta + 0x28 * (1f - delta)) / 255f;
        float b = (0xE8 * delta + 0x38 * (1f - delta)) / 255f;

        event.setRed(r);
        event.setGreen(g);
        event.setBlue(b);
    }
}