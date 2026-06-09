package org.nonolbc1.chunk_loader.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.nonolbc1.chunk_loader.ChunkLoader;
import org.nonolbc1.chunk_loader.block.ModBlocks;

import java.util.List;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ChunkLoader.MODID);
    public static final DeferredItem<BlockItem> CHUNK_LOADER_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("chunk_loader_block", ModBlocks.CHUNK_LOADER_BLOCK);
    public static final DeferredItem<Item> MOB_ATTRACTOR_ITEM =
            ITEMS.registerItem("mob_attractor", Item::new);

    public static final DeferredItem<Item> MOB_PROVOKER_ITEM =
            ITEMS.registerItem("mob_provoker", Item::new);

    public static final DeferredItem<Item> MOB_REPELLER_ITEM =
            ITEMS.registerItem("mob_repeller", Item::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static List<String> getRegisteredNames() {
        return List.of(
                CHUNK_LOADER_BLOCK_ITEM.getId().toString(),
                MOB_ATTRACTOR_ITEM.getId().toString(),
                MOB_PROVOKER_ITEM.getId().toString(),
                MOB_REPELLER_ITEM.getId().toString()
        );
    }
}
