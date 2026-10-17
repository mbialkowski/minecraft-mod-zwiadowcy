package pl.zwiadowcy.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;
import pl.zwiadowcy.network.ActionPacket;
import pl.zwiadowcy.network.ModNetwork;
import pl.zwiadowcy.story.StoryTexts;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Ekran-kronika: strony opowieści na pergaminie, przewijane kliknięciem lub strzałkami. */
public class StoryScreen extends Screen {
    private static final int W = 340, H = 250;
    private static final int INK = 0xFF3B2A1A, ACCENT = 0xFF8A2B1A, FAINT = 0xFF8A7458;

    private final String storyId;
    private final List<StoryTexts.Page> pages;
    private final boolean notifyServer;
    private int page;
    private int left, top;
    private boolean finished;

    public StoryScreen(String storyId, List<StoryTexts.Page> pages, boolean notifyServer) {
        super(Component.literal("Kronika"));
        this.storyId = storyId;
        this.pages = pages.isEmpty() ? List.of(new StoryTexts.Page("…", "", "")) : pages;
        this.notifyServer = notifyServer;
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        boolean last = page == pages.size() - 1;
        if (page > 0) {
            addRenderableWidget(Button.builder(Component.literal("◀ Wstecz"), b -> turn(-1))
                    .bounds(left + 12, top + H - 28, 70, 18).build());
        }
        addRenderableWidget(Button.builder(Component.literal(last ? (notifyServer ? "Zaczynamy!" : "Zamknij") : "Dalej ▶"),
                b -> { if (last) finish(); else turn(1); }).bounds(left + W - 92, top + H - 28, 80, 18).build());
    }

    private void turn(int dir) {
        page = Math.max(0, Math.min(pages.size() - 1, page + dir));
        rebuildWidgets();
    }

    private void finish() {
        if (!finished) {
            finished = true;
            if (notifyServer) ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.KONIEC_OPOWIESCI, storyId));
        }
        if (minecraft != null) minecraft.setScreen(null);
    }

    @Override
    public void onClose() {
        finish();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.fill(left - 4, top - 4, left + W + 4, top + H + 4, 0xFF3A2410);
        g.fill(left - 2, top - 2, left + W + 2, top + H + 2, 0xFF7A5428);
        g.fill(left, top, left + W, top + H, 0xFFEADCB8);
        g.fill(left + 6, top + 6, left + W - 6, top + 7, 0xFFC9B48A);
        g.fill(left + 6, top + H - 34, left + W - 6, top + H - 33, 0xFFC9B48A);

        StoryTexts.Page p = pages.get(page);
        ItemStack icon = icon(p.icon());
        int textLeft = left + 16;
        if (icon != null) {
            g.pose().pushPose();
            g.pose().translate(left + W - 46, top + 12, 0);
            g.pose().scale(2f, 2f, 1f);
            g.renderItem(icon, 0, 0);
            g.pose().popPose();
        }
        g.drawString(font, p.title(), textLeft, top + 16, ACCENT, false);
        g.fill(textLeft, top + 28, textLeft + font.width(p.title()) + 20, top + 29, ACCENT);

        int y = top + 38;
        int maxW = W - 32 - (icon != null ? 26 : 0);
        for (FormattedCharSequence line : wrap(p.text(), maxW)) {
            if (y > top + H - 46) break;
            g.drawString(font, line, textLeft, y, INK, false);
            y += 10;
        }
        String counter = (page + 1) + " / " + pages.size();
        g.drawString(font, counter, left + W / 2 - font.width(counter) / 2, top + H - 23, FAINT, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    private List<FormattedCharSequence> wrap(String text, int maxW) {
        List<FormattedCharSequence> out = new ArrayList<>();
        for (String para : text.split("\n", -1)) {
            if (para.isEmpty()) out.add(FormattedCharSequence.EMPTY);
            else out.addAll(font.split(Component.literal(para), maxW));
        }
        return out;
    }

    @Nullable
    private static ItemStack icon(String id) {
        if (id == null || id.isEmpty()) return null;
        ResourceLocation rl = ResourceLocation.tryParse(id);
        Item item = rl != null ? ForgeRegistries.ITEMS.getValue(rl) : null;
        return item != null ? new ItemStack(item) : null;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_SPACE || key == GLFW.GLFW_KEY_ENTER) {
            if (page == pages.size() - 1) finish();
            else turn(1);
            return true;
        }
        if (key == GLFW.GLFW_KEY_LEFT) {
            turn(-1);
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
