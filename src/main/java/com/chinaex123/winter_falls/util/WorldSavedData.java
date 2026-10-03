package com.chinaex123.winter_falls.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 冬季世界存档数据。
 * <p>
 * 持久化记录冬季世界的初始化标记、启用状态，
 * 以及已完成首次铺雪的区块键集合，避免重进世界时重复铺雪。
 */
public class WorldSavedData extends SavedData {

    /** 存档数据注册名称 */
    private static final String DATA_NAME = "winter_falls_data";
    /** 是否已完成世界初始化 */
    private boolean hasInitialized = false;

    /** 冬季世界是否启用 */
    private boolean winterWorldEnabled = false;

    /** 已完成首次铺雪的区块键集合 */
    private final Set<Long> snowProcessedChunks = ConcurrentHashMap.newKeySet();

    /**
     * 构造空的冬季世界存档数据。
     */
    public WorldSavedData() {
        super();
    }

    /**
     * 从 NBT 标签构造冬季世界存档数据。
     * <p>
     * 读取初始化标记、启用状态与已处理区块键数组。
     *
     * @param tag        包含持久化数据的 NBT 标签
     * @param registries 注册表提供者
     */
    public WorldSavedData(CompoundTag tag, HolderLookup.Provider registries) {
        super();
        this.hasInitialized = tag.getBoolean("hasInitialized");
        this.winterWorldEnabled = tag.getBoolean("winterWorldEnabled");
        long[] chunks = tag.getLongArray("snowProcessedChunks");
        for (long c : chunks) {
            this.snowProcessedChunks.add(c);
        }
    }

    /**
     * 是否已完成世界初始化。
     *
     * @return 已初始化返回 true
     */
    public boolean hasInitialized() {
        return hasInitialized;
    }

    /**
     * 设置世界初始化标记，并标记为脏数据。
     *
     * @param initialized 是否已初始化
     */
    public void setInitialized(boolean initialized) {
        this.hasInitialized = initialized;
        setDirty();
    }

    /**
     * 判断指定区块是否已完成首次铺雪。
     *
     * @param chunkKey 区块键
     * @return 已处理返回 true
     */
    public boolean isChunkProcessed(long chunkKey) {
        return snowProcessedChunks.contains(chunkKey);
    }

    /**
     * 标记指定区块已完成首次铺雪。
     * <p>
     * 仅当集合中原本不含该键时才标记为脏数据。
     *
     * @param chunkKey 区块键
     */
    public void markChunkProcessed(long chunkKey) {
        if (snowProcessedChunks.add(chunkKey)) {
            setDirty();
        }
    }

    /**
     * 冬季世界是否启用。
     *
     * @return 已启用返回 true
     */
    public boolean isWinterWorldEnabled() {
        return winterWorldEnabled;
    }

    /**
     * 设置冬季世界启用状态。
     * <p>
     * 仅当状态发生变化时才标记为脏数据。
     *
     * @param enabled 是否启用
     */
    public void setWinterWorldEnabled(boolean enabled) {
        if (this.winterWorldEnabled != enabled) {
            this.winterWorldEnabled = enabled;
            setDirty();
        }
    }

    /**
     * 将数据保存到 NBT 标签。
     * <p>
     * 写入初始化标记、启用状态以及已处理区块键数组。
     *
     * @param tag        目标 NBT 标签
     * @param registries 注册表提供者
     * @return 包含所有持久化数据的 NBT 标签
     */
    @Override
    public @NotNull CompoundTag save(CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        tag.putBoolean("hasInitialized", hasInitialized);
        tag.putBoolean("winterWorldEnabled", winterWorldEnabled);
        long[] arr = new long[snowProcessedChunks.size()];
        int i = 0;
        for (long c : snowProcessedChunks) arr[i++] = c;
        tag.putLongArray("snowProcessedChunks", arr);
        return tag;
    }

    /**
     * 获取指定服务端世界对应的冬季世界存档数据。
     * <p>
     * 若尚未创建则通过维度数据存储自动创建。
     *
     * @param level 服务端世界
     * @return 冬季世界存档数据实例
     */
    public static WorldSavedData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(
                new SavedData.Factory<>(WorldSavedData::new, WorldSavedData::new),
                DATA_NAME
        );
    }

    /**
     * 仅在新建世界时执行指定操作。
     * <p>
     * 仅对服务端主世界生效：若存档数据尚未初始化，则执行给定操作并标记为已初始化，
     * 避免同一存档重复执行。
     *
     * @param level  服务端世界
     * @param action 待执行的操作
     */
    public static void onlyForNewWorlds(ServerLevel level, Runnable action) {
        if (!level.isClientSide() && level.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
            WorldSavedData data = get(level);
            if (!data.hasInitialized()) {
                action.run();
                data.setInitialized(true);
            }
        }
    }
}