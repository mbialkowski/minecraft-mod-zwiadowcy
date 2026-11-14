package pl.zwiadowcy.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import pl.zwiadowcy.network.ActionPacket;
import pl.zwiadowcy.network.ModNetwork;

import java.util.ArrayList;
import java.util.List;

/**
 * Potwierdzenie szybkiego startu od tomu II (nowy świat, ostatnia strona Kroniki). Wyjaśnia, co gracz pomija i co
 * dostaje, i daje dwie drogi: tom II od razu albo powrót do początku tomu I (zalecany). Esc wraca do Kroniki.
 */
public class VolumeChoiceScreen extends Screen {
    private static final int W = 340, H = 250;
    private static final int INK = 0xFF3B2A1A, ACCENT = 0xFF8A2B1A;

    private static final String TEXT = String.join("\n",
            "Tom II „Płonący most” zaczyna się zimą po wydarzeniach tomu I i zakłada, że Will jest już uczniem zwiadowcy.",
            "",
            "Pominiesz cały tom I: Dzień Wyboru, szkolenie u Halta, oswojenie Wyrwija, Zlot Zwiadowców i walkę z Kalkarami "
                    + "w Ruinach Gorlanu (14 misji).",
            "",
            "Dostaniesz zestaw ucznia zwiadowcy (łuk, kołczan, saksę, noże, płaszcz, olej) i Wyrwija, a w dzienniku - "
                    + "streszczenie tomu I. Wybór z tomu I przyjmie wariant zwiadowcy.",
            "",
            "Tej decyzji nie da się cofnąć w tym świecie: tom I zagrasz już tylko w nowym świecie.");

    private final StoryScreen parent;

    public VolumeChoiceScreen(StoryScreen parent) {
        super(Component.literal("Zacząć od tomu II?"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = (width - W) / 2, top = (height - H) / 2;
        addRenderableWidget(Button.builder(Component.literal("Zacznij od tomu I (zalecane)"), b -> {
            parent.finishChronicle();
        }).bounds(left + 16, top + H - 28, 150, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Tak, zaczynam od tomu II"), b -> {
            ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.SZYBKI_START, ""));
            parent.finishChronicle();
        }).bounds(left + W - 166, top + H - 28, 150, 18).build());
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int left = (width - W) / 2, top = (height - H) / 2;
        g.fill(left - 4, top - 4, left + W + 4, top + H + 4, 0xFF3A2410);
        g.fill(left - 2, top - 2, left + W + 2, top + H + 2, 0xFF7A5428);
        g.fill(left, top, left + W, top + H, 0xFFEADCB8);
        g.fill(left + 6, top + H - 34, left + W - 6, top + H - 33, 0xFFC9B48A);
        g.drawString(font, title.getString(), left + 16, top + 16, ACCENT, false);
        g.fill(left + 16, top + 28, left + 36 + font.width(title.getString()), top + 29, ACCENT);
        int y = top + 38;
        for (FormattedCharSequence line : wrap(TEXT, W - 32)) {
            g.drawString(font, line, left + 16, y, INK, false);
            y += 10;
        }
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
