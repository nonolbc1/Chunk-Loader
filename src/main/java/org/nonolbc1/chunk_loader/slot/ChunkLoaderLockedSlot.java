package org.nonolbc1.chunk_loader.slot;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ChunkLoaderLockedSlot extends SlotItemHandler {
    private final Supplier<Boolean> unlocked;

    public ChunkLoaderLockedSlot(IItemHandler handler, int index, int x, int y, Supplier<Boolean> unlocked) {
        super(handler, index, x, y);
        this.unlocked = unlocked;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return unlocked.get() && super.mayPlace(stack);
    }

    @Override
    public boolean isActive() {
        return unlocked.get();
    }
}