package org.nonolbc1.chunk_loader.listener;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.nonolbc1.chunk_loader.manager.ChunkLoaderManager;
import org.nonolbc1.chunk_loader.type.MobUpgradeType;

import java.util.List;


public class MobSpawnControlEvent {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof Mob mob)) return;

        BlockPos pos = entity.blockPosition();
        ChunkPos chunkPos = new ChunkPos(pos);

        List<MobUpgradeType> upgradeTypes = ChunkLoaderManager.getMobUpgradeTypesForChunk(chunkPos);
        MobCategory mobCategory = mob.getClassification(false);

        if (upgradeTypes.contains(MobUpgradeType.ATTRACTOR)) {
            if (!mobCategory.isFriendly()) {
                event.setCanceled(true);
            }
        }

        if (upgradeTypes.contains(MobUpgradeType.PROVOKER)) {
            if (mobCategory.isFriendly()) {
                event.setCanceled(true);
            }
        }

        if (upgradeTypes.contains(MobUpgradeType.REPELLER)) {
            event.setCanceled(true);
        }
    }
}
