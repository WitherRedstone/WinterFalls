package com.chinaex123.winter_falls.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/**
 * 冬季世界区块生成器。
 * <p>
 * 继承自噪声区块生成器，作为冬季世界的判定依据：
 * 冬季世界工具类通过识别此生成器来确定当前世界是否为冬季世界。
 * 编码器仅定义生物群系源与噪声生成设置两个字段。
 */
public class WinterFallsChunkGenerator extends NoiseBasedChunkGenerator {

    /** 区块生成器编解码器，定义生物群系源与噪声生成设置两个字段 */
    public static final MapCodec<WinterFallsChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(NoiseBasedChunkGenerator::generatorSettings)
            ).apply(instance, WinterFallsChunkGenerator::new)
    );

    /**
     * 构造冬季世界区块生成器。
     *
     * @param biomeSource 生物群系源
     * @param settings    噪声生成设置
     */
    public WinterFallsChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
    }
}