package org.nonolbc1.chunk_loader.itemhandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.nonolbc1.chunk_loader.item.ModItems;
import org.nonolbc1.chunk_loader.type.MobUpgradeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ChunkLoaderItemHandler extends ItemStackHandler {
    private boolean isDeserializing = false;
    private final Runnable onContentsChangedCallback;
    private Level level;
    private BlockPos pos;
    private static final Set<Item> POWER_ITEMS = Set.of(
            Items.STICK,
            Items.IRON_INGOT,
            Items.GOLD_INGOT,
            Items.DIAMOND,
            Items.NETHERITE_INGOT,
            Items.NETHER_STAR
    );
    private static final Set<Item> MOB_UPGRADER_ITEMS = Set.of(
            ModItems.MOB_ATTRACTOR_ITEM.get(),
            ModItems.MOB_PROVOKER_ITEM.get(),
            ModItems.MOB_REPELLER_ITEM.get()
    );
    private static final Set<Item> POTION_UPGRADER_ITEMS = Set.of(
            Items.SPLASH_POTION
    );

    public ChunkLoaderItemHandler(Runnable onContentsChangedCallback) {
        super(7);
        this.onContentsChangedCallback = onContentsChangedCallback;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot == 0) {
            return POWER_ITEMS.contains(stack.getItem());
        }

        int powerLevel = getPowerLevel();
        if (slot == 1) {
            return powerLevel >= 1 && MOB_UPGRADER_ITEMS.contains(stack.getItem());
        }
        if (slot >= 2 && slot <= 6) {
            if (slot - 1 > powerLevel || !POTION_UPGRADER_ITEMS.contains(stack.getItem())) {
                return false;
            }

            for (int i = 2; i <= 6; i++) {
                if (i == slot) continue;
                ItemStack other = getStackInSlot(i);
                if (ItemStack.isSameItemSameComponents(other, stack)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot == 0) return 1;
        if (slot == 1) return 1;
        if (slot <= 7) return 1;

        return 64;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag nbt) {
        isDeserializing = true;
        super.deserializeNBT(provider, nbt);
        isDeserializing = false;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);

        if (isDeserializing || level == null || level.isClientSide) return;

        if (slot == 0) {
            int powerLevel = getPowerLevel();

            // Mob upgrade slot
            if (powerLevel < 1) {
                ItemStack removed = getStackInSlot(1);
                if (!removed.isEmpty()) {
                    dropItem(removed);
                    super.setStackInSlot(1, ItemStack.EMPTY);
                }
            }

            // Potion slots
            for (int i = 2; i <= 6; i++) {
                if (i - 1 > powerLevel) {
                    ItemStack removed = getStackInSlot(i);
                    if (!removed.isEmpty()) {
                        dropItem(removed);
                        super.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            }
        }

        if (onContentsChangedCallback != null) {
            onContentsChangedCallback.run();
        }
    }

    private void dropItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    stack);
            level.addFreshEntity(itemEntity);
        }
    }

    public int getPowerLevel() {
        Item powerItem = getStackInSlot(0).getItem();

        if (powerItem == Items.NETHER_STAR) return 5;
        if (powerItem == Items.NETHERITE_INGOT) return 4;
        if (powerItem == Items.DIAMOND) return 3;
        if (powerItem == Items.GOLD_INGOT) return 2;
        if (powerItem == Items.IRON_INGOT) return 1;
        if (powerItem == Items.STICK) return 0;

        return -1;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public List<MobEffectInstance> getPotionEffects() {
        List<MobEffectInstance> potionEffects = new ArrayList<>();
        for (int i = 0; i < this.stacks.size(); i++) {
            ItemStack potionStack = this.getStackInSlot(i);
            if (this.isItemValid(i, potionStack)) {
                PotionContents contents = potionStack.get(DataComponents.POTION_CONTENTS);
                if (contents != null) {
                    potionEffects.addAll((Collection<? extends MobEffectInstance>) contents.getAllEffects());
                }
            }
        }
        return potionEffects;
    }

    public MobUpgradeType getMobUpgradeType() {
        ItemStack mobUpgradeStack = this.getStackInSlot(1);

        if (mobUpgradeStack.getItem() == ModItems.MOB_ATTRACTOR_ITEM.get()) return MobUpgradeType.ATTRACTOR;
        else if (mobUpgradeStack.getItem() == ModItems.MOB_PROVOKER_ITEM.get()) return MobUpgradeType.PROVOKER;
        else if (mobUpgradeStack.getItem() == ModItems.MOB_REPELLER_ITEM.get()) return MobUpgradeType.REPELLER;
        return MobUpgradeType.NONE;
    }

    public int getPowerRadius() {
        ItemStack stack = this.getStackInSlot(0);
        if (stack.is(Items.STICK)) return 0;
        if (stack.is(Items.IRON_INGOT)) return 1;
        if (stack.is(Items.GOLD_INGOT)) return 2;
        if (stack.is(Items.DIAMOND)) return 4;
        if (stack.is(Items.NETHERITE_INGOT)) return 6;
        if (stack.is(Items.NETHER_STAR)) return 8;
        return -1;
    }
}
