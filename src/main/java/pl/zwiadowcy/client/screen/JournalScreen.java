package pl.zwiadowcy.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import pl.zwiadowcy.client.ClientQuestData;
import pl.zwiadowcy.network.ActionPacket;
import pl.zwiadowcy.network.ModNetwork;
import pl.zwiadowcy.network.QuestSyncPacket;
import pl.zwiadowcy.quest.QuestProgress;

import java.util.ArrayList;
import java.util.List;

/** Dziennik zwiadowcy: lista misji (aktywne/ukończone), szczegóły, notatki. */
public class JournalScreen extends Screen {
    private static final int W = 340, H = 210;
    private static final int INK = 0xFF3B2A1A, INK_LIGHT = 0xFF7A6248, ACCENT = 0xFF8A2B1A;

    private int left, top;
    private String selected = "";
    private boolean notesMode;
    private int scroll;
    private final List<Entry> entries = new ArrayList<>();
    private Button trackButton;

    private record Entry(int x, int y, int w, String questId, boolean header, String label) {}

    public JournalScreen() {
        super(Component.literal("Dziennik zwiadowcy"));
        if (ClientQuestData.sync != null) selected = ClientQuestData.sync.tracked();
    }

    public void refresh() {
        rebuildWidgets();
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        entries.clear();
        QuestSyncPacket sync = ClientQuestData.sync;
        int y = top + 26;
        if (sync != null) {
            List<QuestSyncPacket.QuestView> active = sync.quests().stream().filter(q -> !q.completed()).toList();
            List<QuestSyncPacket.QuestView> done = sync.quests().stream().filter(QuestSyncPacket.QuestView::completed).toList();
            if (selected.isEmpty() && !active.isEmpty()) selected = active.get(0).id();
            if (!active.isEmpty()) {
                entries.add(new Entry(left + 10, y, 110, "", true, "Aktywne"));
                y += 11;
                for (var q : active) {
                    entries.add(new Entry(left + 10, y, 110, q.id(), false, q.title()));
                    y += 11;
                }
                y += 4;
            }
            if (!done.isEmpty()) {
                entries.add(new Entry(left + 10, y, 110, "", true, "Ukończone"));
                y += 11;
                for (var q : done) {
                    if (y > top + H - 34) break;
                    entries.add(new Entry(left + 10, y, 110, q.id(), false, q.title()));
                    y += 11;
                }
            }
        }
        addRenderableWidget(Button.builder(Component.literal(notesMode ? "Misje" : "Notatki"), b -> {
            notesMode = !notesMode;
            scroll = 0;
            rebuildWidgets();
        }).bounds(left + 10, top + H - 26, 70, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Prolog"), b -> {
            if (minecraft != null) minecraft.setScreen(new StoryScreen(pl.zwiadowcy.story.StoryTexts.PROLOG,
                    pl.zwiadowcy.story.StoryTexts.get(pl.zwiadowcy.story.StoryTexts.PROLOG), false));
        }).bounds(left + 84, top + H - 26, 50, 18).build());
        // epilog po ukończeniu kampanii (serwer wybiera wersję zależnie od wyboru przed królem)
        var journal = pl.zwiadowcy.client.ClientQuestData.sync;
        if (journal != null && journal.quests().stream().anyMatch(q -> q.id().equals("przed_obliczem_krola") && q.completed())) {
            addRenderableWidget(Button.builder(Component.literal("Epilog"), b -> {
                ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.POKAZ_EPILOG, ""));
                onClose();
            }).bounds(left + 138, top + H - 26, 50, 18).build());
        }
        trackButton = addRenderableWidget(Button.builder(Component.literal("Śledź"), b -> {
            ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.SLEDZ_MISJE, selected));
        }).bounds(left + W - 70, top + H - 26, 60, 18).build());
        updateTrackButton();
    }

    private void updateTrackButton() {
        QuestSyncPacket sync = ClientQuestData.sync;
        QuestSyncPacket.QuestView q = find(selected);
        trackButton.visible = !notesMode && q != null && !q.completed() && sync != null && !q.id().equals(sync.tracked());
    }

    private QuestSyncPacket.QuestView find(String id) {
        if (ClientQuestData.sync == null) return null;
        for (var q : ClientQuestData.sync.quests()) if (q.id().equals(id)) return q;
        return null;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.fill(left - 3, top - 3, left + W + 3, top + H + 3, 0xFF4A3018);
        g.fill(left, top, left + W, top + H, 0xFFE9DCBC);
        g.fill(left + 128, top + 10, left + 129, top + H - 10, 0xFFB9A57E);
        g.drawString(font, "Dziennik zwiadowcy", left + 10, top + 9, ACCENT, false);

        for (Entry e : entries) {
            if (e.header()) {
                g.drawString(font, e.label(), e.x(), e.y(), INK_LIGHT, false);
            } else {
                boolean sel = e.questId().equals(selected);
                boolean hover = mouseX >= e.x() && mouseX < e.x() + e.w() && mouseY >= e.y() - 1 && mouseY < e.y() + 10;
                if (sel) g.fill(e.x() - 2, e.y() - 1, e.x() + e.w(), e.y() + 9, 0x30804020);
                String label = font.plainSubstrByWidth(e.label(), e.w() - 6);
                QuestSyncPacket.QuestView q = find(e.questId());
                boolean tracked = ClientQuestData.sync != null && e.questId().equals(ClientQuestData.sync.tracked());
                int color = q != null && q.completed() ? INK_LIGHT : (hover ? ACCENT : INK);
                g.drawString(font, (tracked ? "✦" : " ") + label, e.x(), e.y(), color, false);
            }
        }

        int rx = left + 138, rw = W - 148;
        int y = top + 12 - scroll;
        g.enableScissor(rx, top + 8, rx + rw, top + H - 30);
        if (notesMode) {
            y = renderNotes(g, rx, y, rw);
        } else {
            y = renderQuest(g, rx, y, rw);
        }
        g.disableScissor();
        super.render(g, mouseX, mouseY, partialTick);
    }

    private int renderQuest(GuiGraphics g, int x, int y, int w) {
        QuestSyncPacket.QuestView q = find(selected);
        if (q == null) {
            g.drawString(font, "Brak wpisów. Użyj Kroniki Araluenu,", x, y, INK_LIGHT, false);
            g.drawString(font, "aby rozpocząć opowieść.", x, y + 10, INK_LIGHT, false);
            return y + 20;
        }
        g.drawString(font, q.title(), x, y, ACCENT, false);
        y += 11;
        if (!q.act().isEmpty()) {
            g.drawString(font, q.act(), x, y, INK_LIGHT, false);
            y += 12;
        }
        for (FormattedCharSequence line : font.split(Component.literal(q.description()), w)) {
            g.drawString(font, line, x, y, INK, false);
            y += 10;
        }
        y += 6;
        for (String done : q.doneStages()) {
            for (FormattedCharSequence line : font.split(Component.literal("✔ " + done), w)) {
                g.drawString(font, line, x, y, INK_LIGHT, false);
                y += 10;
            }
        }
        if (!q.completed() && !q.currentText().isEmpty()) {
            String cur = "▶ " + q.currentText() + (q.required() > 1 ? " (" + q.progress() + "/" + q.required() + ")" : "");
            for (FormattedCharSequence line : font.split(Component.literal(cur), w)) {
                g.drawString(font, line, x, y, INK, false);
                y += 10;
            }
        }
        if (q.completed()) {
            y += 4;
            g.drawString(font, q.repeatable() ? "Zlecenie wykonane." : "Misja ukończona.", x, y, 0xFF2E6B2E, false);
            y += 10;
        }
        return y;
    }

    private int renderNotes(GuiGraphics g, int x, int y, int w) {
        QuestSyncPacket sync = ClientQuestData.sync;
        if (sync == null || sync.notes().isEmpty()) {
            g.drawString(font, "Brak notatek.", x, y, INK_LIGHT, false);
            return y + 10;
        }
        for (QuestProgress.Note n : sync.notes()) {
            g.drawString(font, n.title(), x, y, ACCENT, false);
            y += 11;
            for (FormattedCharSequence line : font.split(Component.literal(n.text()), w)) {
                g.drawString(font, line, x, y, INK, false);
                y += 10;
            }
            y += 8;
        }
        return y;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Entry e : entries) {
            if (!e.header() && mouseX >= e.x() && mouseX < e.x() + e.w() && mouseY >= e.y() - 1 && mouseY < e.y() + 10) {
                selected = e.questId();
                notesMode = false;
                scroll = 0;
                rebuildWidgets();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll = Math.max(0, scroll - (int) (delta * 12));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
