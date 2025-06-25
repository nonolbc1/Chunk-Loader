package org.nonolbc1.chunk_loader.block;

import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.nonolbc1.chunk_loader.ChunkLoader;

import java.util.List;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ChunkLoader.MODID);
    public static final DeferredBlock<Block> CHUNK_LOADER_BLOCK = BLOCKS.registerBlock("chunk_loader_block", ChunkLoaderBlock::new);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    public static List<String> getRegisteredNames() {
        return List.of(CHUNK_LOADER_BLOCK.getId().toString());
    }
}
