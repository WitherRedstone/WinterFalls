package com.chinaex123.winter_falls.util;

import com.chinaex123.winter_falls.network.SyncWinterWorldPacket;
import com.chinaex123.winter_falls.worldgen.WinterFallsChunkGenerator;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 冬季世界判定工具。
 * <p>
 * 服务端通过区块生成器或存档数据判定，并把结果同步给客户端；
 * 客户端使用最近一次服务端同步得到的缓存值。
 */
public class WinterFallsWorld {

    /** 冬季世界是否处于激活状态，服务端判定后同步缓存 */
    private static volatile boolean WINTER_WORLD_ACTIVE = false;

    /**
     * 判断指定世界是否为冬季世界。
     *
     * @param level 待判断的世界
     * @return 是冬季世界返回 true
     */
    public static boolean isWinterFallsWorld(Level level) {
        if (level == null) return false;
        if (level.dimension() != Level.OVERWORLD) return false;

        if (level instanceof ServerLevel serverLevel) {
            return isWinterFallsWorldServer(serverLevel);
        }

        return WINTER_WORLD_ACTIVE;
    }

    /**
     * 服务端判定逻辑。
     * <p>
     * 优先看区块生成器，其次回退到存档数据。
     * 若判定为冬季世界，则写入存档并同步给客户端。
     *
     * @param serverLevel 服务端世界
     * @return 是冬季世界返回 true
     */
    private static boolean isWinterFallsWorldServer(ServerLevel serverLevel) {
        boolean isWinter;

        var chunkSource = serverLevel.getChunkSource();
        if (chunkSource instanceof ServerChunkCache scc
                && scc.getGenerator() instanceof WinterFallsChunkGenerator) {
            isWinter = true;
        } else {
            isWinter = WorldSavedData.get(serverLevel).isWinterWorldEnabled();
        }

        if (isWinter) {
            WINTER_WORLD_ACTIVE = true;

            // 持久化冬季世界身份
            WorldSavedData data = WorldSavedData.get(serverLevel);
            if (!data.isWinterWorldEnabled()) {
                data.setWinterWorldEnabled(true);
            }

            // 同步给该维度所有客户端
            PacketDistributor.sendToPlayersInDimension(
                    serverLevel,
                    new SyncWinterWorldPacket(true)
            );
        }

        return isWinter;
    }

    /**
     * 客户端处理同步包时调用，设置本地缓存。
     *
     * @param active 是否为冬季世界
     */
    public static void setClientActive(boolean active) {
        WINTER_WORLD_ACTIVE = active;
    }

    /**
     * 重置冬季世界缓存状态。
     */
    public static void reset() {
        WINTER_WORLD_ACTIVE = false;
    }
}