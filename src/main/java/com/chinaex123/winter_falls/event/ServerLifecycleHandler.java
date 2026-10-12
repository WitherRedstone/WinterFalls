package com.chinaex123.winter_falls.event;

import com.chinaex123.winter_falls.Winterfalls;
import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * 服务端生命周期事件监听。
 * <p>
 * 在服务器停止时重置冬季世界判定缓存，
 * 避免下一个存档沿用上一个存档的状态。
 */
@EventBusSubscriber(modid = Winterfalls.MOD_ID)
public class ServerLifecycleHandler {

    /**
     * 服务器停止时重置冬季世界缓存。
     *
     * @param event 服务器停止事件
     */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        WinterFallsWorld.reset();
    }
}