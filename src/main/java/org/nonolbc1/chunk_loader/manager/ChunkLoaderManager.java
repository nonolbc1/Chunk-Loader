package org.nonolbc1.chunk_loader.manager;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import org.nonolbc1.chunk_loader.type.MobUpgradeType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Comparator;
import java.util.Collections;
import java.util.ArrayList;

public class ChunkLoaderManager {
    private static final Map<ChunkPos, Integer> chunkTicketCounts = new HashMap<>();
    private static final Map<ChunkPos, List<MobUpgradeType>> chunkMobUpgradeTypes = new HashMap<>();
    private static final Map<ChunkPos, Map<UUID, MobUpgradeType>> chunkMobUpgradeSources = new HashMap<>();
    private static final Map<ChunkPos, List<MobEffectInstance>> chunkPotionEffects = new HashMap<>();
    private static final Map<ChunkPos, Map<UUID, List<MobEffectInstance>>> chunkPotionSources = new HashMap<>();
    private static int tickCounter = 0;

    public static void loadChunk(ServerLevel level, ChunkPos chunkPos) {
        int count = chunkTicketCounts.getOrDefault(chunkPos, 0);
        if (count == 0) {
            level.getChunkSource().addRegionTicket(TicketType.START, chunkPos, 1, Unit.INSTANCE);
        }
        chunkTicketCounts.put(chunkPos, count + 1);
    }

    public static void unloadChunk(ServerLevel level, ChunkPos chunkPos) {
        int count = chunkTicketCounts.getOrDefault(chunkPos, 0);
        if (count <= 1) {
            chunkTicketCounts.remove(chunkPos);
            chunkMobUpgradeTypes.remove(chunkPos);
            chunkPotionEffects.remove(chunkPos);
            level.getChunkSource().removeRegionTicket(TicketType.START, chunkPos, 1, Unit.INSTANCE);
        } else {
            chunkTicketCounts.put(chunkPos, count - 1);
        }
    }

    public static void setChunkMobUpgradeTypes(ChunkPos chunkPos, UUID sourceId, MobUpgradeType newType) {
        Map<UUID, MobUpgradeType> sources = chunkMobUpgradeSources.computeIfAbsent(chunkPos, k -> new HashMap<>());
        sources.put(sourceId, newType);

        List<MobUpgradeType> types = sources.values().stream()
                .filter(type -> type != MobUpgradeType.NONE)
                .distinct()
                .sorted(Comparator.comparingInt((MobUpgradeType t) -> t.ordinal()).reversed())
                .toList();

        chunkMobUpgradeTypes.put(chunkPos, types);
    }

    public static List<MobUpgradeType> getMobUpgradeTypesForChunk(ChunkPos chunkPos) {
        return chunkMobUpgradeTypes.getOrDefault(chunkPos, Collections.emptyList());
    }

    public static void removeMobUpgradeType(ChunkPos chunkPos, UUID sourceId) {
        Map<UUID, MobUpgradeType> sources = chunkMobUpgradeSources.get(chunkPos);
        if (sources == null) {
            return;
        }

        sources.remove(sourceId);
        if (sources.isEmpty()) {
            chunkMobUpgradeSources.remove(chunkPos);
            chunkMobUpgradeTypes.remove(chunkPos);
        } else {
            List<MobUpgradeType> types = sources.values().stream()
                    .filter(type -> type != MobUpgradeType.NONE)
                    .distinct()
                    .sorted(Comparator.comparingInt((MobUpgradeType t) -> t.ordinal()).reversed())
                    .toList();

            chunkMobUpgradeTypes.put(chunkPos, types);
        }
    }

    public static void setChunkPotionEffects(ChunkPos chunkPos, UUID sourceId, List<MobEffectInstance> newEffects) {
        Map<UUID, List<MobEffectInstance>> sources = chunkPotionSources.computeIfAbsent(chunkPos, k -> new HashMap<>());
        sources.put(sourceId, new ArrayList<>(newEffects));

        // Rebuild merged list
        Map<MobEffect, MobEffectInstance> merged = new HashMap<>();
        for (List<MobEffectInstance> effectList : sources.values()) {
            for (MobEffectInstance effect : effectList) {
                merged.put(effect.getEffect().value(), effect);
            }
        }

        chunkPotionEffects.put(chunkPos, new ArrayList<>(merged.values()));
    }

    public static void removePotionEffects(ChunkPos chunkPos, UUID sourceId) {
        Map<UUID, List<MobEffectInstance>> sources = chunkPotionSources.get(chunkPos);
        if (sources == null) {
            return;
        }

        sources.remove(sourceId);
        Map<MobEffect, MobEffectInstance> merged = new HashMap<>();
        for (List<MobEffectInstance> effectList : sources.values()) {
            for (MobEffectInstance effect : effectList) {
                merged.put(effect.getEffect().value(), effect);
            }
        }

        if (merged.isEmpty()) {
            chunkPotionEffects.remove(chunkPos);
            chunkPotionSources.remove(chunkPos);
        } else {
            chunkPotionEffects.put(chunkPos, new ArrayList<>(merged.values()));
        }
    }

    public static void tick(ServerLevel level) {
        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;

        for (ChunkPos chunkPos : chunkPotionEffects.keySet()) {
            List<MobEffectInstance> effects = chunkPotionEffects.get(chunkPos);
            if (effects.isEmpty()) continue;

            int minX = chunkPos.getMinBlockX();
            int minZ = chunkPos.getMinBlockZ();
            AABB chunkBox = new AABB(minX, 0, minZ, minX + 16, level.getMaxBuildHeight(), minZ + 16);

            List<Player> playersInChunk = level.getEntitiesOfClass(Player.class, chunkBox, player -> true);

            for (Player player : playersInChunk) {
                for (MobEffectInstance effect : effects) {
                    MobEffectInstance newEffect = new MobEffectInstance(
                            effect.getEffect(),
                            40,
                            effect.getAmplifier(),
                            effect.isAmbient(),
                            effect.isVisible()
                    );
                    player.addEffect(newEffect);
                }
            }
        }
    }
}
