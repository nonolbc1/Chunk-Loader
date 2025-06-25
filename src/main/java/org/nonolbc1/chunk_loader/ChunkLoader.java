package org.nonolbc1.chunk_loader;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.nonolbc1.chunk_loader.block.ModBlocks;
import org.nonolbc1.chunk_loader.blockentity.ModBlockEntities;
import org.nonolbc1.chunk_loader.creativetab.ModCreativeTabs;
import org.nonolbc1.chunk_loader.item.ModItems;
import org.nonolbc1.chunk_loader.listener.MobSpawnControlEvent;
import org.nonolbc1.chunk_loader.listener.ModEvents;
import org.nonolbc1.chunk_loader.menu.ModMenus;
import org.nonolbc1.chunk_loader.screen.ChunkLoaderScreen;
import org.slf4j.Logger;

@Mod(ChunkLoader.MODID)
public class ChunkLoader {
    public static final String MODID = "chunk_loader";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ChunkLoader(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing ChunkLoader mod (ID: {})", MODID);
        modEventBus.addListener(this::commonSetup);

        ModBlocks.register(modEventBus);
        LOGGER.info("Registered blocks: {}", ModBlocks.getRegisteredNames());

        ModBlockEntities.register(modEventBus);
        LOGGER.info("Registered block entities: {}", ModBlockEntities.getRegisteredNames());

        ModItems.register(modEventBus);
        LOGGER.info("Registered items: {}", ModItems.getRegisteredNames());

        ModCreativeTabs.register(modEventBus);
        LOGGER.info("Registered creative tabs: {}", ModCreativeTabs.getRegisteredNames());

        ModMenus.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(ModEvents.class);
        NeoForge.EVENT_BUS.register(MobSpawnControlEvent.class);

        modEventBus.addListener(ClientModEvents::onClientSetup);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        LOGGER.info("Registration of blocks, items, entities, menus, and events complete.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }

        @SubscribeEvent
        public static void onRegisterScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenus.CHUNK_LOADER_MENU.get(), ChunkLoaderScreen::new);
            LOGGER.info("ChunkLoaderScreen registered.");
        }
    }
}
