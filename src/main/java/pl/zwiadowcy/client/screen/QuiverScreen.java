package pl.zwiadowcy.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import pl.zwiadowcy.item.QuiverItem;
import pl.zwiadowcy.item.QuiverMenu;

/** Ekran kołczana - tło lejka z zakrytą piątą przegródką; wybrana przegródka obramowana. */
public class QuiverScreen extends AbstractContainerScreen<QuiverMenu> {
    private static final ResourceLocation BG = ResourceLocation.withDefaultNamespace("textures/gui/container/hopper.png");

    public QuiverScreen(QuiverMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageHeight = 133;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        g.fill(leftPos + 115, topPos + 19, leftPos + 133, topPos + 37, 0xFFC6C6C6);
        ItemStack quiver = minecraft != null && minecraft.player != null ? QuiverItem.findQuiver(minecraft.player) : ItemStack.EMPTY;
        if (!quiver.isEmpty()) {
            int sel = QuiverItem.selectedIndex(quiver);
            int x = leftPos + 43 + sel * 18, y = topPos + 19;
            g.fill(x - 1, y - 1, x + 19, y, 0xFFE0B04A);
            g.fill(x - 1, y + 18, x + 19, y + 19, 0xFFE0B04A);
            g.fill(x - 1, y, x, y + 18, 0xFFE0B04A);
            g.fill(x + 18, y, x + 19, y + 18, 0xFFE0B04A);
        }
        g.drawString(font, "R - zmiana strzały", leftPos + 104, topPos + 6, 0xFF404040, false);
    }
}
