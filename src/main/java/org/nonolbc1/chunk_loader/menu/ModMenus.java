package org.nonolbc1.chunk_loader.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.nonolbc1.chunk_loader.ChunkLoader;

import java.util.List;


public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ChunkLoader.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ChunkLoaderMenu>> CHUNK_LOADER_MENU =
            MENUS.register(
                    "chunk_loader_menu",
                    () -> IMenuTypeExtension.create(ChunkLoaderMenu::new)
            );

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }

    public static List<String> getRegisteredNames() {
        return List.of(CHUNK_LOADER_MENU.getId().toString());
    }
}
