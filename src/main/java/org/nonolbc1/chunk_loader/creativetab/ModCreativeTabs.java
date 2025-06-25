package org.nonolbc1.chunk_loader.creativetab;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.nonolbc1.chunk_loader.ChunkLoader;
import org.nonolbc1.chunk_loader.item.ModItems;

import java.util.List;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ChunkLoader.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CHUNK_LOADER_TAB = CREATIVE_MODE_TABS
            .register(
                    "chunk_loader_tab",
                    () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.chunk_loader"))
                            .withTabsBefore(CreativeModeTabs.COMBAT)
                            .icon(() -> ModItems.CHUNK_LOADER_BLOCK_ITEM.get().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                output.accept(ModItems.CHUNK_LOADER_BLOCK_ITEM.get());
                                output.accept(ModItems.MOB_ATTRACTOR_ITEM.get());
                                output.accept(ModItems.MOB_PROVOKER_ITEM.get());
                                output.accept(ModItems.MOB_REPELLER_ITEM.get());
                            }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

    public static List<String> getRegisteredNames() {
        return List.of(CHUNK_LOADER_TAB.getId().toString());
    }
}
