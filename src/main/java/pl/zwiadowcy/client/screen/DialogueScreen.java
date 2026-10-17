package pl.zwiadowcy.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;
import pl.zwiadowcy.entity.StoryNpc;
import pl.zwiadowcy.network.DialogueChoicePacket;
import pl.zwiadowcy.network.ModNetwork;
import pl.zwiadowcy.network.OpenDialoguePacket;

import java.util.ArrayList;
import java.util.List;

/** Okno rozmowy: portret postaci, tekst wyświetlany stopniowo, opcje odpowiedzi (także klawisze 1-9). */
public class DialogueScreen extends Screen {
    private static final int PANEL_W = 380;
    private OpenDialoguePacket msg;
    private long shownAt;
    private boolean answered;
    private final List<Button> optionButtons = new ArrayList<>();
    private int panelX, panelY, panelH;

    public DialogueScreen(OpenDialoguePacket msg) {
        super(Component.literal(msg.speakerName()));
        this.msg = msg;
        this.shownAt = System.currentTimeMillis();
    }

    public void show(OpenDialoguePacket next) {
        this.msg = next;
        this.shownAt = System.currentTimeMillis();
        this.answered = false;
        rebuildWidgets();
    }

    @Override
    protected void init() {
        optionButtons.clear();
        List<OpenDialoguePacket.OptionView> options = msg.options();
        int count = Math.max(1, options.size());
        int textLines = wrapped(msg.text()).size();
        panelH = 34 + Math.max(4, textLines) * 10 + 8 + count * 22;
        panelX = width / 2 - PANEL_W / 2;
        panelY = height - panelH - 12;
        int by = panelY + panelH - count * 22 - 4;
        int bx = panelX + 96;
        int bw = PANEL_W - 104;
        if (options.isEmpty()) {
            addOption(Component.literal("[Zakończ rozmowę]"), -1, bx, by, bw);
        } else {
            for (int i = 0; i < options.size(); i++) {
                OpenDialoguePacket.OptionView o = options.get(i);
                String prefix = (i + 1) + ". ";
                addOption(Component.literal(prefix + o.text() + (o.ends() ? "" : " …")), i, bx, by + i * 22, bw);
            }
        }
    }

    private void addOption(Component label, int index, int x, int y, int w) {
        Button b = Button.builder(label, btn -> choose(index)).bounds(x, y, w, 20).build();
        optionButtons.add(addRenderableWidget(b));
    }

    private List<FormattedCharSequence> wrapped(String text) {
        List<FormattedCharSequence> out = new ArrayList<>();
        for (String para : text.split("\n")) {
            if (para.isEmpty()) {
                out.add(FormattedCharSequence.EMPTY);
            } else {
                out.addAll(font.split(Component.literal(para), PANEL_W - 110));
            }
        }
        return out;
    }

    private int visibleChars() {
        return (int) ((System.currentTimeMillis() - shownAt) / 16);
    }

    private boolean fullyShown() {
        return visibleChars() >= msg.text().length();
    }

    private void choose(int index) {
        if (answered) return;
        if (!fullyShown() && index >= 0) {
            shownAt = 0;
            return;
        }
        answered = true;
        ModNetwork.sendToServer(new DialogueChoicePacket(index));
        boolean ends = index < 0 || index >= msg.options().size() || msg.options().get(index).ends();
        if (ends) Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, height / 2, width, height, 0x00000000, 0x90000000);
        g.fill(panelX - 2, panelY - 2, panelX + PANEL_W + 2, panelY + panelH + 2, 0xFF6B4A1E);
        g.fill(panelX, panelY, panelX + PANEL_W, panelY + panelH, 0xF01A140E);
        g.fill(panelX + 4, panelY + 4, panelX + 88, panelY + 92, 0xFF2A2018);

        LivingEntity portrait = portrait();
        if (portrait != null) {
            int px = panelX + 46, py = panelY + 88;
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, px, py, 38, px - mouseX, py - 60 - mouseY, portrait);
        }

        g.drawString(font, msg.speakerName(), panelX + 96, panelY + 8, 0xFFE0B04A, true);
        g.fill(panelX + 96, panelY + 19, panelX + PANEL_W - 8, panelY + 20, 0xFF6B4A1E);

        String text = msg.text();
        int vis = Math.min(text.length(), visibleChars());
        List<FormattedCharSequence> lines = wrapped(text.substring(0, vis));
        int y = panelY + 26;
        for (FormattedCharSequence line : lines) {
            g.drawString(font, line, panelX + 96, y, 0xFFEDE6D6, false);
            y += 10;
        }
        for (Button b : optionButtons) b.active = fullyShown() && !answered;
        super.render(g, mouseX, mouseY, partialTick);
        if (!fullyShown()) {
            g.drawString(font, "(kliknij, aby pominąć)", panelX + PANEL_W - 8 - font.width("(kliknij, aby pominąć)"),
                    panelY + 8, 0xFF8A7A60, false);
        }
    }

    private LivingEntity portrait() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return null;
        StoryNpc best = null;
        double bestD = 64 * 64;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof StoryNpc npc && npc.npcId().equals(msg.speakerId())) {
                double d = npc.distanceToSqr(mc.player);
                if (d < bestD) {
                    bestD = d;
                    best = npc;
                }
            }
        }
        if (best != null) return best;
        Entity src = msg.entityId() >= 0 ? mc.level.getEntity(msg.entityId()) : null;
        return src instanceof LivingEntity le ? le : null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!fullyShown()) {
            shownAt = 0;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_9) {
            int idx = key - GLFW.GLFW_KEY_1;
            if (msg.options().isEmpty() && idx == 0) choose(-1);
            else if (idx < msg.options().size()) choose(idx);
            return true;
        }
        if ((key == GLFW.GLFW_KEY_SPACE || key == GLFW.GLFW_KEY_ENTER) && !fullyShown()) {
            shownAt = 0;
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void onClose() {
        if (!answered) {
            answered = true;
            ModNetwork.sendToServer(new DialogueChoicePacket(-1));
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
