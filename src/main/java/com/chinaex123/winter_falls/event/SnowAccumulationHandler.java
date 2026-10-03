package com.chinaex123.winter_falls.event;

import com.chinaex123.winter_falls.config.WFConfig;
import com.chinaex123.winter_falls.util.WinterFallsWorld;
import com.chinaex123.winter_falls.util.WorldSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * 积雪累积处理器。
 * <p>
 * 在冬季世界中负责为区块铺设额外积雪：
 * 首次加载区块时进行一次性全量覆盖，并在后续刻中按间隔与概率
 * 随机选取位置尝试结冰或增加雪层厚度。
 */
public class SnowAccumulationHandler {

    /** 缓存的雪层上限，-1 表示尚未从配置读取 */
    private static int cachedMaxSnowLayers = -1;
    /** 缓存的检查间隔（刻），-1 表示尚未从配置读取 */
    private static int cachedCheckInterval = -1;
    /** 缓存的累积概率倒数，-1 表示尚未从配置读取 */
    private static int cachedAccumulationChance = -1;

    /**
     * 为区块铺设额外积雪。
     * <p>
     * 仅在冬季世界生效：首次处理该区块时执行全量积雪并标记为已处理，
     * 之后按配置的检查间隔与概率，随机选取一个位置尝试结冰或增加雪层。
     * 仅在降雨或雷暴天气、可露天且光照满足条件时才会实际放置。
     *
     * @param level       服务端世界
     * @param chunk       目标区块
     * @param tickCounter 当前刻计数
     */
    public static void placeExtraSnow(ServerLevel level, ChunkAccess chunk, int tickCounter) {
        if (!WinterFallsWorld.isWinterFallsWorld(level)) return;

        long key = chunk.getPos().toLong();
        WorldSavedData savedData = WorldSavedData.get(level);

        // 持久化到存档防止重进世界重复铺雪（已有雪不动，幂等）
        if (!savedData.isChunkProcessed(key)) {
            accumulateSnowOnChunk(level, chunk);
            savedData.markChunkProcessed(key);
        }

        if (cachedCheckInterval == -1) {
            cachedCheckInterval = WFConfig.SNOW_CHECK_INTERVAL.get();
            cachedAccumulationChance = WFConfig.SNOW_ACCUMULATION_CHANCE.get();
            cachedMaxSnowLayers = WFConfig.MAX_SNOW_LAYERS.get();
        }

        // 频率控制：每 INTERVAL tick 才检查一次
        if (tickCounter % cachedCheckInterval != 0) return;
        // 概率控制：每次 1/CHANCE 才真的执行
        if (level.random.nextInt(cachedAccumulationChance) != 0) return;

        // 只有在下雨/雷暴时才冻冰+放雪
        if (!level.isRaining() && !level.isThundering()) return;

        // 随机选一个位置尝试放雪
        int blockX = chunk.getPos().getMinBlockX() + level.random.nextInt(16);
        int blockZ = chunk.getPos().getMinBlockZ() + level.random.nextInt(16);
        BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(blockX, 0, blockZ));

        if (pos.getY() <= level.getMinBuildHeight()) return;
        if (!level.canSeeSky(pos)) return;

        BlockState state = level.getBlockState(pos);
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);

        // 水面或水面下方结冰
        if (state.getBlock() == Blocks.WATER) {
            level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
            return;
        }
        if (belowState.getBlock() == Blocks.WATER) {
            level.setBlockAndUpdate(belowPos, Blocks.ICE.defaultBlockState());
            return;
        }

        boolean alreadySnow = state.getBlock() == Blocks.SNOW;
        if (!alreadySnow) {
            if (!belowState.isSolid() || belowState.is(BlockTags.ICE)) return;
            if (!state.canBeReplaced()) return;
        }
        if (level.getBrightness(LightLayer.BLOCK, pos) >= 10) return;

        // 已有雪则增加层数，否则放置一层雪
        if (alreadySnow) {
            int currentLayers = state.getValue(BlockStateProperties.LAYERS);
            if (currentLayers < cachedMaxSnowLayers) {
                level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LAYERS, currentLayers + 1));
            }
        } else {
            level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState());
        }
    }

    /**
     * 对区块执行一次性全量积雪覆盖。
     * <p>
     * 仅在冬季世界生效：遍历区块内全部 16×16 列，对可露天且符合条件的位置
     * 结冰或放置一层雪，不增加已有雪层厚度。主要用于区块首次加载时的保底覆盖。
     *
     * @param level 服务端世界
     * @param chunk 目标区块
     */
    public static void accumulateSnowOnChunk(ServerLevel level, ChunkAccess chunk) {
        if (!WinterFallsWorld.isWinterFallsWorld(level)) return;

        if (cachedMaxSnowLayers == -1) {
            cachedCheckInterval = WFConfig.SNOW_CHECK_INTERVAL.get();
            cachedAccumulationChance = WFConfig.SNOW_ACCUMULATION_CHANCE.get();
            cachedMaxSnowLayers = WFConfig.MAX_SNOW_LAYERS.get();
        }

        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int blockX = minX + lx;
                int blockZ = minZ + lz;
                BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(blockX, 0, blockZ));

                if (pos.getY() <= level.getMinBuildHeight()) continue;
                if (!level.canSeeSky(pos)) continue;

                BlockState state = level.getBlockState(pos);
                BlockPos belowPos = pos.below();
                BlockState belowState = level.getBlockState(belowPos);

                // 水面或水面下方结冰
                if (state.getBlock() == Blocks.WATER) {
                    level.setBlock(pos, Blocks.ICE.defaultBlockState(), 2);
                    continue;
                }
                if (belowState.getBlock() == Blocks.WATER) {
                    level.setBlock(belowPos, Blocks.ICE.defaultBlockState(), 2);
                    continue;
                }

                boolean alreadySnow = state.getBlock() == Blocks.SNOW;
                if (!alreadySnow) {
                    if (!belowState.isSolid() || belowState.is(BlockTags.ICE)) continue;
                    if (!state.canBeReplaced()) continue;
                }
                if (level.getBrightness(LightLayer.BLOCK, pos) >= 10) continue;

                // 首次全量积雪：只做保底覆盖
                if (!alreadySnow) {
                    level.setBlock(pos, Blocks.SNOW.defaultBlockState(), 2);
                }
            }
        }
    }
}