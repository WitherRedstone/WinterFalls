package com.chinaex123.winter_falls.init;

import com.chinaex123.winter_falls.Winterfalls;
import com.chinaex123.winter_falls.worldgen.WinterFallsChunkGenerator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModChunkGenerators {

    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATOR_TYPES =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, Winterfalls.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<WinterFallsChunkGenerator>> WINTER_FALLS =
            CHUNK_GENERATOR_TYPES.register("winter_falls", () -> WinterFallsChunkGenerator.CODEC);

    private ModChunkGenerators() {}

    public static void register(IEventBus modEventBus) {
        CHUNK_GENERATOR_TYPES.register(modEventBus);
    }
}