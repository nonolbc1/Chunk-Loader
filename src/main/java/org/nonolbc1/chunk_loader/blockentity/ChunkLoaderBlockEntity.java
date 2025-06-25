package org.nonolbc1.chunk_loader.blockentity;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.nonolbc1.chunk_loader.itemhandler.ChunkLoaderItemHandler;
import org.nonolbc1.chunk_loader.manager.ChunkLoaderManager;
import org.nonolbc1.chunk_loader.menu.ChunkLoaderMenu;
import org.nonolbc1.chunk_loader.type.MobUpgradeType;

import java.util.*;

public class ChunkLoaderBlockEntity extends BlockEntity implements MenuProvider {
    private final UUID uuid = UUID.randomUUID();
    private final ChunkLoaderItemHandler itemHandler;
    private final ContainerData data;
    private Set<ChunkPos> loadedChunks = new HashSet<>();
    private int lastRadius = -1;
    private MobUpgradeType lastMobUpgradeType = MobUpgradeType.NONE;
    private List<MobEffectInstance> lastPotionEffects = new ArrayList<>();
    private boolean chunksToReload = false;

    public ChunkLoaderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHUNK_LOADER_BE.get(), pos, state);

        itemHandler = new ChunkLoaderItemHandler(() -> {
            this.setChanged();
            if (level != null && !level.isClientSide) {
                inventoryChanged();
            }
        });

        data = new ContainerData() {
            @Override
            public int get(int index) {
                if (index == 0) return itemHandler.getPowerLevel();
                return 0;
            }

            @Override
            public void set(int index, int value) {

            }

            @Override
            public int getCount() {
                return 1;
            }
        };
    }

    @Override
    public void setLevel(@NotNull Level level) {
        super.setLevel(level);
        if (itemHandler != null) {
            itemHandler.setLevel(level);
            itemHandler.setPos(this.worldPosition);
        }
    }


    @NotNull
    @Override
    public Component getDisplayName() {
        return Component.literal("Chunk Loader");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player player) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(worldPosition);
        return new ChunkLoaderMenu(id, playerInventory, buf);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel serverLevel) {
            for (ChunkPos chunkPos : loadedChunks) {
                ChunkLoaderManager.unloadChunk(serverLevel, chunkPos);
            }
            loadedChunks.clear();
            this.lastRadius = -1;
            this.lastMobUpgradeType = MobUpgradeType.NONE;
            this.lastPotionEffects = new ArrayList<>();
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Radius", this.lastRadius);

        ListTag chunkList = new ListTag();
        for (ChunkPos pos : loadedChunks) {
            CompoundTag chunkTag = new CompoundTag();
            chunkTag.putInt("x", pos.x);
            chunkTag.putInt("z", pos.z);
            chunkList.add(chunkTag);
        }
        tag.put("LoadedChunks", chunkList);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.lastRadius = tag.getInt("Radius");

        this.loadedChunks.clear();
        ListTag chunkList = tag.getList("LoadedChunks", Tag.TAG_COMPOUND);
        for (Tag chunkTag : chunkList) {
            CompoundTag ct = (CompoundTag) chunkTag;
            int x = ct.getInt("x");
            int z = ct.getInt("z");
            this.loadedChunks.add(new ChunkPos(x, z));
        }

        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
            this.setChanged();
        }
        this.lastMobUpgradeType = itemHandler.getMobUpgradeType();
        this.lastPotionEffects = itemHandler.getPotionEffects();

        this.chunksToReload = true;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        loadAdditional(tag, registries);
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(t instanceof ChunkLoaderBlockEntity be)) return;

        if (be.chunksToReload) {
            MobUpgradeType mobUpgradeType = be.itemHandler.getMobUpgradeType();
            List<MobEffectInstance> potionEffects = be.itemHandler.getPotionEffects();

            for (ChunkPos chunkPos : be.loadedChunks) {
                ChunkLoaderManager.loadChunk(serverLevel, chunkPos);
                ChunkLoaderManager.setChunkMobUpgradeTypes(chunkPos, be.uuid, mobUpgradeType);
                ChunkLoaderManager.setChunkPotionEffects(chunkPos, be.uuid, potionEffects);
            }
            be.chunksToReload = false;
        }
    }

    public static <T extends BlockEntity> void clientTick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        if (!(t instanceof ChunkLoaderBlockEntity be)) return;

        int power = be.itemHandler.getPowerLevel();
        if (power < 0) return;

        double gameTime = level.getGameTime() + Math.random();
        double radius = 0.8 + (power * 0.2);
        double angle = (gameTime / 10.0 + power) % (2 * Math.PI);

        double x = blockPos.getX() + 0.5 + Math.cos(angle) * radius;
        double y = blockPos.getY() + 0.25 + Math.sin(gameTime / 20.0) * 0.1;
        double z = blockPos.getZ() + 0.5 + Math.sin(angle) * radius;

        Vector3f color = switch (power) {
            case 0 -> new Vector3f(0.6f, 0.2f, 0.0f);
            case 1 -> new Vector3f(0.8f, 0.8f, 0.8f);
            case 2 -> new Vector3f(1.0f, 0.85f, 0.0f);
            case 3 -> new Vector3f(0.0f, 0.8f, 1.0f);
            case 4 -> new Vector3f(0.1f, 0.1f, 0.1f);
            case 5 -> new Vector3f(1.0f, 1.0f, 0.8f);
            default -> null;
        };

        if (color != null) {
            float size = 0.8f;
            DustParticleOptions dust = new DustParticleOptions(color, size);
            level.addParticle(dust, x, y, z, 0, 0.01, 0);
        }
    }

    public void inventoryChanged() {
        if (this.level instanceof ServerLevel serverLevel) {
            int radius = this.itemHandler.getPowerRadius();
            if (radius != this.lastRadius) {
                this.unloadChunks(serverLevel);
                this.lastRadius = radius;
            }

            MobUpgradeType mobUpgradeType = this.itemHandler.getMobUpgradeType();
            if (mobUpgradeType != this.lastMobUpgradeType) {
                this.removeMobUpgradeType();
                this.lastMobUpgradeType = mobUpgradeType;
            }

            List<MobEffectInstance> potionEffects = this.itemHandler.getPotionEffects();
            boolean potionEffectsChanged = !this.lastPotionEffects.equals(potionEffects);
            if (potionEffectsChanged) {
                this.removePotionEffects();
                this.lastPotionEffects = potionEffects;
            }

            if (radius < 0) return;

            Set<ChunkPos> newLoadedChunks = new HashSet<>();
            BlockPos blockPos = this.getBlockPos();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    ChunkPos chunkPos = new ChunkPos((blockPos.getX() >> 4) + dx, (blockPos.getZ() >> 4) + dz);
                    ChunkLoaderManager.loadChunk(serverLevel, chunkPos);
                    ChunkLoaderManager.setChunkMobUpgradeTypes(chunkPos, this.uuid, mobUpgradeType);
                    ChunkLoaderManager.setChunkPotionEffects(chunkPos, this.uuid, potionEffects);
                    newLoadedChunks.add(chunkPos);
                }
            }
            this.loadedChunks = newLoadedChunks;
        }
    }

    private void unloadChunks(ServerLevel level) {
        for (ChunkPos chunkPos : loadedChunks) {
            ChunkLoaderManager.unloadChunk(level, chunkPos);
        }

        loadedChunks.clear();
    }

    private void removeMobUpgradeType() {
        for (ChunkPos chunkPos : loadedChunks) {
            ChunkLoaderManager.removeMobUpgradeType(chunkPos, this.uuid);
        }
    }

    private void removePotionEffects() {
        for (ChunkPos chunkPos : loadedChunks) {
            ChunkLoaderManager.removePotionEffects(chunkPos, this.uuid);
        }
    }



    public ChunkLoaderItemHandler getItemHandler() {
        return itemHandler;
    }

    public ContainerData getData() {
        return data;
    }
}
