package org.nonolbc1.chunk_loader.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.nonolbc1.chunk_loader.block.ModBlocks;
import org.nonolbc1.chunk_loader.blockentity.ChunkLoaderBlockEntity;
import org.nonolbc1.chunk_loader.itemhandler.ChunkLoaderItemHandler;
import org.nonolbc1.chunk_loader.slot.ChunkLoaderLockedSlot;

public class ChunkLoaderMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final ChunkLoaderItemHandler internal;
    private final BlockPos blockPos;
    private final ContainerData data;

    public ChunkLoaderMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        super(ModMenus.CHUNK_LOADER_MENU.get(), id);
        blockPos = buf.readBlockPos();
        Level level = playerInv.player.level();
        this.access = ContainerLevelAccess.create(level, blockPos);
        BlockEntity be = level.getBlockEntity(blockPos);
        if (be instanceof ChunkLoaderBlockEntity chunkLoader) {
            this.internal = chunkLoader.getItemHandler();
            this.data = chunkLoader.getData();
        } else {
            this.internal = new ChunkLoaderItemHandler(null);
            this.data = new SimpleContainerData(1);
        }
        this.addDataSlots(this.data);

        this.addSlot(new SlotItemHandler(internal, 0, 51, 41));
        this.addSlot(new ChunkLoaderLockedSlot(internal, 1, 139, 41, () -> data.get(0) >= 1));
        for (int i = 0; i < 5; i++) {
            int index = i + 2;
            int x = 178;
            int y = 8 + i * 18;
            int requiredPower = i + 1;

            this.addSlot(new ChunkLoaderLockedSlot(internal, index, x, y, () -> data.get(0) >= requiredPower));
        }

        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(playerInv, sj + (si + 1) * 9, 8 + sj * 18, 84 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(playerInv, si, 8 + si * 18, 142));
    }
    @Override
    @NotNull
    public ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack originalStack = slot.getItem();
        ItemStack copy = originalStack.copy();

        int containerSize = 7;
        int playerInventoryEnd = this.slots.size();

        if (index < containerSize) {
            // From container to player inventory
            if (!moveItemStackTo(originalStack, containerSize, playerInventoryEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // From player inventory to container
            if (!moveItemStackTo(originalStack, 0, containerSize, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (originalStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        slot.onTake(player, originalStack);
        return copy;
    }

    @Override
    protected boolean moveItemStackTo(@NotNull ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean moved = false;
        int index = reverseDirection ? endIndex - 1 : startIndex;

        // Try to merge with existing stacks
        while (!stack.isEmpty() && (reverseDirection ? index >= startIndex : index < endIndex)) {
            Slot slot = this.slots.get(index);
            ItemStack existing = slot.getItem();

            if (!existing.isEmpty()
                    && ItemStack.isSameItemSameComponents(stack, existing)
                    && slot.mayPlace(stack)) {
                int max = Math.min(stack.getMaxStackSize(), slot.getMaxStackSize(stack));
                int total = existing.getCount() + stack.getCount();

                if (total <= max) {
                    stack.setCount(0);
                    existing.setCount(total);
                    slot.setChanged();
                    moved = true;
                    break;
                } else if (existing.getCount() < max) {
                    int diff = max - existing.getCount();
                    stack.shrink(diff);
                    existing.grow(diff);
                    slot.setChanged();
                    moved = true;
                }
            }

            index += reverseDirection ? -1 : 1;
        }

        // Try to insert into empty slots
        index = reverseDirection ? endIndex - 1 : startIndex;
        while (!stack.isEmpty() && (reverseDirection ? index >= startIndex : index < endIndex)) {
            Slot slot = this.slots.get(index);
            if (slot.getItem().isEmpty() && slot.mayPlace(stack)) {
                slot.setByPlayer(stack.split(slot.getMaxStackSize(stack)));
                slot.setChanged();
                moved = true;
                break;
            }
            index += reverseDirection ? -1 : 1;
        }

        return moved;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.CHUNK_LOADER_BLOCK.get());
    }

    public ContainerData getData() {
        return this.data;
    }
}
