package com.chinaex123.winter_falls.util;

import com.chinaex123.winter_falls.worldgen.WinterFallsChunkGenerator;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * 冬季世界判定工具。
 * <p>
 * 用于判断给定世界是否属于冬季世界：
 * 服务端通过区块生成器或存档数据判定并缓存结果，
 * 客户端则使用最近一次服务端判定得到的缓存值。
 */
public class WinterFallsWorld {

    /** 冬季世界是否处于激活状态，服务端判定后同步缓存 */
    private static volatile boolean WINTER_WORLD_ACTIVE = false;

    /**
     * 判断指定世界是否为冬季世界。
     * <p>
     * 世界为空或非主世界时直接返回 false。
     * 服务端：若区块生成器为冬季世界区块生成器则判定为冬季世界并缓存结果，
     * 否则回退到存档数据中的启用标记。
     * 客户端：直接返回缓存值。
     *
     * @param level 待判断的世界
     * @return 是冬季世界返回 true
     */
    public static boolean isWinterFallsWorld(Level level) {
        if (level == null) return false;
        if (level.dimension() != Level.OVERWORLD) return false;

        if (level instanceof ServerLevel serverLevel) {
            var chunkSource = serverLevel.getChunkSource();
            if (chunkSource instanceof ServerChunkCache scc
                    && scc.getGenerator() instanceof WinterFallsChunkGenerator) {
                WINTER_WORLD_ACTIVE = true;
                return true;
            }
            return WorldSavedData.get(serverLevel).isWinterWorldEnabled();
        }

        return WINTER_WORLD_ACTIVE;
    }

    /**
     * 重置冬季世界缓存状态。
     * <p>
     * 通常在冬季世界卸载时调用，使后续判定重新走服务端逻辑。
     */
    public static void reset() {
        WINTER_WORLD_ACTIVE = false;
    }
}