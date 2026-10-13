package com.chinaex123.winter_falls.event;

import com.chinaex123.winter_falls.config.WFConfig;
import com.chinaex123.winter_falls.util.WinterFallsWorld;
import com.chinaex123.winter_falls.util.WorldSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * 冬季世界事件处理器。
 * <p>
 * 监听世界加载、刻更新与卸载事件，
 * 为服务端的冬季世界维持永久雪天天气，并在卸载时重置状态。
 */
@EventBusSubscriber
public class WFEvents {

    /**
     * 处理世界加载事件。
     * <p>
     * 仅对服务端的冬季世界生效，且仅在新建世界时执行，
     * 初始化永久雪天天气。
     *
     * @param event 世界加载事件
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && WinterFallsWorld.isWinterFallsWorld(level)) {
            WorldSavedData.onlyForNewWorlds(level, () -> setEternalSnowWeather(level));
        }
    }

    /**
     * 处理世界刻事件。
     * <p>
     * 仅对服务端的冬季世界生效，每隔 100 刻检查一次天气参数，
     * 若当前天气参数异常（duration ≤ 0 或 raining 被意外关闭），
     * 则重新设置永久雪天天气。
     *
     * @param event 世界刻事件
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!WinterFallsWorld.isWinterFallsWorld(level)) return;

        if (level.getGameTime() % 100 == 0) {
            if (!level.getLevelData().isRaining()) {
                setEternalSnowWeather(level);
            }
        }
    }

    /**
     * 处理世界卸载事件。
     * <p>
     * 仅对服务端的冬季世界生效，重置冬季世界状态标记与天气设置标记。
     *
     * @param event 世界卸载事件
     */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level && WinterFallsWorld.isWinterFallsWorld(level)) {
            WinterFallsWorld.reset();
        }
    }

    /**
     * 设置永久雪天天气。
     * <p>
     * 将天气参数固定为降雨（表现为雪）、无雷暴且持续时间为最大值，
     * 并在配置开启时关闭原版的天气循环规则。
     *
     * @param level 服务端世界
     */
    private static void setEternalSnowWeather(ServerLevel level) {
        level.setWeatherParameters(Integer.MAX_VALUE, 0, true, false);
        if (WFConfig.DISABLE_WEATHER_CYCLE.get()) {
            level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, level.getServer());
        }
    }
}