package com.chinaex123.winter_falls;

import com.chinaex123.winter_falls.config.WFConfig;
import com.chinaex123.winter_falls.init.ModChunkGenerators;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Winterfalls.MOD_ID)
public class Winterfalls {
    public static final String MOD_ID = "winter_falls";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Winterfalls(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, WFConfig.SPEC);
        ModChunkGenerators.register(modEventBus);
    }
}