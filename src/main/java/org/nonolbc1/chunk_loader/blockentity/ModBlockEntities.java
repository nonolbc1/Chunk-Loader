package org.nonolbc1.chunk_loader.blockentity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.nonolbc1.chunk_loader.ChunkLoader;
import org.nonolbc1.chunk_loader.block.ModBlocks;

import java.util.List;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ChunkLoader.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChunkLoaderBlockEntity>> CHUNK_LOADER_BE =
            BLOCK_ENTITIES.register(
                    "chunk_loader_be",
                    () -> BlockEntityType.Builder.of(ChunkLoaderBlockEntity::new, ModBlocks.CHUNK_LOADER_BLOCK.get()).build(null)
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

    public static List<String> getRegisteredNames() {
        return List.of(CHUNK_LOADER_BE.getId().toString());
    }
}
