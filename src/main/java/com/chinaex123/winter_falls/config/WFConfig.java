package com.chinaex123.winter_falls.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class WFConfig {
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.BooleanValue DISABLE_WEATHER_CYCLE;
    public static ModConfigSpec.BooleanValue OVERRIDE_PRECIPITATION;
    public static ModConfigSpec.IntValue MAX_SNOW_LAYERS;
    public static ModConfigSpec.IntValue SNOW_CHECK_INTERVAL;
    public static ModConfigSpec.DoubleValue SNOW_ACCUMULATION_CHANCE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("通用配置").push("Common Config");
        DISABLE_WEATHER_CYCLE = builder
                .comment("是否禁用天气循环")
                .comment("Whether to disable the weather cycle")
                .define("disableWeatherCycle", true);
        OVERRIDE_PRECIPITATION = builder
                .comment("是否覆盖所有生物群系的降水类型")
                .comment("Whether it covers the precipitation types of all biomes")
                .define("overridePrecipitation", true);
        MAX_SNOW_LAYERS = builder
                .comment("最大积雪的层数")
                .comment("Maximum number of snow accumulation layers")
                .defineInRange("maxSnowLayers", 8, 1, Integer.MAX_VALUE);
        SNOW_CHECK_INTERVAL = builder
                .comment("积雪检查间隔（tick）")
                .comment("Snow accumulation check interval (tick)")
                .defineInRange("snowCheckInterval", 20, 1, Integer.MAX_VALUE);
        SNOW_ACCUMULATION_CHANCE = builder
                .comment("积雪概率（%）")
                .comment("Probability of snow accumulation (%)")
                .defineInRange("snowAccumulationChance", 0.0625, 0.0, 1.0);

        builder.pop();

        SPEC = builder.build();
    }
}
