package com.chinaex123.winter_falls.event;

import com.chinaex123.winter_falls.Winterfalls;
import com.chinaex123.winter_falls.network.SyncWinterWorldPacket;
import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 玩家登录 / 切换维度时，补发冬季世界状态给该玩家。
 */
@EventBusSubscriber(modid = Winterfalls.MOD_ID)
public class WinterWorldSyncHandler {

    /**
     * 玩家登录时同步一次。
     *
     * @param event 登录事件
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            syncTo(sp);
        }
    }

    /**
     * 玩家切换维度时同步一次。
     *
     * @param event 切换维度事件
     */
    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            syncTo(sp);
        }
    }

    /**
     * 给指定玩家发送当前世界的冬季世界状态。
     *
     * @param player 目标玩家
     */
    private static void syncTo(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        boolean active = WinterFallsWorld.isWinterFallsWorld(level);
        PacketDistributor.sendToPlayer(player, new SyncWinterWorldPacket(active));
    }
}