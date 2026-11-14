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
import pl.zwiadowcy.skill.Skills;
import pl.zwiadowcy.story.StoryTexts;

import java.util.ArrayList;
import java.util.List;

/**
 * Dziennik zwiadowcy: misje podzielone na tomy (przewijana lista), szczegóły misji, notatki, umiejętności,
 * wnioski ze śledztw, drużyna (opisowe zaufanie) oraz prolog/epilog, przejście do tomu II i poziom trudności.
 */
public class JournalScreen extends Screen {
    private static final int W = 380, H = 232;
    private static final int LIST_W = 128;
    private static final int INK = 0xFF3B2A1A, INK_LIGHT = 0xFF7A6248, ACCENT = 0xFF8A2B1A, GREEN = 0xFF2E6B2E;

    private enum Tab { MISJE("Misje"), NOTATKI("Notatki"), UMIEJETNOSCI("Umiejętności"), WNIOSKI("Wnioski"), DRUZYNA("Drużyna");
        final String label;
        Tab(String label) { this.label = label; }
    }

    private int left, top;
    private String selected = "";
    private Tab tab = Tab.MISJE;
    private int scroll;
    private int listScroll;
    private final List<Entry> entries = new ArrayList<>();
    private final List<int[]> tabBounds = new ArrayList<>();
    private Button trackButton;

    private record Entry(int y, String questId, boolean header, String label) {}

    public JournalScreen() {
        super(Component.literal("Dziennik zwiadowcy"));
        if (ClientQuestData.sync != null) selected = ClientQuestData.sync.tracked();
    }

    public void refresh() {
        rebuildWidgets();
    }

    private QuestSyncPacket.Extra extra() {
        return ClientQuestData.sync != null ? ClientQuestData.sync.extra() : QuestSyncPacket.Extra.EMPTY;
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        buildEntries();
        int by = top + H - 24;
        int x = left + 8;
        QuestSyncPacket.QuestView sel = find(selected);
        int tom = sel != null ? sel.tom() : (extra().has(QuestSyncPacket.TOM2_STARTED) ? 2 : 1);
        // prolog i epilog tomu wybranej misji
        int prologTom = tom;
        addRenderableWidget(Button.builder(Component.literal("Prolog"), b -> {
            if (minecraft == null) return;
            String id = prologTom == 2 ? StoryTexts.PROLOG_TOM2 : StoryTexts.PROLOG;
            minecraft.setScreen(new StoryScreen(id, StoryTexts.get(id), false));
        }).bounds(x, by, 44, 18).build());
        x += 48;
        boolean epilog1 = extra().has(QuestSyncPacket.TOM1_FINISHED_STORY);
        boolean epilog2 = extra().has(QuestSyncPacket.TOM2_DONE);
        if (tom == 2 ? epilog2 : epilog1) {
            addRenderableWidget(Button.builder(Component.literal("Epilog"), b -> {
                ModNetwork.sendToServer(new ActionPacket(prologTom == 2 ? ActionPacket.Kind.POKAZ_EPILOG_TOM2 : ActionPacket.Kind.POKAZ_EPILOG, ""));
                onClose();
            }).bounds(x, by, 44, 18).build());
            x += 48;
            addRenderableWidget(Button.builder(Component.literal("Koniec tomu"), b -> {
                ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.POKAZ_KONIEC_TOMU, String.valueOf(prologTom)));
                onClose();
            }).bounds(x, by, 70, 18).build());
            x += 74;
        }
        if (extra().has(QuestSyncPacket.TOM1_DONE) && !extra().has(QuestSyncPacket.TOM2_STARTED)) {
            addRenderableWidget(Button.builder(Component.literal("Tom II ▶"), b -> {
                ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.KONTYNUUJ_TOM2, ""));
                onClose();
            }).bounds(x, by, 60, 18).build());
            x += 64;
        }
        if (extra().has(QuestSyncPacket.TOM2_STARTED)) {
            String label = "Trudność: " + pl.zwiadowcy.difficulty.StoryDifficulty.byId(extra().difficulty()).label;
            addRenderableWidget(Button.builder(Component.literal(label), b ->
                    ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.TRUDNOSC, "nastepny"))).bounds(x, by, 104, 18).build());
        }
        trackButton = addRenderableWidget(Button.builder(Component.literal("Śledź"), b ->
                ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.SLEDZ_MISJE, selected))).bounds(left + W - 56, by, 48, 18).build());
        updateTrackButton();
        // odpowiedzi na pytanie śledztwa (zakładka Wnioski)
        List<String> ded = extra().deduction();
        if (tab == Tab.WNIOSKI && ded.size() > 2) {
            int oy = top + H - 30 - (ded.size() - 2) * 20;
            for (int i = 2; i < ded.size(); i++) {
                int idx = i - 2;
                addRenderableWidget(Button.builder(Component.literal(font.plainSubstrByWidth(ded.get(i), W - LIST_W - 30)), b -> {
                    ModNetwork.sendToServer(new ActionPacket(ActionPacket.Kind.WNIOSEK, ded.get(0) + ":" + idx));
                    onClose();
                }).bounds(left + LIST_W + 12, oy + idx * 20, W - LIST_W - 22, 18).build());
            }
        }
    }

    private void buildEntries() {
        entries.clear();
        QuestSyncPacket sync = ClientQuestData.sync;
        if (sync == null) return;
        int y = 0;
        for (int tom = 2; tom >= 1; tom--) {
            final int t = tom;
            List<QuestSyncPacket.QuestView> active = sync.quests().stream().filter(q -> q.tom() == t && !q.completed()).toList();
            List<QuestSyncPacket.QuestView> done = sync.quests().stream().filter(q -> q.tom() == t && q.completed()).toList();
            if (active.isEmpty() && done.isEmpty()) continue;
            entries.add(new Entry(y, "", true, tom == 2 ? "Tom II: Płonący most" : "Tom I: Ruiny Gorlanu"));
            y += 12;
            if (selected.isEmpty() && !active.isEmpty()) selected = active.get(0).id();
            for (var q : active) {
                entries.add(new Entry(y, q.id(), false, q.title()));
                y += 11;
            }
            for (var q : done) {
                entries.add(new Entry(y, q.id(), false, q.title()));
                y += 11;
            }
            y += 5;
        }
    }

    private void updateTrackButton() {
        QuestSyncPacket sync = ClientQuestData.sync;
        QuestSyncPacket.QuestView q = find(selected);
        trackButton.visible = tab == Tab.MISJE && q != null && !q.completed() && sync != null && !q.id().equals(sync.tracked());
    }

    private QuestSyncPacket.QuestView find(String id) {
        if (ClientQuestData.sync == null) return null;
        for (var q : ClientQuestData.sync.quests()) if (q.id().equals(id)) return q;
        return null;
    }

    private int listTop() { return top + 22; }
    private int listBottom() { return top + H - 30; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.fill(left - 3, top - 3, left + W + 3, top + H + 3, 0xFF4A3018);
        g.fill(left, top, left + W, top + H, 0xFFE9DCBC);
        g.fill(left + LIST_W + 4, top + 20, left + LIST_W + 5, top + H - 28, 0xFFB9A57E);
        g.drawString(font, "Dziennik zwiadowcy", left + 8, top + 7, ACCENT, false);
        renderTabs(g, mouseX, mouseY);

        // lista misji (przewijana)
        g.enableScissor(left + 4, listTop(), left + LIST_W + 2, listBottom());
        for (Entry e : entries) {
            int y = listTop() + e.y() - listScroll;
            if (y < listTop() - 12 || y > listBottom()) continue;
            if (e.header()) {
                g.drawString(font, e.label(), left + 8, y, ACCENT, false);
                continue;
            }
            boolean sel = e.questId().equals(selected);
            boolean hover = mouseX >= left + 8 && mouseX < left + LIST_W && mouseY >= y - 1 && mouseY < y + 10;
            if (sel) g.fill(left + 6, y - 1, left + LIST_W, y + 9, 0x30804020);
            QuestSyncPacket.QuestView q = find(e.questId());
            boolean tracked = ClientQuestData.sync != null && e.questId().equals(ClientQuestData.sync.tracked());
            int color = q != null && q.completed() ? INK_LIGHT : (hover ? ACCENT : INK);
            String mark = q != null && q.completed() ? "✔" : tracked ? "✦" : " ";
            g.drawString(font, mark + font.plainSubstrByWidth(e.label(), LIST_W - 16), left + 8, y, color, false);
        }
        g.disableScissor();

        int rx = left + LIST_W + 12, rw = W - LIST_W - 22;
        int y = top + 24 - scroll;
        g.enableScissor(rx, top + 20, rx + rw, top + H - 30);
        switch (tab) {
            case MISJE -> renderQuest(g, rx, y, rw);
            case NOTATKI -> renderNotes(g, rx, y, rw, false);
            case UMIEJETNOSCI -> renderSkills(g, rx, y, rw);
            case WNIOSKI -> renderDeductions(g, rx, y, rw);
            case DRUZYNA -> renderParty(g, rx, y, rw);
        }
        g.disableScissor();
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderTabs(GuiGraphics g, int mouseX, int mouseY) {
        tabBounds.clear();
        int x = left + W - 8;
        Tab[] tabs = Tab.values();
        for (int i = tabs.length - 1; i >= 0; i--) {
            Tab t = tabs[i];
            if (!tabVisible(t)) continue;
            int w = font.width(t.label);
            x -= w;
            boolean hover = mouseX >= x - 2 && mouseX < x + w + 2 && mouseY >= top + 5 && mouseY < top + 16;
            int color = t == tab ? ACCENT : hover ? INK : INK_LIGHT;
            g.drawString(font, t.label, x, top + 7, color, false);
            if (t == tab) g.fill(x, top + 16, x + w, top + 17, ACCENT);
            tabBounds.add(new int[]{t.ordinal(), x - 2, x + w + 2});
            x -= 10;
        }
    }

    private boolean tabVisible(Tab t) {
        boolean tom2 = extra().has(QuestSyncPacket.TOM2_STARTED);
        return switch (t) {
            case MISJE, NOTATKI -> true;
            case UMIEJETNOSCI, DRUZYNA -> tom2;
            case WNIOSKI -> tom2 && (!extra().clues().isEmpty() || !extra().deduction().isEmpty());
        };
    }

    private int lines(GuiGraphics g, String text, int x, int y, int w, int color) {
        for (FormattedCharSequence line : font.split(Component.literal(text), w)) {
            g.drawString(font, line, x, y, color, false);
            y += 10;
        }
        return y;
    }

    private void renderQuest(GuiGraphics g, int x, int y, int w) {
        QuestSyncPacket.QuestView q = find(selected);
        if (q == null) {
            g.drawString(font, "Brak wpisów. Użyj Kroniki Araluenu,", x, y, INK_LIGHT, false);
            g.drawString(font, "aby rozpocząć opowieść.", x, y + 10, INK_LIGHT, false);
            return;
        }
        g.drawString(font, q.title(), x, y, ACCENT, false);
        y += 11;
        if (!q.act().isEmpty()) {
            g.drawString(font, q.act(), x, y, INK_LIGHT, false);
            y += 12;
        }
        y = lines(g, q.description(), x, y, w, INK) + 6;
        for (String done : q.doneStages()) y = lines(g, "✔ " + done, x, y, w, INK_LIGHT);
        if (!q.completed() && !q.currentText().isEmpty()) {
            String cur = "▶ " + q.currentText() + (q.required() > 1 ? " (" + q.progress() + "/" + q.required() + ")" : "");
            y = lines(g, cur, x, y, w, INK);
        }
        if (q.completed()) g.drawString(font, q.repeatable() ? "Zlecenie wykonane." : "Misja ukończona.", x, y + 4, GREEN, false);
    }

    private void renderNotes(GuiGraphics g, int x, int y, int w, boolean cluesOnly) {
        QuestSyncPacket sync = ClientQuestData.sync;
        boolean any = false;
        if (sync != null) {
            for (QuestProgress.Note n : sync.notes()) {
                boolean clue = n.title().startsWith("Wskazówka");
                if (clue != cluesOnly) continue;
                any = true;
                g.drawString(font, n.title(), x, y, ACCENT, false);
                y = lines(g, n.text(), x, y + 11, w, INK) + 8;
            }
        }
        if (!any) g.drawString(font, cluesOnly ? "Brak wskazówek." : "Brak notatek.", x, y, INK_LIGHT, false);
    }

    private void renderSkills(GuiGraphics g, int x, int y, int w) {
        List<String> have = extra().skills();
        for (Skills.Skill s : Skills.ALL) {
            boolean got = have.contains(s.id());
            g.drawString(font, (got ? "★ " : "☆ ") + s.name(), x, y, got ? ACCENT : INK_LIGHT, false);
            y = lines(g, got ? s.effect() : "Zablokowana - " + s.source(), x + 10, y + 10, w - 10, got ? INK : INK_LIGHT) + 4;
        }
    }

    private void renderDeductions(GuiGraphics g, int x, int y, int w) {
        List<String> ded = extra().deduction();
        if (ded.size() > 1) {
            g.drawString(font, "Wniosek", x, y, ACCENT, false);
            y = lines(g, ded.get(1), x, y + 11, w, INK) + 6;
        }
        g.drawString(font, "Zebrane wskazówki: " + extra().clues().size(), x, y, INK_LIGHT, false);
        renderNotesFrom(g, x, y + 12, w);
    }

    private void renderNotesFrom(GuiGraphics g, int x, int y, int w) {
        QuestSyncPacket sync = ClientQuestData.sync;
        if (sync == null) return;
        for (QuestProgress.Note n : sync.notes()) {
            if (!n.title().startsWith("Wskazówka")) continue;
            g.drawString(font, n.title().substring(Math.min(n.title().length(), 11)), x, y, ACCENT, false);
            y = lines(g, n.text(), x, y + 10, w, INK) + 4;
        }
    }

    private void renderParty(GuiGraphics g, int x, int y, int w) {
        if (extra().trust().isEmpty()) {
            g.drawString(font, "Drużyna jeszcze się nie zebrała.", x, y, INK_LIGHT, false);
            return;
        }
        g.drawString(font, "Drużyna", x, y, ACCENT, false);
        y += 12;
        for (var e : extra().trust().entrySet()) y = lines(g, "• " + pl.zwiadowcy.companion.Trust.hint(e.getKey(), e.getValue()), x, y, w, INK) + 3;
        y += 6;
        lines(g, "Rozkazy dla drużyny: przytrzymaj G. Spójrz na towarzysza, by wydać rozkaz tylko jemu.", x, y, w, INK_LIGHT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY >= top + 5 && mouseY < top + 16) {
            for (int[] b : tabBounds) {
                if (mouseX >= b[1] && mouseX < b[2]) {
                    tab = Tab.values()[b[0]];
                    scroll = 0;
                    rebuildWidgets();
                    return true;
                }
            }
        }
        if (mouseX >= left + 6 && mouseX < left + LIST_W && mouseY >= listTop() && mouseY < listBottom()) {
            for (Entry e : entries) {
                int y = listTop() + e.y() - listScroll;
                if (!e.header() && mouseY >= y - 1 && mouseY < y + 10) {
                    selected = e.questId();
                    tab = Tab.MISJE;
                    scroll = 0;
                    rebuildWidgets();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX < left + LIST_W + 4) {
            int max = entries.isEmpty() ? 0 : Math.max(0, entries.get(entries.size() - 1).y() + 20 - (listBottom() - listTop()));
            listScroll = Math.max(0, Math.min(max, listScroll - (int) (delta * 12)));
        } else {
            scroll = Math.max(0, scroll - (int) (delta * 12));
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
