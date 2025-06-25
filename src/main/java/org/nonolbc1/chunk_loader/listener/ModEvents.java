package org.nonolbc1.chunk_loader.listener;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.nonolbc1.chunk_loader.manager.ChunkLoaderManager;


public class ModEvents {
    @SubscribeEvent
    public static void onServerTick(LevelTickEvent.Post event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
            ChunkLoaderManager.tick(serverLevel);
        }
    }
}
