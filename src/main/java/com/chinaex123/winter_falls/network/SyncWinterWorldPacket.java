package com.chinaex123.winter_falls.network;

import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 冬季世界状态同步数据包（服务端→客户端）。
 * <p>
 * 服务端在冬季世界激活状态发生变化时发送此数据包，
 * 客户端收到后更新本地的冬季世界标记，供客户端侧判定与渲染使用。
 *
 * @param active 冬季世界是否处于激活状态
 */
public record SyncWinterWorldPacket(boolean active) implements CustomPacketPayload {

    /** 网络包类型标识 */
    public static final CustomPacketPayload.Type<SyncWinterWorldPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath("winter_falls", "sync_winter_world")
            );

    /** 网络包编解码器，仅携带激活状态字段 */
    public static final StreamCodec<FriendlyByteBuf, SyncWinterWorldPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncWinterWorldPacket::active,
                    SyncWinterWorldPacket::new
            );

    /**
     * 获取网络包类型。
     *
     * @return 网络包类型标识
     */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 处理冬季世界状态同步。
     * <p>
     * 在主线程中将收到的激活状态写入客户端的冬季世界标记。
     *
     * @param packet 状态同步数据包
     * @param ctx    上下文
     */
    public static void handle(SyncWinterWorldPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> WinterFallsWorld.setClientActive(packet.active()));
    }
}