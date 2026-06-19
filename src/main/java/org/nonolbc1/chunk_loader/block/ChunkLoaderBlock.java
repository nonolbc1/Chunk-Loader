package org.nonolbc1.chunk_loader.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nonolbc1.chunk_loader.blockentity.ChunkLoaderBlockEntity;
import org.nonolbc1.chunk_loader.blockentity.ModBlockEntities;
import org.nonolbc1.chunk_loader.manager.ChunkLoaderManager;

public class ChunkLoaderBlock extends Block implements EntityBlock {
    public ChunkLoaderBlock(Properties properties) {
        super(properties.mapColor(MapColor.STONE).strength(6.0F, 10f).requiresCorrectToolForDrops().pushReaction(PushReaction.IGNORE));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new ChunkLoaderBlockEntity(blockPos, blockState);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (type != ModBlockEntities.CHUNK_LOADER_BE.get()) return null;

        if (level.isClientSide) {
            return ChunkLoaderBlockEntity::clientTick;
        } else {
            return ChunkLoaderBlockEntity::serverTick;
        }
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MenuProvider provider && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(provider, pos);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }


    @Override
    public @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MenuProvider provider && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(provider, pos);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, boolean movedByPiston) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChunkLoaderBlockEntity chunkLoaderBE) {
            ChunkPos chunkPos = new ChunkPos(pos);
            ChunkLoaderManager.unloadChunk(level, chunkPos, chunkLoaderBE.getLastRadius());
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}
