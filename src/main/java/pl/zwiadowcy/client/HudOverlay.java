package pl.zwiadowcy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import pl.zwiadowcy.client.screen.DialogueScreen;
import pl.zwiadowcy.item.QuiverItem;
import pl.zwiadowcy.item.RangerBowItem;
import pl.zwiadowcy.network.QuestSyncPacket;
import pl.zwiadowcy.world.Araluen;

import java.util.List;

/** HUD: śledzona misja z kierunkiem do celu, wskaźnik kamuflażu i wybrana strzała. */
public final class HudOverlay implements IGuiOverlay {
    public static final HudOverlay INSTANCE = new HudOverlay();
    private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};

    private HudOverlay() {}

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.screen instanceof DialogueScreen) return;
        Font font = mc.font;
        renderTracker(mc, g, font);
        renderStealth(mc, g, font, width, height);
        renderArrow(mc, g, font, width, height);
    }

    private void renderTracker(Minecraft mc, GuiGraphics g, Font font) {
        QuestSyncPacket sync = ClientQuestData.sync;
        QuestSyncPacket.QuestView q = ClientQuestData.tracked();
        if (sync == null || q == null || mc.options.renderDebug) return;
        int x = 6, y = 6, w = 190;
        String objective = q.currentText();
        if (q.required() > 1) objective += " (" + q.progress() + "/" + q.required() + ")";
        if (q.awaitNight() && mc.level != null) {
            // czas do zmroku (cel "night" spełnia się od 13000 ticku doby); 1 minuta = 1200 ticków
            long t = Math.floorMod(mc.level.getDayTime(), 24000L);
            if (t < 13000 || t > 23000) {
                long ticks = t < 13000 ? 13000 - t : 24000 - t + 13000;
                long min = (ticks + 1199) / 1200;
                objective += min <= 1 ? " (zmrok za chwilę)" : " (zmrok za ok. " + min + " min)";
            }
        }
        List<FormattedCharSequence> lines = font.split(Component.literal(objective), w - 8);
        String nav = navigation(mc.player, sync);
        int h = 16 + lines.size() * 10 + (nav != null ? 11 : 0);
        g.fill(x - 2, y - 2, x + w, y + h, 0x88000000);
        g.fill(x - 2, y - 2, x, y + h, 0xFFB8862B);
        g.drawString(font, "✦ " + q.title(), x + 3, y + 1, 0xFFE0B04A, true);
        int ly = y + 13;
        for (FormattedCharSequence line : lines) {
            g.drawString(font, line, x + 5, ly, 0xFFEFEFEF, true);
            ly += 10;
        }
        if (nav != null) g.drawString(font, nav, x + 5, ly + 1, 0xFF9FD08A, true);
    }

    private String navigation(Player player, QuestSyncPacket sync) {
        BlockPos t = sync.target();
        if (t == null) return null;
        String name = sync.targetName().isEmpty() ? "" : sync.targetName() + " ";
        if (!Araluen.isAraluen(player.level())) return name + "(w Araluenie - użyj Kroniki)";
        double dx = t.getX() + 0.5 - player.getX();
        double dz = t.getZ() + 0.5 - player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 4) return name + "- jesteś na miejscu";
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float rel = Mth.wrapDegrees(targetYaw - player.getYRot());
        int idx = Math.floorMod(Math.round(rel / 45f), 8);
        return ARROWS[idx] + " " + name + Math.round(dist) + " m";
    }

    private void renderStealth(Minecraft mc, GuiGraphics g, Font font, int width, int height) {
        if (!ClientQuestData.cloak && ClientQuestData.bezruchTicks <= 0) return;
        int vis = ClientQuestData.visibility;
        String label;
        int color;
        if (ClientQuestData.bezruchTicks > 0) {
            label = "Bezruch " + (ClientQuestData.bezruchTicks / 20 + 1) + " s";
            color = 0xFF7FD7FF;
        } else if (vis < 20) {
            label = "Niewidoczny";
            color = 0xFF5FD35F;
        } else if (vis < 45) {
            label = "Ukryty";
            color = 0xFFA6D35F;
        } else if (vis < 75) {
            label = "Częściowo widoczny";
            color = 0xFFE0C04A;
        } else {
            label = "Widoczny";
            color = 0xFFE05A4A;
        }
        String text = "◆ " + label;
        int x = width / 2 - 91 - 8 - font.width(text);
        int y = height - 14;
        g.drawString(font, text, x, y, color, true);
    }

    private void renderArrow(Minecraft mc, GuiGraphics g, Font font, int width, int height) {
        Player p = mc.player;
        if (p == null) return;
        boolean bow = p.getMainHandItem().getItem() instanceof RangerBowItem || p.getOffhandItem().getItem() instanceof RangerBowItem;
        if (!bow) return;
        ItemStack quiver = QuiverItem.findQuiver(p);
        if (quiver.isEmpty()) return;
        ItemStack sel = QuiverItem.selectedArrow(quiver);
        String text = sel.isEmpty() ? "Kołczan pusty" : "➶ " + sel.getHoverName().getString() + " ×" + sel.getCount();
        g.drawString(font, text, width / 2 + 91 + 8, height - 14, 0xFFE8E0C8, true);
    }
}
