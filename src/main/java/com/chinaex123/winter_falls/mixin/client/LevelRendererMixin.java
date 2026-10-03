package com.chinaex123.winter_falls.mixin.client;

import com.chinaex123.winter_falls.util.WinterFallsWorld;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 世界渲染器 Mixin。
 * <p>
 * 在冬季世界中使用简化的雪花渲染替代原版的雨雪渲染：
 * 以相机为中心的固定范围内逐列采样地表高度，
 * 批量绘制朝向相机的雪花四边形，并取消原渲染逻辑。
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    /** 雪花纹理位置 */
    @Shadow
    @Final
    private static ResourceLocation SNOW_LOCATION;

    /** 当前客户端世界 */
    @Shadow
    private ClientLevel level;

    /** Minecraft 客户端实例 */
    @Shadow
    @Final
    private Minecraft minecraft;

    /** 累计刻数，用于雪花纹理滚动 */
    @Shadow
    private int ticks;

    /** 每列的 X 方向降水偏移表 */
    @Shadow
    @Final
    private float[] rainSizeX;

    /** 每列的 Z 方向降水偏移表 */
    @Shadow
    @Final
    private float[] rainSizeZ;

    /**
     * 在雨雪渲染头部注入，冬季世界时以简化雪花渲染替代原逻辑。
     * <p>
     * 以相机位置为中心、在固定范围内逐列遍历：跳过低于地表的高度区间，
     * 依据随机源与当前刻数计算雪花纹理的滚动位置，
     * 结合与相机的距离计算透明度与光照，批量绘制雪花四边形，
     * 最后恢复渲染状态并取消原方法执行。
     *
     * @param lightTexture 光照纹理
     * @param partialTick  部分 tick 插值
     * @param srcCamX      相机 X 坐标
     * @param srcCamY      相机 Y 坐标
     * @param srcCamZ      相机 Z 坐标
     * @param ci           回调信息，用于取消原方法执行
     */
    @Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true)
    private void renderFastSnow(LightTexture lightTexture, float partialTick, double srcCamX, double srcCamY, double srcCamZ, CallbackInfo ci) {
        if (!WinterFallsWorld.isWinterFallsWorld(this.minecraft.level)) {
            return;
        }

        final Tesselator tesselator = Tesselator.getInstance();
        final Level level = this.minecraft.level;

        final float camX = (float) srcCamX;
        final float camY = (float) srcCamY;
        final float camZ = (float) srcCamZ;

        final int blockX = Mth.floor(camX);
        final int blockY = Mth.floor(camY);
        final int blockZ = Mth.floor(camZ);

        final int particleAmount = 12;
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        assert level != null;

        BufferBuilder buffer = null;
        boolean renderedAny = false;

        lightTexture.turnOnLightLayer();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(Minecraft.useShaderTransparency());
        RenderSystem.setShader(GameRenderer::getParticleShader);

        for (int z = blockZ - particleAmount; z <= blockZ + particleAmount; z++) {
            for (int x = blockX - particleAmount; x <= blockX + particleAmount; x++) {
                final int rainSizeIndex = (z - blockZ + 16) * 32 + x - blockX + 16;
                final float snowDriftX = this.rainSizeX[rainSizeIndex] * 0.15F;
                final float snowDriftZ = this.rainSizeZ[rainSizeIndex] * 0.15F;
                final int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                int minY = blockY - particleAmount;
                int maxY = blockY + particleAmount;

                if (minY < y) {
                    minY = y;
                }

                if (maxY < y) {
                    maxY = y;
                }

                final int targetY = Math.max(y, blockY);

                if (minY != maxY) {
                    // 首次命中时初始化纹理与顶点缓冲
                    if (!renderedAny) {
                        renderedAny = true;
                        RenderSystem.setShaderTexture(0, SNOW_LOCATION);
                        buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                    }

                    // 依据列坐标生成确定性随机源，避免每帧抖动
                    final RandomSource random = RandomSource.create(x * x * 3121L + x * 45238971L ^ z * z * 418711L + z * 13761L);
                    final int tickOffset = this.ticks & 131071;
                    final int posOffset = x * x * 3121 + x * 45238971 + z * z * 418711 + z * 13761 & 0xFF;

                    final float textureV = -((float) (tickOffset + posOffset) + partialTick) / 64.0F * (1.5F + random.nextFloat()) % 32.0F;

                    final double offsetX = x + 0.5 - camX;
                    final double offsetZ = z + 0.5 - camZ;
                    // 依据与相机的水平距离计算透明度，远处逐渐淡出
                    final float offsetXZ = (float) Math.sqrt(offsetX * offsetX + offsetZ * offsetZ) / particleAmount;
                    final float alpha = (1.0F - offsetXZ * offsetXZ) * 0.5F + 0.5F;

                    cursor.set(x, targetY, z);
                    final int packedLight = LevelRenderer.getLightColor(level, cursor);

                    // 提升光照亮度，使雪花更明亮
                    final int lightU = packedLight & 0xffff;
                    final int lightV = (packedLight >> 16) & 0xffff;
                    final int brightLightU = (lightU * 3 + 240) / 4;
                    final int brightLightV = (lightV * 3 + 240) / 4;

                    final float snowSize = 0.25F;

                    // 按四个顶点绘制朝向相机的雪花四边形
                    buffer.addVertex(x - camX - snowDriftX + snowSize, maxY - camY, z - camZ - snowDriftZ + snowSize)
                            .setUv(0.0F, minY * 0.25F + textureV)
                            .setColor(1.0F, 1.0F, 1.0F, alpha)
                            .setUv2(brightLightU, brightLightV);
                    buffer.addVertex(x - camX + snowDriftX + snowSize, maxY - camY, z - camZ + snowDriftZ + snowSize)
                            .setUv(1.0F, minY * 0.25F + textureV)
                            .setColor(1.0F, 1.0F, 1.0F, alpha)
                            .setUv2(brightLightU, brightLightV);
                    buffer.addVertex(x - camX + snowDriftX + snowSize, minY - camY, z - camZ + snowDriftZ + snowSize)
                            .setUv(1.0F, maxY * 0.25F + textureV)
                            .setColor(1.0F, 1.0F, 1.0F, alpha)
                            .setUv2(brightLightU, brightLightV);
                    buffer.addVertex(x - camX - snowDriftX + snowSize, minY - camY, z - camZ - snowDriftZ + snowSize)
                            .setUv(0.0F, maxY * 0.25F + textureV)
                            .setColor(1.0F, 1.0F, 1.0F, alpha)
                            .setUv2(brightLightU, brightLightV);
                }
            }
        }

        // 有绘制内容时才提交缓冲
        if (renderedAny) {
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        lightTexture.turnOffLightLayer();
        ci.cancel();
    }
}