package com.chinaex123.winter_falls.network;

import com.chinaex123.winter_falls.Winterfalls;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 模组网络管理类。
 * <p>
 * 负责注册本模组的网络数据包，
 * 目前包含服务端到客户端的冬季世界状态同步。
 * 通过事件订阅自动注册到游戏事件总线。
 */
@EventBusSubscriber(modid = Winterfalls.MOD_ID)
public class WFNetwork {

    /**
     * 注册网络数据包处理器。
     * <p>
     * 以协议版本 "1" 创建注册器，
     * 并注册服务端到客户端的冬季世界状态同步数据包。
     *
     * @param event 负载处理器注册事件
     */
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                SyncWinterWorldPacket.TYPE,
                SyncWinterWorldPacket.CODEC,
                SyncWinterWorldPacket::handle
        );
    }
}