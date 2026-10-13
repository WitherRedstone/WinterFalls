package com.chinaex123.winter_falls.event;

import com.chinaex123.winter_falls.config.WFConfig;
import com.chinaex123.winter_falls.util.WinterFallsWorld;
import com.chinaex123.winter_falls.util.WorldSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * 积雪累积处理器。
 * <p>
 * 负责冬季世界中的自定义积雪逻辑：
 * 新区块首次处理时铺设积雪与结冰，之后按配置的间隔与概率
 * 逐步增加已有积雪的层数，并缓存配置数值避免重复读取。
 */
public class SnowAccumulationHandler {

    /** 缓存的最大积雪层数 */
    private static int cachedMaxSnowLayers = -1;

    /** 缓存的检查间隔（刻） */
    private static int cachedCheckInterval = -1;

    /** 缓存的积雪累积概率 */
    private static double cachedAccumulationChance = -1;

    /**
     * 按需刷新配置缓存。
     * <p>
     * 仅在缓存未初始化时读取一次配置，
     * 避免每次调用都重复读取配置值。
     */
    private static void refreshConfigIfNeeded() {
        if (cachedCheckInterval == -1) {
            cachedCheckInterval = WFConfig.SNOW_CHECK_INTERVAL.get();
            cachedAccumulationChance = WFConfig.SNOW_ACCUMULATION_CHANCE.get();
            cachedMaxSnowLayers = WFConfig.MAX_SNOW_LAYERS.get();
        }
    }

    /**
     * 重置配置缓存。
     * <p>
     * 将缓存值全部恢复为未初始化状态，下次使用时重新读取配置。
     */
    public static void reset() {
        cachedMaxSnowLayers = -1;
        cachedCheckInterval = -1;
        cachedAccumulationChance = -1;
    }

    /**
     * 判断方块是否属于冰类方块。
     *
     * @param state 方块状态
     * @return 若为冰、浮冰、蓝冰或霜冰则返回 true
     */
    private static boolean isIceClass(BlockState state) {
        return state.getBlock() == Blocks.ICE
                || state.getBlock() == Blocks.PACKED_ICE
                || state.getBlock() == Blocks.BLUE_ICE
                || state.getBlock() == Blocks.FROSTED_ICE;
    }

    /**
     * 为区块放置额外积雪。
     * <p>
     * 仅对冬季世界生效。区块首次处理时铺设积雪与结冰并标记为已处理，
     * 之后按配置的检查间隔与累积概率，随机选取一列尝试增加积雪层数。
     *
     * @param level       服务端世界
     * @param chunk       目标区块
     * @param tickCounter 全局 tick 计数
     */
    public static void placeExtraSnow(ServerLevel level, ChunkAccess chunk, int tickCounter) {
        if (!WinterFallsWorld.isWinterFallsWorld(level)) return;

        long key = chunk.getPos().toLong();
        WorldSavedData savedData = WorldSavedData.get(level);

        if (!savedData.isChunkProcessed(key)) {
            placeSnowAndFreeze(level, chunk);
            savedData.markChunkProcessed(key);
        }

        refreshConfigIfNeeded();

        if (tickCounter % cachedCheckInterval != 0) return;
        if (level.random.nextDouble() >= cachedAccumulationChance) return;

        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int blockX = minX + level.random.nextInt(16);
        int blockZ = minZ + level.random.nextInt(16);
        BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                new BlockPos(blockX, 0, blockZ));

        incrementalTickAt(level, pos);
    }

    /**
     * 为区块铺设积雪与结冰。
     * <p>
     * 遍历区块内全部 16×16 列，按高度图定位地表方块：
     * 水面结冰、水下方块结冰，其余可替换且下方为固体、
     * 方块光照低于 10 的位置铺设一层雪。
     *
     * @param level 服务端世界
     * @param chunk 目标区块
     */
    private static void placeSnowAndFreeze(ServerLevel level, ChunkAccess chunk) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int blockX = minX + lx;
                int blockZ = minZ + lz;
                BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                        new BlockPos(blockX, 0, blockZ));

                BlockState state = level.getBlockState(pos);
                BlockPos belowPos = pos.below();
                BlockState below = level.getBlockState(belowPos);

                if (state.getBlock() == Blocks.WATER) {
                    level.setBlock(pos, Blocks.ICE.defaultBlockState(), 2);
                    continue;
                }

                if (below.getBlock() == Blocks.WATER) {
                    level.setBlock(belowPos, Blocks.ICE.defaultBlockState(), 2);
                    continue;
                }

                if (isIceClass(below)) continue;

                if (isIceClass(state)) continue;

                if (state.canBeReplaced() && below.isSolid()
                        && level.getBrightness(LightLayer.BLOCK, pos) < 10) {
                    level.setBlock(pos, Blocks.SNOW.defaultBlockState(), 2);
                }
            }
        }
    }

    /**
     * 对单个位置执行增量积雪处理。
     * <p>
     * 若目标为雪且其下方为冰类方块，则移除该雪；
     * 若目标为雪且层数未达上限、方块光照低于 10，
     * 则将该处雪的层数增加一层。
     *
     * @param level 服务端世界
     * @param pos   目标位置
     */
    private static void incrementalTickAt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() == Blocks.SNOW && isIceClass(level.getBlockState(pos.below()))) {
            level.removeBlock(pos, false);
            return;
        }

        if (state.getBlock() == Blocks.SNOW) {
            int layers = state.getValue(BlockStateProperties.LAYERS);
            if (layers < cachedMaxSnowLayers
                    && level.getBrightness(LightLayer.BLOCK, pos) < 10) {
                level.setBlock(pos,
                        state.setValue(BlockStateProperties.LAYERS, layers + 1),
                        2);
            }
        }
    }
}