package pl.zwiadowcy.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import pl.zwiadowcy.network.ActionPacket;
import pl.zwiadowcy.network.ModNetwork;
import pl.zwiadowcy.network.OpenEndScreenPacket;

/**
 * Ekran końca tomu: krótkie podsumowanie, napisy końcowe i przejście do kolejnej części
 * („Kontynuuj do części drugiej” po tomie I, zapowiedź części trzeciej i powrót do wolnej gry po tomie II).
 */
public class EndOfVolumeScreen extends Screen {
    private static final int W = 360, H = 262;
    private static final int INK = 0xFF3B2A1A, ACCENT = 0xFF8A2B1A, FAINT = 0xFF8A7458;

    private final OpenEndScreenPacket msg;
    private int left, top;

    public EndOfVolumeScreen(OpenEndScreenPacket msg) {
        super(Component.literal(msg.volume() == 1 ? "Koniec tomu I" : "Koniec tomu II"));
        this.msg = msg;
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        int by = top + H - 30;
        if (msg.volume() == 1) {
            Button next = Button.builder(Component.literal(msg.nextStarted() ? "Tom II już trwa" : "Kontynuuj do części drugiej"), b -> {
                ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.KONTYNUUJ_TOM2, ""));
                onClose();
            }).bounds(left + 14, by, 200, 20).build();
            next.active = !msg.nextStarted();
            addRenderableWidget(next);
            addRenderableWidget(Button.builder(Component.literal("Później"), b -> onClose()).bounds(left + W - 104, by, 90, 20).build());
        } else {
            Button next = Button.builder(Component.literal("Kontynuuj do części trzeciej (wkrótce)"), b -> {})
                    .bounds(left + 14, by - 24, W - 28, 20).build();
            next.active = false;
            addRenderableWidget(next);
            if (!msg.freePlay()) {
                addRenderableWidget(Button.builder(Component.literal("Wróć do Araluenu (wolna gra)"), b -> {
                    ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.WOLNA_GRA, ""));
                    onClose();
                }).bounds(left + 14, by, 220, 20).build());
            }
            addRenderableWidget(Button.builder(Component.literal("Zamknij"), b -> onClose()).bounds(left + W - 104, by, 90, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.fill(left - 4, top - 4, left + W + 4, top + H + 4, 0xFF3A2410);
        g.fill(left - 2, top - 2, left + W + 2, top + H + 2, 0xFF7A5428);
        g.fill(left, top, left + W, top + H, 0xFFEADCB8);
        boolean one = msg.volume() == 1;
        String title = one ? "Koniec tomu I: Ruiny Gorlanu" : "Koniec tomu II: Płonący most";
        g.drawString(font, title, left + W / 2 - font.width(title) / 2, top + 14, ACCENT, false);
        g.fill(left + 40, top + 26, left + W - 40, top + 27, ACCENT);

        String summary;
        if (one) {
            summary = msg.knight()
                    ? "Kalkarowie nie żyją, a Will stanął przed królem i wybrał Szkołę Rycerską. Halt nie zamknął przed nim drzwi chaty - bo kto raz nauczył się patrzeć jak zwiadowca, ten nie przestanie."
                    : "Kalkarowie nie żyją, a Will stanął przed królem i wybrał Korpus Zwiadowców. Na jego szyi wisi brązowy liść dębu, a Halt - po raz pierwszy - skinął głową z uznaniem.";
        } else {
            summary = "Most spłonął, Morgarath nie żyje, a Araluen świętuje zwycięstwo. Ale Will i Evanlyn płyną na północ, na pokładzie skandyjskiego okrętu. Halt już szuka ich śladów.";
        }
        int y = text(g, summary, top + 34, INK);
        y += 8;
        y = text(g, "Autorzy: ABialas & 🤖CC", y, FAINT);
        y = text(g, "Nieoficjalny mod fanowski - wolna interpretacja książek Johna Flanagana. Dziękujemy wszystkim testerom!", y, FAINT);
        y += 8;
        String teaser = one ? "Ciąg dalszy: Tom II - Płonący most" : "Ciąg dalszy: Tom III - wkrótce";
        g.drawString(font, teaser, left + W / 2 - font.width(teaser) / 2, y, ACCENT, false);
        y += 14;
        if (one) {
            text(g, "Tom II zaczniesz w każdej chwili także z dziennika (J) albo z komunikatu na czacie.", y, FAINT);
        } else {
            text(g, "Wolna gra to Araluen poza kanonem: zlecenia Halta i Gilana, obozy Wargalów i ogniska zwiadowców.", y, FAINT);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    private int text(GuiGraphics g, String s, int y, int color) {
        for (FormattedCharSequence line : font.split(Component.literal(s), W - 32)) {
            g.drawString(font, line, left + 16, y, color, false);
            y += 10;
        }
        return y;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
