package org.nonolbc1.chunk_loader.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import org.jetbrains.annotations.NotNull;
import org.nonolbc1.chunk_loader.ChunkLoader;
import org.nonolbc1.chunk_loader.menu.ChunkLoaderMenu;

public class ChunkLoaderScreen extends AbstractContainerScreen<ChunkLoaderMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ChunkLoader.MODID, "textures/gui/chunk_loader.png");
    private static final ResourceLocation POTION_SLOTS_TEXTURE = ResourceLocation.fromNamespaceAndPath(ChunkLoader.MODID, "textures/gui/potion_upgrader_slots.png");
    private static final ResourceLocation POTION_SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(ChunkLoader.MODID, "textures/gui/slot_potion.png");
    private static final ResourceLocation MOB_SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(ChunkLoader.MODID, "textures/gui/slot_mob_upgrader.png");

    public ChunkLoaderScreen(ChunkLoaderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176 + (36 - 3);
        this.imageHeight = 166;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 176, 166, 176, 166);
        guiGraphics.blit(POTION_SLOTS_TEXTURE, this.leftPos + 176 - 3, this.topPos, 0, 0, 36, 104, 36, 104);

        ContainerData data = this.menu.getData();
        int power = data.get(0);
        if (power >= 1) {
            guiGraphics.blit(MOB_SLOT_TEXTURE, this.leftPos + 138, this.topPos + 21, 0, 0, 18, 37, 18, 37);
        }
        for (int i = 0; i < power; i++) {
            int x = this.leftPos + 176 - 3 + 4;
            int y = this.topPos + 7 + i * 18;
            guiGraphics.blit(POTION_SLOT_TEXTURE, x, y, 0, 0, 18, 18, 18, 18);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    public void init() {
        super.init();
    }
}
