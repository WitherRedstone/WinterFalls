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
 * <p>
 * 判定结果会被缓存，避免在热路径中重复查询生成器与存档数据。
 */
public class WinterFallsWorld {

    /** 冬季世界是否处于激活状态，服务端判定后同步缓存 */
    private static volatile boolean WINTER_WORLD_ACTIVE = false;

    /** 是否已经向客户端同步过冬季世界状态，避免重复发包 */
    private static volatile boolean WINTER_WORLD_SYNCED = false;

    /** 服务端判定结果缓存，null 表示尚未判定 */
    private static volatile Boolean CACHED_IS_WINTER = null;

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
     * 判定结果会被缓存；判定为冬季世界时写入存档，
     * 并仅在首次判定时向该维度所有客户端发包同步。
     *
     * @param serverLevel 服务端世界
     * @return 是冬季世界返回 true
     */
    private static boolean isWinterFallsWorldServer(ServerLevel serverLevel) {
        Boolean cached = CACHED_IS_WINTER;
        if (cached != null) {
            return cached;
        }

        boolean isWinter;

        var chunkSource = serverLevel.getChunkSource();
        if (chunkSource instanceof ServerChunkCache scc
                && scc.getGenerator() instanceof WinterFallsChunkGenerator) {
            isWinter = true;
        } else {
            isWinter = WorldSavedData.get(serverLevel).isWinterWorldEnabled();
        }

        CACHED_IS_WINTER = isWinter;

        if (isWinter) {
            WINTER_WORLD_ACTIVE = true;

            // 只在首次判定时写入存档并同步客户端，避免重复发包
            if (!WINTER_WORLD_SYNCED) {
                WINTER_WORLD_SYNCED = true;

                WorldSavedData data = WorldSavedData.get(serverLevel);
                if (!data.isWinterWorldEnabled()) {
                    data.setWinterWorldEnabled(true);
                }

                PacketDistributor.sendToPlayersInDimension(
                        serverLevel,
                        new SyncWinterWorldPacket(true)
                );
            }
        }

        return isWinter;
    }

    /**
     * 冬季世界是否处于激活状态。
     * <p>
     * 供没有世界上下文的逻辑使用（如 {@code Biome} 的各类判断）。
     * 该读取几乎无开销，适合热路径调用。
     *
     * @return 激活返回 true
     */
    public static boolean isWinterWorldActive() {
        return WINTER_WORLD_ACTIVE;
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
     * <p>
     * 在世界卸载、玩家退出或切换存档时调用，
     * 使后续判定重新走服务端逻辑并重新同步客户端。
     */
    public static void reset() {
        WINTER_WORLD_ACTIVE = false;
        WINTER_WORLD_SYNCED = false;
        CACHED_IS_WINTER = null;
    }
}