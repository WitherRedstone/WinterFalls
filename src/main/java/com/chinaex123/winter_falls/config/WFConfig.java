package com.chinaex123.winter_falls.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class WFConfig {
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.BooleanValue DISABLE_WEATHER_CYCLE;
    public static ModConfigSpec.BooleanValue OVERRIDE_PRECIPITATION;
    public static ModConfigSpec.IntValue MAX_SNOW_LAYERS;
    public static ModConfigSpec.IntValue SNOW_CHECK_INTERVAL;
    public static ModConfigSpec.DoubleValue SNOW_ACCUMULATION_CHANCE;

    // 生物生成配置
    public static ModConfigSpec.DoubleValue SNOW_GOLEM_SPAWN_CHANCE;
    public static ModConfigSpec.IntValue SNOW_GOLEM_MIN_COUNT;
    public static ModConfigSpec.IntValue SNOW_GOLEM_MAX_COUNT;
    public static ModConfigSpec.DoubleValue STRAY_SPAWN_CHANCE;
    public static ModConfigSpec.IntValue STRAY_MIN_COUNT;
    public static ModConfigSpec.IntValue STRAY_MAX_COUNT;
    public static ModConfigSpec.DoubleValue SKELETON_TO_STRAY_CHANCE;
    public static ModConfigSpec.DoubleValue POLAR_BEAR_SPAWN_CHANCE;
    public static ModConfigSpec.IntValue POLAR_BEAR_MIN_COUNT;
    public static ModConfigSpec.IntValue POLAR_BEAR_MAX_COUNT;
    public static ModConfigSpec.DoubleValue FOX_SPAWN_CHANCE;
    public static ModConfigSpec.IntValue FOX_MIN_COUNT;
    public static ModConfigSpec.IntValue FOX_MAX_COUNT;

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

        builder.comment("生物生成配置").push("Mob Spawn Config");

        builder.comment("雪傀儡").push("Snow Golem");
        SNOW_GOLEM_SPAWN_CHANCE = builder
                .comment("雪傀儡自然生成概率（%）")
                .comment("Snow Golem natural spawn probability (%)")
                .defineInRange("snowGolemSpawnChance", 0.02, 0.0, 1.0);
        SNOW_GOLEM_MIN_COUNT = builder
                .comment("雪傀儡每次最少生成数量")
                .comment("Minimum Snow Golems to spawn at once")
                .defineInRange("snowGolemMinCount", 0, 0, Integer.MAX_VALUE);
        SNOW_GOLEM_MAX_COUNT = builder
                .comment("雪傀儡每次最多生成数量")
                .comment("Maximum Snow Golems to spawn at once")
                .defineInRange("snowGolemMaxCount", 1, 0, Integer.MAX_VALUE);
        STRAY_SPAWN_CHANCE = builder
                .comment("流浪者自然生成概率（%）")
                .comment("Stray natural spawn probability (%)")
                .defineInRange("straySpawnChance", 0.40, 0.0, 1.0);
        builder.pop();

        builder.comment("流浪者").push("Stray");
        STRAY_MIN_COUNT = builder
                .comment("流浪者每次最少生成数量")
                .comment("Minimum Strays to spawn at once")
                .defineInRange("strayMinCount", 0, 0, Integer.MAX_VALUE);
        STRAY_MAX_COUNT = builder
                .comment("流浪者每次最多生成数量")
                .comment("Maximum Strays to spawn at once")
                .defineInRange("strayMaxCount", 3, 0, Integer.MAX_VALUE);
        SKELETON_TO_STRAY_CHANCE = builder
                .comment("骷髅生成时替换为流浪者的概率（%）")
                .comment("Chance for Skeleton to be replaced by Stray when spawning (%)")
                .defineInRange("skeletonToStrayChance", 0.66, 0.0, 1.0);
        builder.pop();

        builder.comment("北极熊").push("Polar Bear");
        POLAR_BEAR_SPAWN_CHANCE = builder
                .comment("北极熊自然生成概率（%）")
                .comment("Polar Bear natural spawn probability (%)")
                .defineInRange("polarBearSpawnChance", 0.08, 0.0, 1.0);
        POLAR_BEAR_MIN_COUNT = builder
                .comment("北极熊每次最少生成数量")
                .comment("Minimum Polar Bears to spawn at once")
                .defineInRange("polarBearMinCount", 0, 0, Integer.MAX_VALUE);
        POLAR_BEAR_MAX_COUNT = builder
                .comment("北极熊每次最多生成数量")
                .comment("Maximum Polar Bears to spawn at once")
                .defineInRange("polarBearMaxCount", 4, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("狐狸").push("Fox");
        FOX_SPAWN_CHANCE = builder
                .comment("狐狸自然生成概率（%）")
                .comment("Fox natural spawn probability (%)")
                .defineInRange("foxSpawnChance", 0.10, 0.0, 1.0);
        FOX_MIN_COUNT = builder
                .comment("狐狸每次最少生成数量（原版 2-4 只一群）")
                .comment("Minimum Foxes to spawn at once")
                .defineInRange("foxMinCount", 2, 0, Integer.MAX_VALUE);
        FOX_MAX_COUNT = builder
                .comment("狐狸每次最多生成数量")
                .comment("Maximum Foxes to spawn at once")
                .defineInRange("foxMaxCount", 4, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.pop();

        SPEC = builder.build();
    }
}